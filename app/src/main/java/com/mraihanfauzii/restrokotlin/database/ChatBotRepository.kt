package com.mraihanfauzii.restrokotlin.database

import android.content.Context
import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content
import com.mraihanfauzii.restrokotlin.model.MessageChatBot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.io.IOException

class ChatBotRepository(context: Context) {

    private val chatBotDao = AppDatabase.getDatabase(context).chatBotDao()

    // Ganti dengan API Key Gemini Anda yang sebenarnya
    private val geminiApiKey = "AIzaSyCVInB0wup8XijzWbAgNcQrJ9iYaUeEYAY"
    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = geminiApiKey
    )
    private var chat = generativeModel.startChat()

    private suspend fun loadChatHistoryFromDb() {
        val messages = chatBotDao.getAllMessages().firstOrNull() ?: emptyList()
        val history = messages
            .filter { !it.isThinking }
            .map {
                if (it.isUser) {
                    Content(role = "user", parts = listOf(com.google.ai.client.generativeai.type.TextPart(it.text)))
                } else {
                    Content(role = "model", parts = listOf(com.google.ai.client.generativeai.type.TextPart(it.text)))
                }
            }
        chat = generativeModel.startChat(history) // Mulai chat dengan riwayat yang dimuat
        Log.d("ChatBotRepository", "Chat history loaded from DB. Size: ${history.size}")
    }

    suspend fun sendMessageToChatBot(userMessage: String) {
        if (chat.history.isEmpty()) {
            loadChatHistoryFromDb()
        }

        val userMessageModel = MessageChatBot(text = userMessage, isUser = true, isThinking = false)
        chatBotDao.insertMessage(userMessageModel)

        val thinkingMessage = MessageChatBot(text = "Sedang berpikir...", isUser = false, isThinking = true)
        chatBotDao.insertMessage(thinkingMessage)

        try {
            val currentMessages = chatBotDao.getAllMessages().firstOrNull() // Ambil list terbaru
            val thinkingMessageId = currentMessages?.find { it.isThinking && it.text == "Sedang berpikir..." }?.id

            val response = chat.sendMessage(userMessage)

            if (thinkingMessageId != null) {
                chatBotDao.updateMessage(thinkingMessage.copy(id = thinkingMessageId, text = "TEMPORARY_DELETION_MARKER", isThinking = false))

                val botResponseText = response.text ?: "Maaf, terjadi kesalahan."
                val updatedThinkingMessage = thinkingMessage.copy(
                    id = thinkingMessageId,
                    text = botResponseText,
                    isUser = false,
                    isThinking = false,
                    timestamp = System.currentTimeMillis()
                )
                chatBotDao.updateMessage(updatedThinkingMessage)

                Log.d("ChatBotRepository", "Bot response received: $botResponseText")
            } else {
                Log.w("ChatBotRepository", "Thinking message not found to update. Inserting new bot response.")
                val botResponseText = response.text ?: "Maaf, terjadi kesalahan."
                val botMessage = MessageChatBot(text = botResponseText, isUser = false, isThinking = false)
                chatBotDao.insertMessage(botMessage)
            }


        } catch (e: Exception) {
            Log.e("ChatBotRepository", "Error sending message to Gemini: ${e.message}", e)
            val currentMessages = chatBotDao.getAllMessages().firstOrNull()
            val thinkingMessageId = currentMessages?.find { it.isThinking }?.id
            if (thinkingMessageId != null) {
                val errorMessage = MessageChatBot(
                    id = thinkingMessageId,
                    text = "Gagal mendapatkan jawaban dari bot. (${e.message ?: "Unknown error"})",
                    isUser = false,
                    isThinking = false,
                    timestamp = System.currentTimeMillis()
                )
                chatBotDao.updateMessage(errorMessage)
            } else {
                chatBotDao.insertMessage(MessageChatBot(text = "Gagal mendapatkan jawaban dari bot. (${e.message ?: "Unknown error"})", isUser = false))
            }

            if (e is IOException) {
                Log.e("ChatBotRepository", "Network error: ${e.message}")
            }
        }
    }

    fun getChatMessages(): Flow<List<MessageChatBot>> {
        return chatBotDao.getAllMessages()
    }

    suspend fun clearChat() {
        chatBotDao.deleteAllMessages()
        chat = generativeModel.startChat()
        Log.d("ChatBotRepository", "Chat history cleared.")
    }
}