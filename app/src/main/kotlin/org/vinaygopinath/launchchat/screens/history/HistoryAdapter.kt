package org.vinaygopinath.launchchat.screens.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    
    private val selectedItem = mutableSetOf<DetailedActivity>()


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
            val isSelected = selectedItem.contains(item)
            holder.bind(item, isSelected)
        }
    }

    fun selectItem(item: DetailedActivity) {
        selectedItem.add(item)
        notifyDataSetChanged()
        selectionListener.onSelectionChanged(selectedItem.size)
    }

    fun clearSelection() {
        selectedItem.clear()
        notifyDataSetChanged()
        selectionListener.onSelectionChanged(0)
    }

    fun getSelectedItems(): List<DetailedActivity> = selectedItem.toList()

    inner class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val actionsText: MaterialTextView = view.findViewById(R.id.history_list_actions)

        fun bind(item: DetailedActivity, selected: Boolean){
            actionsText.text = item.actions.toString()
            itemView.isSelected = selected
            itemView.setBackgroundColor(
                if (selected) 0xFFE0E0E0.toInt()
                else 0xFFFFFFFF.toInt()
            )
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