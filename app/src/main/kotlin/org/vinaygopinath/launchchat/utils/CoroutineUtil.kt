package org.vinaygopinath.launchchat.utils

import androidx.annotation.WorkerThread
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object CoroutineUtil {

    private sealed class CoroutineResult<T> {
        data class Success<T>(val result: T) : CoroutineResult<T>()
        class Empty<T> : CoroutineResult<T>()
    }

    fun <T> doWorkInBackgroundAndGetResult(
        viewModelScope: CoroutineScope,
        dispatcherUtil: DispatcherUtil,
        doWork: suspend () -> T,
        @WorkerThread onResult: (suspend (result: T) -> Unit),
        @WorkerThread onError: (suspend (t: Throwable) -> Unit)
    ) {
        viewModelScope.launch {
            withContext(dispatcherUtil.getIoDispatcher()) {
                var result: CoroutineResult<T> = CoroutineResult.Empty()
                try {
                    result = CoroutineResult.Success(doWork())
                } catch (genericThrowable: Throwable) {
                    onError(genericThrowable)
                }

                if (result is CoroutineResult.Success) {
                    onResult(result.result)
                }
            }
        }
    }

    fun doWorkInBackground(
        viewModelScope: CoroutineScope,
        dispatcherUtil: DispatcherUtil,
        doWork: suspend () -> Unit,
        @WorkerThread onComplete: (suspend () -> Unit) = { },
        @WorkerThread onError: (suspend (t: Throwable) -> Unit) = { }
    ) {
        viewModelScope.launch {
            withContext(dispatcherUtil.getIoDispatcher()) {
                var hasCompleted = false
                try {
                    doWork()
                    hasCompleted = true
                } catch (genericThrowable: Throwable) {
                    onError(genericThrowable)
                }

                if (hasCompleted) {
                    onComplete()
                }
            }
        }
    }
}
