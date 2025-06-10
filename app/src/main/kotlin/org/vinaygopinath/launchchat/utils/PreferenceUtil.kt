package org.vinaygopinath.launchchat.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.OpenForTesting
import androidx.annotation.StringRes
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@OpenForTesting
class PreferenceUtil @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferences: SharedPreferences
) {

    fun getString(key: String, default: String?): String? {
        return preferences.getString(key, default)
    }

    fun getString(@StringRes key: Int, default: String?): String? {
        return getString(context.getString(key), default)
    }

    fun setString(key: String, value: String?) {
        preferences.edit { putString(key, value) }
    }

    fun clear(key: String) {
        preferences.edit { remove(key) }
    }

    fun getBoolean(key: String, default: Boolean): Boolean {
        return preferences.getBoolean(key, default)
    }

    fun getBoolean(@StringRes key: Int, default: Boolean): Boolean {
        return getBoolean(context.getString(key), default)
    }

    fun getInt(key: String, default: Int): Int {
        return preferences.getInt(key, default)
    }

    fun getInt(@StringRes key: Int, default: Int): Int {
        return getInt(context.getString(key), default)
    }

    fun setInt(key: String, value: Int) {
        preferences.edit { putInt(key, value) }
    }

    fun getInt(key: String): Int? {
        val result = getInt(key, Int.MIN_VALUE)

        return if (result == Int.MIN_VALUE) {
            null
        } else {
            result
        }
    }
}
