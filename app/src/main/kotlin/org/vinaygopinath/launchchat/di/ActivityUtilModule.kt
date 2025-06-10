package org.vinaygopinath.launchchat.di

import android.content.ClipboardManager
import android.content.Context
import android.content.res.Resources
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext

@Module
@InstallIn(ActivityComponent::class)
internal object ActivityUtilModule {

    @Provides
    fun provideClipboardManager(@ActivityContext context: Context): ClipboardManager {
        return context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    @Provides
    fun provideResources(@ActivityContext context: Context): Resources {
        return context.resources
    }
}
