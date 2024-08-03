package org.vinaygopinath.launchchat.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.vinaygopinath.launchchat.utils.ClockProvider

@Module
@InstallIn(SingletonComponent::class)
object ClockModule {

    @Provides
    fun provideClockProvider() = ClockProvider()
}