package com.mraihanfauzii.restrokotlin.adapter.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mraihanfauzii.restrokotlin.R
import com.mraihanfauzii.restrokotlin.model.ChatContact
import java.text.SimpleDateFormat
import java.util.Locale

class ChatContactAdapter(private val onItemClick: (ChatContact) -> Unit) : ListAdapter<ChatContact, ChatContactAdapter.ChatContactViewHolder>(
    ChatContactDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatContactViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_contact, parent, false)
        return ChatContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatContactViewHolder, position: Int) {
        val chatContact = getItem(position)
        holder.bind(chatContact)
    }

    inner class ChatContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTherapistName: TextView = itemView.findViewById(R.id.tvTherapistName)
        private val tvLastMessage: TextView = itemView.findViewById(R.id.tvLastMessage)
        private val tvLastMessageTime: TextView = itemView.findViewById(R.id.tvLastMessageTime)

        init {
            itemView.setOnClickListener {
                onItemClick(getItem(adapterPosition))
            }
        }

        fun bind(chatContact: ChatContact) {
            tvTherapistName.text = chatContact.therapistName
            tvLastMessage.text = chatContact.lastMessage ?: "Belum ada pesan."

            chatContact.lastMessageTimestamp?.let {
                val sdf = SimpleDateFormat("HH:mm, dd MMM", Locale.getDefault())
                tvLastMessageTime.text = sdf.format(it)
                tvLastMessageTime.visibility = View.VISIBLE
            } ?: run {
                tvLastMessageTime.visibility = View.GONE
            }
        }
    }
}

class ChatContactDiffCallback : DiffUtil.ItemCallback<ChatContact>() {
    override fun areItemsTheSame(oldItem: ChatContact, newItem: ChatContact): Boolean {
        return oldItem.therapistFirebaseUid == newItem.therapistFirebaseUid
    }

    override fun areContentsTheSame(oldItem: ChatContact, newItem: ChatContact): Boolean {
        return oldItem == newItem
    }
}