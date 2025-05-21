package org.vinaygopinath.launchchat

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.vinaygopinath.launchchat.converters.RoomTypeConverter
import org.vinaygopinath.launchchat.daos.ActionDao
import org.vinaygopinath.launchchat.daos.ActivityDao
import org.vinaygopinath.launchchat.daos.ChatAppDao
import org.vinaygopinath.launchchat.daos.DetailedActivityDao
import org.vinaygopinath.launchchat.extensions.addConditionalDataMigration
import org.vinaygopinath.launchchat.models.Action
import org.vinaygopinath.launchchat.models.Activity
import org.vinaygopinath.launchchat.models.ChatApp
import org.vinaygopinath.launchchat.utils.DateUtils

@Database(
    entities = [
        Activity::class,
        Action::class,
        ChatApp::class
    ],
    version = 2,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ]
)
@TypeConverters(RoomTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao
    abstract fun actionDao(): ActionDao
    abstract fun detailedActivityDao(): DetailedActivityDao
    abstract fun chatAppDao(): ChatAppDao

    companion object {
        fun buildDatabase(context: Context, dateUtils: DateUtils): AppDatabase {
            return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "launch-chat"
            ).build()
                .addConditionalDataMigration(
                    condition = { it.chatAppDao().getCount() == 0 },
                    action = {
                        it.chatAppDao().create(ChatApp.getPredefinedChatApps(dateUtils))
                    }
                )
        }
    }
}
