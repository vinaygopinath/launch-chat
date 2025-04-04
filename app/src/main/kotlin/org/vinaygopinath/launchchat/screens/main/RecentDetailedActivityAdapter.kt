package org.vinaygopinath.launchchat.screens.main

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.NO_POSITION
import com.google.android.material.textview.MaterialTextView
import org.vinaygopinath.launchchat.R
import org.vinaygopinath.launchchat.helpers.DetailedActivityHelper
import org.vinaygopinath.launchchat.models.DetailedActivity

class RecentDetailedActivityAdapter(
    private val helper: DetailedActivityHelper,
    private val listener: RecentHistoryClickListener
) :
    RecyclerView.Adapter<RecentDetailedActivityAdapter.RecentDetailedActivityViewHolder>() {

    private val dataSet = mutableListOf<DetailedActivity>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecentDetailedActivityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_recent_detailed_activity, parent, false)

        return RecentDetailedActivityViewHolder(view, listener)
    }

    override fun getItemCount() = dataSet.size

    override fun onBindViewHolder(holder: RecentDetailedActivityViewHolder, position: Int) {
        if (position == NO_POSITION) {
            return
        }

        val detailedActivity = dataSet[position]
        holder.titleText.setText(helper.getSourceDisplayName(detailedActivity))
        holder.timestampText.text = helper.getActivityShortTimestamp(detailedActivity)
        holder.contentText.text = helper.getActivityContent(detailedActivity)
        with(holder.firstActionText) {
            isVisible = helper.isFirstActionVisible(detailedActivity)
            text = helper.getFirstActionText(detailedActivity)
        }
        with(holder.secondActionText) {
            isVisible = helper.isSecondActionVisible(detailedActivity)
            text = helper.getSecondActionText(detailedActivity)
        }
        with(holder.moreActionText) {
            isVisible = helper.isMoreTextVisible(detailedActivity)
            text = helper.getMoreText(detailedActivity)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(detailedActivityList: List<DetailedActivity>) {
        dataSet.clear()
        dataSet.addAll(detailedActivityList)
        notifyDataSetChanged()
    }


    inner class RecentDetailedActivityViewHolder(view: View, listener: RecentHistoryClickListener) :
        RecyclerView.ViewHolder(view) {
        val titleText: MaterialTextView =
            view.findViewById(R.id.recent_detailed_activity_list_title)
        val timestampText: MaterialTextView =
            view.findViewById(R.id.recent_detailed_activity_list_timestamp)
        val contentText: MaterialTextView =
            view.findViewById(R.id.recent_detailed_activity_list_content)
        val firstActionText: MaterialTextView =
            view.findViewById(R.id.recent_detailed_activity_list_first_action)
        val secondActionText: MaterialTextView =
            view.findViewById(R.id.recent_detailed_activity_list_second_action)
        val moreActionText: MaterialTextView =
            view.findViewById(R.id.recent_detailed_activity_list_more_label)

        init {
            itemView.setOnClickListener {
                if (bindingAdapterPosition != NO_POSITION) {
                    listener.onRecentHistoryItemClick(dataSet[bindingAdapterPosition])
                }
            }
        }
    }

    companion object {
        interface RecentHistoryClickListener {
            fun onRecentHistoryItemClick(detailedActivity: DetailedActivity)
        }
    }
}