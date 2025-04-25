package org.vinaygopinath.launchchat.fakes

import android.content.SharedPreferences

class SharedPreferenceFake : SharedPreferences {
    private val stringMap = mutableMapOf<String, String?>()
    private val intMap = mutableMapOf<String, Int>()
    private val booleanMap = mutableMapOf<String, Boolean>()
    private val floatMap = mutableMapOf<String, Float>()
    private val longMap = mutableMapOf<String, Long>()
    private val stringSetMap = mutableMapOf<String, Set<String>>()
    private val editor by lazy { EditorFake() }
    private var listener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    override fun getAll(): Map<String?, *> {
        return stringMap + intMap + booleanMap + floatMap + longMap + stringSetMap
    }

    override fun getString(key: String?, defValue: String?): String? {
        return if (stringMap.contains(key)) {
            stringMap[key]
        } else {
            defValue
        }
    }

    override fun getStringSet(
        key: String?,
        defValues: Set<String?>?
    ): Set<String?>? {
        return stringSetMap[key] ?: defValues
    }

    override fun getInt(key: String?, defValue: Int): Int {
        return intMap[key] ?: defValue
    }

    override fun getLong(key: String?, defValue: Long): Long {
        return longMap[key] ?: defValue
    }

    override fun getFloat(key: String?, defValue: Float): Float {
        return floatMap[key] ?: defValue
    }

    override fun getBoolean(key: String?, defValue: Boolean): Boolean {
        return if (booleanMap.contains(key)) {
            booleanMap[key]!!
        } else {
            defValue
        }
    }

    override fun contains(key: String?): Boolean {
        return listOf(stringMap, booleanMap, intMap).any { it.contains(key) }
    }

    override fun edit(): SharedPreferences.Editor = editor

    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        this.listener = listener
    }

    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        this.listener = null
    }

    inner class EditorFake : SharedPreferences.Editor {
        override fun putString(
            key: String,
            value: String?
        ): SharedPreferences.Editor {
            stringMap[key] = value
            listener?.onSharedPreferenceChanged(this@SharedPreferenceFake, key)
            return this
        }

        override fun putStringSet(
            key: String,
            values: Set<String>?
        ): SharedPreferences.Editor {
            if (values == null) {
                stringSetMap.remove(key)
            } else {
                stringSetMap[key] = values
            }
            listener?.onSharedPreferenceChanged(this@SharedPreferenceFake, key)
            return this
        }

        override fun putInt(
            key: String,
            value: Int
        ): SharedPreferences.Editor {
            intMap[key] = value
            listener?.onSharedPreferenceChanged(this@SharedPreferenceFake, key)
            return this
        }

        override fun putLong(
            key: String,
            value: Long
        ): SharedPreferences.Editor {
            longMap[key] = value
            listener?.onSharedPreferenceChanged(this@SharedPreferenceFake, key)
            return this
        }

        override fun putFloat(
            key: String,
            value: Float
        ): SharedPreferences.Editor {
            floatMap[key] = value
            listener?.onSharedPreferenceChanged(this@SharedPreferenceFake, key)
            return this
        }

        override fun putBoolean(
            key: String,
            value: Boolean
        ): SharedPreferences.Editor {
            booleanMap[key] = value
            listener?.onSharedPreferenceChanged(this@SharedPreferenceFake, key)
            return this
        }

        override fun remove(key: String): SharedPreferences.Editor {
            stringMap.remove(key)
            intMap.remove(key)
            booleanMap.remove(key)
            floatMap.remove(key)
            longMap.remove(key)
            stringSetMap.remove(key)
            listener?.onSharedPreferenceChanged(this@SharedPreferenceFake, key)

            return this
        }

        override fun clear(): SharedPreferences.Editor {
            stringMap.clear()
            intMap.clear()
            booleanMap.clear()
            floatMap.clear()
            longMap.clear()
            stringSetMap.clear()

            return this
        }

        override fun commit() = true

        override fun apply() {
            // Do nothing
        }
    }
}