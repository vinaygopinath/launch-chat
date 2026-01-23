package org.vinaygopinath.launchchat.screens.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.NO_POSITION
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import org.vinaygopinath.launchchat.R
import org.vinaygopinath.launchchat.helpers.DetailedActivityHelper
import org.vinaygopinath.launchchat.models.DetailedActivity

class HistoryAdapter(
    private val helper: DetailedActivityHelper,
    private val listener: HistoryAdapterListener
) : PagingDataAdapter<DetailedActivity, HistoryAdapter.HistoryViewHolder>(
    DetailedActivityDiffCallback()
) {

    val selectedActivities = mutableSetOf<DetailedActivity>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.list_item_history,
            parent,
            false
        )
        val viewHolder = HistoryViewHolder(view)
        viewHolder.itemView.setOnLongClickListener {
            val position = viewHolder.bindingAdapterPosition
            if (position != NO_POSITION) {
                getItem(position)?.let { item ->
                    toggleItemSelection(item, position)
                }
            }

            true
        }
        viewHolder.itemView.setOnClickListener {
            val position = viewHolder.bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                getItem(position)?.let { item ->
                    listener.onClick(item)
                }
            }
        }
        viewHolder.noteButton.setOnClickListener {
            val position = viewHolder.bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                getItem(position)?.let { item ->
                    listener.onNoteButtonClick(item)
                }
            }
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        if (position == NO_POSITION) {
            return
        }

        getItem(position)?.let { item ->
            val isSelected = selectedActivities.contains(item)
            getItem(position)?.let { detailedActivity ->
                holder.titleText.setText(helper.getSourceDisplayName(detailedActivity))
                holder.timestampText.text = helper.getActivityShortTimestamp(detailedActivity)
                holder.contentText.text = helper.getActivityContent(detailedActivity)
                holder.actionsText.text = helper.getActionsText(detailedActivity)
                holder.selectedIcon.isVisible = isSelected

                val note = detailedActivity.activity.note
                holder.noteText.isVisible = !note.isNullOrBlank()
                holder.noteText.text = note
                holder.noteButton.setText(
                    if (note.isNullOrBlank()) {
                        R.string.history_note_add
                    } else {
                        R.string.history_note_update
                    }
                )
            }
        }
    }

    fun toggleItemSelection(item: DetailedActivity, position: Int) {
        if (selectedActivities.contains(item)) {
            selectedActivities.remove(item)
        } else {
            selectedActivities.add(item)
        }
        notifyItemChanged(position)
        listener.onItemSelectionChanged(selectedActivities)
    }

    fun clearSelection() {
        selectedActivities.clear()
        notifyDataSetChanged()
        listener.onItemSelectionChanged(selectedActivities)
    }

    fun getSelectedItems(): Set<DetailedActivity> {
        return selectedActivities.toSet()
    }

    fun hasSelectedItems(): Boolean {
        return selectedActivities.isNotEmpty()
    }

    inner class HistoryViewHolder(view: View) : ViewHolder(view) {
        val titleText: MaterialTextView =
            view.findViewById(R.id.history_list_title)
        val timestampText: MaterialTextView =
            view.findViewById(R.id.history_list_timestamp)
        val contentText: MaterialTextView =
            view.findViewById(R.id.history_list_content)
        val actionsText: MaterialTextView =
            view.findViewById(R.id.history_list_actions)
        val selectedIcon: ImageView = view.findViewById(R.id.history_list_selected_icon)
        val noteText: MaterialTextView =
            view.findViewById(R.id.history_list_note)
        val noteButton: MaterialButton =
            view.findViewById(R.id.history_list_note_button)
    }

    class DetailedActivityDiffCallback : ItemCallback<DetailedActivity>() {
        override fun areItemsTheSame(
            oldItem: DetailedActivity,
            newItem: DetailedActivity
        ) = oldItem.activity.id == newItem.activity.id

        override fun areContentsTheSame(
            oldItem: DetailedActivity,
            newItem: DetailedActivity
        ) = oldItem == newItem
    }

    interface HistoryAdapterListener {
        fun onClick(detailedActivity: DetailedActivity)
        fun onItemSelectionChanged(selectedItems: Set<DetailedActivity>)
        fun onNoteButtonClick(detailedActivity: DetailedActivity)
    }
}
