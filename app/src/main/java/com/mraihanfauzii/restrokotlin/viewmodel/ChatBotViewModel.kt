package com.mraihanfauzii.restrokotlin.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.mraihanfauzii.restrokotlin.database.ChatBotRepository
import com.mraihanfauzii.restrokotlin.model.MessageChatBot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatBotViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ChatBotRepository(application)

    val chatMessages: LiveData<List<MessageChatBot>> = repository.getChatMessages().asLiveData()

    fun sendMessage(message: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.sendMessageToChatBot(message)
        }
    }

    fun clearChat() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearChat()
        }
    }
}