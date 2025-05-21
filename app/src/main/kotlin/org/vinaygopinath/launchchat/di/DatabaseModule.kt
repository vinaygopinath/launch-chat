package org.vinaygopinath.launchchat.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.vinaygopinath.launchchat.AppDatabase
import org.vinaygopinath.launchchat.utils.DateUtils
import org.vinaygopinath.launchchat.utils.TransactionUtil
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        dateUtils: DateUtils
    ): AppDatabase {
        return AppDatabase.buildDatabase(context, dateUtils)
    }

    @Provides
    @Singleton
    fun provideTransactionUtil(database: AppDatabase): TransactionUtil {
        return TransactionUtil(database)
    }
}
