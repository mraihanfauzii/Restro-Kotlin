package com.mraihanfauzii.restrokotlin.adapter.chat

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mraihanfauzii.restrokotlin.R
import com.mraihanfauzii.restrokotlin.model.MessageTerapis
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatTerapisAdapter(private val currentUserId: String?) : ListAdapter<MessageTerapis, RecyclerView.ViewHolder>(
    MessageDiffCallback()
) {

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
        private const val TAG = "ChatTerapisAdapter"
    }

    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)
        return if (message.senderId == currentUserId) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENT) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_message_sent, parent, false)
            SentMessageViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_message_received, parent, false)
            ReceivedMessageViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        val date: Date? = message.timestamp?.toDate()

        val timeString = if (date != null) {
            try {
                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                sdf.format(date)
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, "Error formatting date for message ID: ${message.id}, timestamp: ${message.timestamp}", e)
                "Invalid Time"
            }
        } else {
            ""
        }


        when (holder.itemViewType) {
            VIEW_TYPE_SENT -> (holder as SentMessageViewHolder).bind(message, timeString)
            VIEW_TYPE_RECEIVED -> (holder as ReceivedMessageViewHolder).bind(message, timeString)
        }
    }

    inner class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.textViewMessageSent)
        private val messageTime: TextView = itemView.findViewById(R.id.textViewMessageTimeSent)

        fun bind(message: MessageTerapis, time: String) {
            messageText.text = message.text
            messageTime.text = time
        }
    }

    inner class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.textViewMessageReceived)
        private val senderNameText: TextView? = itemView.findViewById(R.id.textViewSenderName)
        private val messageTime: TextView = itemView.findViewById(R.id.textViewMessageTimeReceived)

        fun bind(message: MessageTerapis, time: String) {
            messageText.text = message.text
            senderNameText?.text = message.senderName.ifEmpty { "Terapis" } // Gunakan ifEmpty untuk default
            messageTime.text = time
        }
    }
}

class MessageDiffCallback : DiffUtil.ItemCallback<MessageTerapis>() {
    override fun areItemsTheSame(oldItem: MessageTerapis, newItem: MessageTerapis): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: MessageTerapis, newItem: MessageTerapis): Boolean {
        return oldItem == newItem
    }
}