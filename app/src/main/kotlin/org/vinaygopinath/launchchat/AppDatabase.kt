package org.vinaygopinath.launchchat

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.vinaygopinath.launchchat.converters.RoomTypeConverter
import org.vinaygopinath.launchchat.daos.ActionDao
import org.vinaygopinath.launchchat.daos.ActivityDao
import org.vinaygopinath.launchchat.daos.DetailedActivityDao
import org.vinaygopinath.launchchat.models.Action
import org.vinaygopinath.launchchat.models.Activity
import org.vinaygopinath.launchchat.models.ChatApp

@Database(
    entities = [
        Activity::class,
        Action::class,
        ChatApp::class
    ],
    version = 2
)
@TypeConverters(RoomTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao
    abstract fun actionDao(): ActionDao
    abstract fun detailedActivityDao(): DetailedActivityDao

    companion object {
        fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "launch-chat"
            ).build()
        }
    }
}
