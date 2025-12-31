package org.vinaygopinath.launchchat.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.vinaygopinath.launchchat.AppDatabase
import org.vinaygopinath.launchchat.daos.ActionDao
import org.vinaygopinath.launchchat.daos.ActivityDao
import org.vinaygopinath.launchchat.daos.ChatAppDao
import org.vinaygopinath.launchchat.daos.DetailedActivityDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseDaoModule {

    @Provides
    @Singleton
    fun provideActivityDao(database: AppDatabase): ActivityDao {
        return database.activityDao()
    }

    @Provides
    @Singleton
    fun provideActionDao(database: AppDatabase): ActionDao {
        return database.actionDao()
    }

    @Provides
    @Singleton
    fun provideDetailedActivityDao(database: AppDatabase): DetailedActivityDao {
        return database.detailedActivityDao()
    }

    @Provides
    @Singleton
    fun provideChatAppDao(database: AppDatabase): ChatAppDao {
        return database.chatAppDao()
    }
}
