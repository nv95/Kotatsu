package org.koitharu.kotatsu.history.domain

import androidx.collection.ArraySet
import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koitharu.kotatsu.core.db.MangaDatabase
import org.koitharu.kotatsu.core.db.entity.MangaEntity
import org.koitharu.kotatsu.core.db.entity.TagEntity
import org.koitharu.kotatsu.core.model.Manga
import org.koitharu.kotatsu.core.model.MangaHistory
import org.koitharu.kotatsu.history.data.HistoryEntity
import org.koitharu.kotatsu.tracker.domain.TrackingRepository
import org.koitharu.kotatsu.utils.ext.mapItems
import org.koitharu.kotatsu.utils.ext.mapToSet

class HistoryRepository(private val db: MangaDatabase) : KoinComponent {

	private val trackingRepository by inject<TrackingRepository>()

	suspend fun getList(offset: Int, limit: Int = 20): List<Manga> {
		val entities = db.historyDao.findAll(offset, limit)
		return entities.map { it.manga.toManga(it.tags.mapToSet(TagEntity::toMangaTag)) }
	}

	fun observeAll(): Flow<List<Manga>> {
		return db.historyDao.observeAll().mapItems {
			it.manga.toManga(it.tags.mapToSet(TagEntity::toMangaTag))
		}
	}

	fun observeAllWithHistory(): Flow<List<MangaWithHistory>> {
		return db.historyDao.observeAll().mapItems {
			MangaWithHistory(
				it.manga.toManga(it.tags.mapToSet(TagEntity::toMangaTag)),
				it.history.toMangaHistory()
			)
		}
	}

	fun observeOne(id: Long): Flow<MangaHistory?> {
		return db.historyDao.observe(id).map {
			it?.toMangaHistory()
		}
	}

	suspend fun addOrUpdate(manga: Manga, chapterId: Long, page: Int, scroll: Int) {
		val tags = manga.tags.map(TagEntity.Companion::fromMangaTag)
		db.withTransaction {
			db.tagsDao.upsert(tags)
			db.mangaDao.upsert(MangaEntity.from(manga), tags)
			db.historyDao.upsert(
				HistoryEntity(
					mangaId = manga.id,
					createdAt = System.currentTimeMillis(),
					updatedAt = System.currentTimeMillis(),
					chapterId = chapterId,
					page = page,
					scroll = scroll.toFloat() // we migrate to int, but decide to not update database
				)
			)
			trackingRepository.upsert(manga)
		}
		notifyHistoryChanged()
	}

	suspend fun getOne(manga: Manga): MangaHistory? {
		return db.historyDao.find(manga.id)?.toMangaHistory()
	}

	suspend fun clear() {
		db.historyDao.clear()
		notifyHistoryChanged()
	}

	suspend fun delete(manga: Manga) {
		db.historyDao.delete(manga.id)
		notifyHistoryChanged()
	}

	/**
	 * Try to replace one manga with another one
	 * Useful for replacing saved manga on deleting it with remove source
	 */
	suspend fun deleteOrSwap(manga: Manga, alternative: Manga?) {
		if (alternative == null || db.mangaDao.update(MangaEntity.from(alternative)) <= 0) {
			db.historyDao.delete(manga.id)
			notifyHistoryChanged()
		}
	}

	companion object {

		private val listeners = ArraySet<OnHistoryChangeListener>()

		fun subscribe(listener: OnHistoryChangeListener) {
			listeners += listener
		}

		fun unsubscribe(listener: OnHistoryChangeListener) {
			listeners -= listener
		}

		private suspend fun notifyHistoryChanged() {
			withContext(Dispatchers.Main) {
				listeners.forEach { x -> x.onHistoryChanged() }
			}
		}
	}
}