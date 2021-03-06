package org.koitharu.kotatsu.core.db.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import org.koitharu.kotatsu.utils.ext.mapToSet

data class MangaWithTags(
	@Embedded val manga: MangaEntity,
	@Relation(
		parentColumn = "manga_id",
		entityColumn = "tag_id",
		associateBy = Junction(MangaTagsEntity::class)
	)
	val tags: List<TagEntity>
) {

	fun toManga() = manga.toManga(tags.mapToSet {
		it.toMangaTag()
	})
}