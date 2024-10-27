package org.vinaygopinath.launchchat.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import org.vinaygopinath.launchchat.AppDatabase
import javax.inject.Singleton

@Module
@TestInstallIn(
    replaces = [DatabaseModule::class],
    components = [SingletonComponent::class]
)
class TestDatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }
}