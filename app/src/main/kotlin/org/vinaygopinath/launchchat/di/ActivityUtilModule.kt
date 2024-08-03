package org.vinaygopinath.launchchat.di

import android.content.ClipboardManager
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil

@Module
@InstallIn(ActivityComponent::class)
internal object ActivityUtilModule {

    @Provides
    fun providePhoneNumberUtil(@ActivityContext context: Context): PhoneNumberUtil {
        return PhoneNumberUtil.createInstance(context)
    }

    @Provides
    fun provideClipboardManager(@ActivityContext context: Context): ClipboardManager {
        return context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }
}