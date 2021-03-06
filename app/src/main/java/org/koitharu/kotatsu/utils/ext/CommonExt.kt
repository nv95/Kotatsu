package org.koitharu.kotatsu.utils.ext

import android.content.res.Resources
import android.util.Log
import kotlinx.coroutines.delay
import org.koitharu.kotatsu.R
import org.koitharu.kotatsu.core.exceptions.*
import java.io.FileNotFoundException
import java.net.SocketTimeoutException

suspend inline fun <T, R> T.retryUntilSuccess(maxAttempts: Int, action: T.() -> R): R {
	var attempts = maxAttempts
	while (true) {
		try {
			return this.action()
		} catch (e: Exception) {
			attempts--
			if (attempts <= 0) {
				throw e
			} else {
				delay(1000)
			}
		}
	}
}

fun Throwable.getDisplayMessage(resources: Resources) = when (this) {
	is AuthRequiredException -> resources.getString(R.string.auth_required)
	is CloudFlareProtectedException -> resources.getString(R.string.captcha_required)
	is UnsupportedOperationException -> resources.getString(R.string.operation_not_supported)
	is UnsupportedFileException -> resources.getString(R.string.text_file_not_supported)
	is FileNotFoundException -> resources.getString(R.string.file_not_found)
	is EmptyHistoryException -> resources.getString(R.string.history_is_empty)
	is SocketTimeoutException -> resources.getString(R.string.network_error)
	is WrongPasswordException -> resources.getString(R.string.wrong_password)
	else -> localizedMessage ?: resources.getString(R.string.error_occurred)
}

inline fun <T> measured(tag: String, block: () -> T): T {
	val time = System.currentTimeMillis()
	val res = block()
	val spent = System.currentTimeMillis() - time
	Log.d("measured", "$tag ${spent.format(1)} ms")
	return res
}