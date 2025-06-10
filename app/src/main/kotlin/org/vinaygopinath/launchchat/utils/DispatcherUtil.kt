package org.vinaygopinath.launchchat.utils

import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@Suppress("InjectDispatcher")
class DispatcherUtil @Inject constructor() {

    fun getIoDispatcher() = Dispatchers.IO
}
