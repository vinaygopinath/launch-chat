package org.vinaygopinath.launchchat.fakes

import org.vinaygopinath.launchchat.AppDatabase
import org.vinaygopinath.launchchat.utils.TransactionUtil

class TransactionUtilFake(database: AppDatabase) : TransactionUtil(database) {

    override suspend fun <T> run(lambda: suspend () -> T): T {
        return lambda()
    }
}