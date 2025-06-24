package com.mraihanfauzii.restrokotlin.adapter.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mraihanfauzii.restrokotlin.R
import com.mraihanfauzii.restrokotlin.model.MessageChatBot
import java.text.SimpleDateFormat
import java.util.Locale

class ChatBotAdapter : ListAdapter<MessageChatBot, RecyclerView.ViewHolder>(MessageChatBotDiffCallback()) {

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
        private const val VIEW_TYPE_THINKING = 3
    }

    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)
        return if (message.isThinking) {
            VIEW_TYPE_THINKING
        } else if (message.isUser) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_SENT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_sent, parent, false)
                SentMessageViewHolder(view)
            }
            VIEW_TYPE_RECEIVED -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_received, parent, false)
                ReceivedMessageViewHolder(view)
            }
            VIEW_TYPE_THINKING -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_received, parent, false)
                ThinkingMessageViewHolder(view)
            }
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val timeString = sdf.format(message.timestamp)

        when (holder.itemViewType) {
            VIEW_TYPE_SENT -> (holder as SentMessageViewHolder).bind(message, timeString)
            VIEW_TYPE_RECEIVED -> (holder as ReceivedMessageViewHolder).bind(message, timeString)
            VIEW_TYPE_THINKING -> (holder as ThinkingMessageViewHolder).bind(message, timeString)
        }
    }

    inner class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.textViewMessageSent)
        private val messageTime: TextView = itemView.findViewById(R.id.textViewMessageTimeSent)

        fun bind(message: MessageChatBot, time: String) {
            messageText.text = message.text
            messageTime.text = time
        }
    }

    inner class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.textViewMessageReceived)
        private val senderNameText: TextView? = itemView.findViewById(R.id.textViewSenderName)
        private val messageTime: TextView = itemView.findViewById(R.id.textViewMessageTimeReceived)

        fun bind(message: MessageChatBot, time: String) {
            messageText.text = message.text
            senderNameText?.text = "Gemini Bot"
            messageTime.text = time
        }
    }

    inner class ThinkingMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.textViewMessageReceived)
        private val senderNameText: TextView? = itemView.findViewById(R.id.textViewSenderName)
        private val messageTime: TextView = itemView.findViewById(R.id.textViewMessageTimeReceived)

        fun bind(message: MessageChatBot, time: String) {
            messageText.text = message.text
            senderNameText?.text = "Gemini Bot"
            messageTime.text = time
        }
    }
}

class MessageChatBotDiffCallback : DiffUtil.ItemCallback<MessageChatBot>() {
    override fun areItemsTheSame(oldItem: MessageChatBot, newItem: MessageChatBot): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: MessageChatBot, newItem: MessageChatBot): Boolean {
        return oldItem == newItem
    }
}