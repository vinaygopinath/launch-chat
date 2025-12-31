package org.vinaygopinath.launchchat.screens.chatapps

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textview.MaterialTextView
import org.vinaygopinath.launchchat.R
import org.vinaygopinath.launchchat.helpers.ChatAppHelper
import org.vinaygopinath.launchchat.models.ChatApp

class ChatAppListAdapter(
    private val chatAppHelper: ChatAppHelper,
    private val listener: ChatAppListAdapterListener
) : ListAdapter<ChatApp, ChatAppListAdapter.ChatAppViewHolder>(ChatAppDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatAppViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_chat_app, parent, false)
        return ChatAppViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatAppViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ChatAppViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val iconView: ImageView = view.findViewById(R.id.chat_app_icon)
        private val nameView: MaterialTextView = view.findViewById(R.id.chat_app_name)
        private val subtitleView: MaterialTextView = view.findViewById(R.id.chat_app_subtitle)
        private val enabledSwitch: MaterialSwitch = view.findViewById(R.id.chat_app_enabled_switch)

        fun bind(chatApp: ChatApp) {
            iconView.setImageDrawable(chatAppHelper.getIconDrawable(chatApp))
            nameView.text = chatApp.name
            subtitleView.text = getSubtitle(chatApp)

            enabledSwitch.setOnCheckedChangeListener(null)
            enabledSwitch.isChecked = chatApp.isEnabled
            enabledSwitch.setOnCheckedChangeListener { _, isChecked ->
                listener.onEnabledToggled(chatApp, isChecked)
            }

            itemView.setOnClickListener {
                listener.onClick(chatApp)
            }
        }

        private fun getSubtitle(chatApp: ChatApp): String {
            val context = itemView.context
            return if (chatApp.isPredefined) {
                context.getString(R.string.chat_app_list_predefined)
            } else {
                context.getString(R.string.chat_app_list_custom)
            }
        }
    }

    private class ChatAppDiffCallback : DiffUtil.ItemCallback<ChatApp>() {
        override fun areItemsTheSame(oldItem: ChatApp, newItem: ChatApp): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ChatApp, newItem: ChatApp): Boolean {
            return oldItem == newItem
        }
    }

    interface ChatAppListAdapterListener {
        fun onClick(chatApp: ChatApp)
        fun onEnabledToggled(chatApp: ChatApp, isEnabled: Boolean)
    }
}
