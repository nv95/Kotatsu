package org.koitharu.kotatsu.favourites.ui.categories

import android.content.Context
import android.text.InputType
import androidx.appcompat.app.AlertDialog
import org.koitharu.kotatsu.R
import org.koitharu.kotatsu.base.ui.dialog.TextInputDialog
import org.koitharu.kotatsu.core.model.FavouriteCategory

class CategoriesEditDelegate(
	private val context: Context,
	private val callback: CategoriesEditCallback
) {

	fun deleteCategory(category: FavouriteCategory) {
		AlertDialog.Builder(context)
			.setMessage(context.getString(R.string.category_delete_confirm, category.title))
			.setTitle(R.string.remove_category)
			.setNegativeButton(android.R.string.cancel, null)
			.setPositiveButton(R.string.remove) { _, _ ->
				callback.onDeleteCategory(category)
			}.create()
			.show()
	}

	fun renameCategory(category: FavouriteCategory) {
		TextInputDialog.Builder(context)
			.setTitle(R.string.rename)
			.setText(category.title)
			.setHint(R.string.enter_category_name)
			.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
			.setNegativeButton(android.R.string.cancel)
			.setMaxLength(MAX_TITLE_LENGTH, false)
			.setPositiveButton(R.string.rename) { _, name ->
				callback.onRenameCategory(category, name)
			}.create()
			.show()
	}

	fun createCategory() {
		TextInputDialog.Builder(context)
			.setTitle(R.string.add_new_category)
			.setHint(R.string.enter_category_name)
			.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
			.setNegativeButton(android.R.string.cancel)
			.setMaxLength(MAX_TITLE_LENGTH, false)
			.setPositiveButton(R.string.add) { _, name ->
				callback.onCreateCategory(name)
			}.create()
			.show()
	}

	interface CategoriesEditCallback {

		fun onDeleteCategory(category: FavouriteCategory)

		fun onRenameCategory(category: FavouriteCategory, newName: String)

		fun onCreateCategory(name: String)
	}

	private companion object {

		const val MAX_TITLE_LENGTH = 24
	}
}