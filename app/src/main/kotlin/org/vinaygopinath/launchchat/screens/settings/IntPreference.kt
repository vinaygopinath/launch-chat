package org.vinaygopinath.launchchat.screens.settings

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import androidx.preference.EditTextPreference

class IntPreference : EditTextPreference {
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int): super(context, attrs, defStyle)

    init {
        setOnBindEditTextListener { listener ->
            listener.inputType = InputType.TYPE_CLASS_NUMBER
        }
    }

    override fun getPersistedString(defaultReturnValue: String?): String? {
        val persistedInt = getPersistedInt(INVALID_INT_VALUE)

        return if (persistedInt == INVALID_INT_VALUE) {
            null
        } else {
            "$persistedInt"
        }
    }

    override fun persistString(value: String?): Boolean {
        return value?.let { persistInt(Integer.parseInt(it)) } == true
    }

    companion object {
        private const val INVALID_INT_VALUE = -1
    }
}