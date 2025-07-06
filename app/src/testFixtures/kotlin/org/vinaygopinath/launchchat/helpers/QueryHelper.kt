package org.vinaygopinath.launchchat.helpers

import org.vinaygopinath.launchchat.AppDatabase
import javax.inject.Inject

class QueryHelper @Inject constructor(private val database: AppDatabase) {

    fun queryTableRowCount(tableName: String): Int {
        return queryCount("SELECT COUNT(*) FROM $tableName")
    }

    fun queryRecordCountById(tableName: String, ids: Set<Long>): Int {
        return queryCount(
            "SELECT COUNT(*) FROM $tableName WHERE id IN ${ids.serializeToSqliteArg()}"
        )
    }

    private fun queryCount(queryString: String): Int {
        return database.query(queryString, null).use { cursor ->
            if (cursor.count != 1 && cursor.columnCount != 1) {
                error("Unexpected query result. Expected one row with one column")
            }

            cursor.moveToFirst()
            cursor.getInt(0)
        }
    }

    private fun Set<Long>.serializeToSqliteArg(): String {
        return "(${this.joinToString(",")})"
    }
}