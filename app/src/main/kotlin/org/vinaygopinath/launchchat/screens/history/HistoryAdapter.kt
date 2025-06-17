package org.vinaygopinath.launchchat.screens.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import org.vinaygopinath.launchchat.R
import org.vinaygopinath.launchchat.helpers.DetailedActivityHelper
import org.vinaygopinath.launchchat.models.DetailedActivity

class HistoryAdapter(
    private val helper: DetailedActivityHelper,
    private val listener: HistoryClickListener,
    private val selectionListener: SelectionListener
) : PagingDataAdapter<DetailedActivity, HistoryAdapter.HistoryViewHolder>(DetailedActivityDiffCallback()) {
    
    private val selectedItems = mutableSetOf<DetailedActivity>()


    interface SelectionListener {
        fun onSelectionChanged(selectedCount: Int)
        fun onItemLongPress(detailedActivity: DetailedActivity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_history, parent, false)
        val holder = HistoryViewHolder(view)

        holder.itemView.setOnLongClickListener{
            val position = holder.bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION){
                getItem(position)?.let { item ->
                    selectionListener.onItemLongPress(item)
                }
            }
            true
        }

        holder.itemView.setOnClickListener {
            val position = holder.bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                getItem(position)?.let { item ->
                    listener.onClick(item)
                }
            }
        }
        return holder
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            val isSelected = selectedItems.contains(item)
            holder.bind(item, isSelected)
        }
    }

    fun selectItem(item: DetailedActivity) {
        selectedItems.add(item)
        notifyItemChanged(selectedItems.size - 1)
        selectionListener.onSelectionChanged(selectedItems.size)
    }

    fun clearSelection() {
        selectedItems.clear()
        notifyItemChanged(0, itemCount)
        selectionListener.onSelectionChanged(0)
    }

    fun getSelectedItems(): List<DetailedActivity> = selectedItems.toList()

    inner class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val actionsText: MaterialTextView = view.findViewById(R.id.history_list_actions)

        fun bind(item: DetailedActivity, selected: Boolean){
            actionsText.text = item.actions.toString()
            itemView.isSelected = selected
            val color = if (selected)
                ContextCompat.getColor(itemView.context, R.color.history_selected)
            else
                ContextCompat.getColor(itemView.context, R.color.history_unselected)
            itemView.setBackgroundColor(color)
        }
    }

    class DetailedActivityDiffCallback : DiffUtil.ItemCallback<DetailedActivity>() {
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
    }
}