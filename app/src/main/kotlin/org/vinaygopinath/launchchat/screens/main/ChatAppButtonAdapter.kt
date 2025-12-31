package org.vinaygopinath.launchchat.screens.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import org.vinaygopinath.launchchat.R
import org.vinaygopinath.launchchat.helpers.ChatAppHelper
import org.vinaygopinath.launchchat.models.ChatApp

class ChatAppButtonAdapter(
    private val chatAppHelper: ChatAppHelper,
    private val onChatAppClick: (ChatApp) -> Unit
) : ListAdapter<ChatApp, ChatAppButtonAdapter.ChatAppButtonViewHolder>(ChatAppDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatAppButtonViewHolder {
        val button = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_app_button, parent, false) as MaterialButton
        return ChatAppButtonViewHolder(button)
    }

    override fun onBindViewHolder(holder: ChatAppButtonViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ChatAppButtonViewHolder(
        private val button: MaterialButton
    ) : RecyclerView.ViewHolder(button) {

        fun bind(chatApp: ChatApp) {
            button.text = chatApp.name
            button.icon = chatAppHelper.getIconDrawable(chatApp)
            button.iconTint = null
            button.setOnClickListener {
                onChatAppClick(chatApp)
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
}
