package com.mraihanfauzii.restrokotlin.adapter.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mraihanfauzii.restrokotlin.R
import com.mraihanfauzii.restrokotlin.model.MessageChatBot

class MessageAdapter(private val messages: MutableList<MessageChatBot>) :
    RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    // Definisikan tipe tampilan untuk membedakan pesan user dan bot
    private val VIEW_TYPE_USER = 1
    private val VIEW_TYPE_BOT = 2

    // Override untuk menentukan layout mana yang akan digunakan berdasarkan pengirim pesan
    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isUser) VIEW_TYPE_USER else VIEW_TYPE_BOT
    }

    // Buat ViewHolder baru
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val layout = if (viewType == VIEW_TYPE_USER) {
            R.layout.item_message_user // Layout untuk pesan dari user
        } else {
            R.layout.item_message_bot // Layout untuk pesan dari bot
        }
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return MessageViewHolder(view)
    }

    // Ikat data ke ViewHolder
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.bind(message)
    }

    // Mengembalikan jumlah total item dalam daftar
    override fun getItemCount(): Int = messages.size

    // Fungsi untuk menambahkan pesan tunggal (opsional, bisa pakai updateMessages juga)
    fun addMessage(message: MessageChatBot) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    // Fungsi untuk memperbarui seluruh daftar pesan dan memberi tahu RecyclerView untuk menggambar ulang
    fun updateMessages(newMessages: List<MessageChatBot>) {
        messages.clear()
        messages.addAll(newMessages)
        notifyDataSetChanged() // Ini akan me-refresh seluruh daftar
    }

    // ViewHolder class untuk setiap item pesan
    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)

        fun bind(message: MessageChatBot) {
            messageTextView.text = message.text
            // Anda bisa menambahkan timestamp, nama pengirim, atau info lain jika diinginkan
            // Contoh: val timestampTextView: TextView = itemView.findViewById(R.id.timestampTextView)
            // timestampTextView.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))
        }
    }
}