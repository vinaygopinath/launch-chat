package org.vinaygopinath.launchchat.extensions

import org.vinaygopinath.launchchat.AppDatabase

fun AppDatabase.addConditionalDataMigration(
    condition: (database: AppDatabase) -> Boolean,
    action: (database: AppDatabase) -> Unit
): AppDatabase {
    transactionExecutor.execute {
        if (condition(this)) {
            runInTransaction {
                action(this)
            }
        }
    }

    return this
}
