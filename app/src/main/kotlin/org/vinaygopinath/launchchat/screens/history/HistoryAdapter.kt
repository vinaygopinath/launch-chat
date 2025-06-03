package org.vinaygopinath.launchchat.screens.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.RecyclerView.NO_POSITION
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.android.material.textview.MaterialTextView
import org.vinaygopinath.launchchat.R
import org.vinaygopinath.launchchat.helpers.DetailedActivityHelper
import org.vinaygopinath.launchchat.models.DetailedActivity

class HistoryAdapter(
    private val helper: DetailedActivityHelper,
    private val listener: HistoryClickListener
) : PagingDataAdapter<DetailedActivity, HistoryAdapter.HistoryViewHolder>(
    DetailedActivityDiffCallback()
) {
    private val selectedItems = mutableSetOf<DetailedActivity>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.list_item_history,
            parent,
            false
        )
        val viewHolder = HistoryViewHolder(view)
        view.setOnClickListener {
            val position = viewHolder.bindingAdapterPosition
            if (position != NO_POSITION) {
                getItem(position)?.let { listener.onClick(it) }
            }
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        if (position == NO_POSITION) {
            return
        }

        getItem(position)?.let { detailedActivity ->
            holder.itemView.isSelected = selectedItems.contains(detailedActivity)
            holder.titleText.setText(helper.getSourceDisplayName(detailedActivity))
            holder.timestampText.text = helper.getActivityShortTimestamp(detailedActivity)
            holder.contentText.text = helper.getActivityContent(detailedActivity)
            holder.actionsText.text = helper.getActionsText(detailedActivity)
        }
    }

    fun toggleSelection(item: DetailedActivity) {
        if (selectedItems.contains(item)) {
            selectedItems.remove(item)
        } else {
            selectedItems.add(item)
        }
        notifyDataSetChanged()
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
    }

    fun clearSelection() {
        selectedItems.clear()
        notifyDataSetChanged()
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

    interface HistoryClickListener {
        fun onClick(detailedActivity: DetailedActivity)
        fun onLongClick(detailedActivity: DetailedActivity): Boolean
    }
}