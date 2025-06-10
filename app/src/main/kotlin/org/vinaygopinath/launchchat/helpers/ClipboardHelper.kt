package org.vinaygopinath.launchchat.helpers

import android.content.ClipDescription
import android.content.ClipboardManager
import javax.inject.Inject

class ClipboardHelper @Inject constructor(
    private val clipboardManager: ClipboardManager
) {

    fun readClipboardContent(): ClipboardContent {
        val clip = clipboardManager.primaryClip
        val description = clipboardManager.primaryClipDescription
        return if (clip == null || description == null || clip.itemCount == 0) {
            ClipboardContent.Empty
        } else if (!description.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
            ClipboardContent.InvalidClipboardData
        } else {
            ClipboardContent.ClipboardData(clip.getItemAt(0).text.toString())
        }
    }

    sealed class ClipboardContent {
        data class ClipboardData(val content: String) : ClipboardContent()
        data object InvalidClipboardData : ClipboardContent()
        data object Empty : ClipboardContent()
    }
}
