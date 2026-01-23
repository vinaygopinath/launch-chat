package org.vinaygopinath.launchchat.helpers

import android.content.Context
import android.view.LayoutInflater
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import org.vinaygopinath.launchchat.R
import org.vinaygopinath.launchchat.models.DetailedActivity

object NoteDialogHelper {

    fun showNoteDialog(
        context: Context,
        detailedActivity: DetailedActivity,
        onNoteSaved: (activityId: Long, note: String) -> Unit
    ) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_note, null)
        val noteInput = dialogView.findViewById<TextInputEditText>(R.id.note_input)
        noteInput.setText(detailedActivity.activity.note)

        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.history_note_dialog_title)
            .setView(dialogView)
            .setPositiveButton(R.string.history_note_dialog_save) { _, _ ->
                val note = noteInput.text.toString()
                onNoteSaved(detailedActivity.activity.id, note)
            }
            .setNegativeButton(R.string.history_note_dialog_cancel, null)
            .show()
    }
}
