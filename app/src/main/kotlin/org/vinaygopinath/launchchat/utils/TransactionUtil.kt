package org.vinaygopinath.launchchat.utils

import androidx.annotation.OpenForTesting
import androidx.room.withTransaction
import org.vinaygopinath.launchchat.AppDatabase
import javax.inject.Inject

@OpenForTesting
class TransactionUtil @Inject constructor(
    private val database: AppDatabase
) {

    suspend fun <T> run(lambda: suspend () -> T): T {
        return database.withTransaction(lambda)
    }
}
