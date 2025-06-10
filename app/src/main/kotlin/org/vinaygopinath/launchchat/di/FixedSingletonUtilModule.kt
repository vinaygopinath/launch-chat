package org.vinaygopinath.launchchat.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil

@Module
@InstallIn(SingletonComponent::class)
object FixedSingletonUtilModule {

    @Provides
    fun providePhoneNumberUtil(@ApplicationContext context: Context): PhoneNumberUtil {
        return PhoneNumberUtil.createInstance(context)
    }
}
