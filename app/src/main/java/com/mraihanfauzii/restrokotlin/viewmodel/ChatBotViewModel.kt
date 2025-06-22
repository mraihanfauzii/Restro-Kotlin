package com.mraihanfauzii.restrokotlin.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mraihanfauzii.restrokotlin.api.DirectLineApi
import com.mraihanfauzii.restrokotlin.model.Activity
import com.mraihanfauzii.restrokotlin.model.From
import com.mraihanfauzii.restrokotlin.model.MessageChatBot
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ChatBotViewModel : ViewModel() {

    private val TAG = "ChatViewModel"

    private val directLineSecret = "Bearer AuJ2V2neRiZkYDfa0pQDx6PZGWtGsRdkz1YwHDpSz04aV74GdkO5JQQJ99BFAC77bzfAArohAAABAZBS3kVa.9IYXTw94443dn0plEfrOp0SVafyKJmkC4irGdTHGOMyTAXkVDZ03JQQJ99BFAC77bzfAArohAAABAZBS11Xk"

    private val directLineBaseUrl = "https://directline.botframework.com/"

    private val _messages = MutableLiveData<MutableList<MessageChatBot>>(mutableListOf())
    val messages: LiveData<MutableList<MessageChatBot>> = _messages

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    private lateinit var directLineApi: DirectLineApi
    private var conversationId: String? = null
    private var conversationToken: String? = null
    private var watermark: String? = null
    private val userId = "user_id_android" // ID unik untuk pengguna aplikasi Anda

    init {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // Untuk melihat detail request/response di Logcat
        }

        val httpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(directLineBaseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build()

        directLineApi = retrofit.create(DirectLineApi::class.java)

        startNewConversation()
    }

    fun startNewConversation() {
        _isLoading.value = true
        _errorMessage.value = null // Reset error saat memulai percakapan baru
        viewModelScope.launch {
            try {
                val response = directLineApi.generateToken(directLineSecret)
                if (response.isSuccessful) {
                    val tokenResponse = response.body()
                    if (tokenResponse != null) {
                        conversationId = tokenResponse.conversationId
                        conversationToken = tokenResponse.token
                        Log.d(TAG, "New conversation started. ID: $conversationId, Token: $conversationToken")
                        _messages.value = mutableListOf() // Reset pesan untuk percakapan baru
                        pollForActivities() // Mulai polling setelah token didapatkan
                    } else {
                        _errorMessage.value = "Gagal memulai percakapan: Respon token kosong."
                        Log.e(TAG, "Gagal memulai percakapan: Respon token kosong.")
                    }
                } else {
                    _errorMessage.value = "Gagal memulai percakapan: ${response.code()} ${response.errorBody()?.string()}"
                    Log.e(TAG, "Gagal memulai percakapan: ${response.code()} ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error koneksi: ${e.message}"
                Log.e(TAG, "Error koneksi saat generate token: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val userMessage = MessageChatBot(text, true)
        val currentMessages = _messages.value ?: mutableListOf()
        currentMessages.add(userMessage)
        _messages.value = currentMessages // Trigger LiveData update

        if (conversationId == null || conversationToken == null) {
            _errorMessage.value = "Percakapan belum dimulai atau token tidak ada."
            Log.e(TAG, "Percakapan belum dimulai atau token tidak ada saat mencoba kirim pesan.")
            return
        }

        viewModelScope.launch {
            try {
                val activity = Activity(
                    type = "message",
                    from = From(id = userId),
                    text = text
                )
                val response = directLineApi.sendMessage(conversationId!!, "Bearer $conversationToken", activity)
                if (!response.isSuccessful) {
                    _errorMessage.value = "Gagal mengirim pesan: ${response.code()} ${response.errorBody()?.string()}"
                    Log.e(TAG, "Gagal mengirim pesan: ${response.code()} ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error koneksi saat mengirim pesan: ${e.message}"
                Log.e(TAG, "Error koneksi saat mengirim pesan: ${e.message}", e)
            }
        }
    }

    private fun pollForActivities() {
        viewModelScope.launch {
            while (true) {
                if (conversationId != null && conversationToken != null) {
                    try {
                        val response = directLineApi.getActivities(conversationId!!, "Bearer $conversationToken", watermark)
                        if (response.isSuccessful) {
                            val activitiesResponse = response.body()
                            activitiesResponse?.let {
                                val currentMessages = _messages.value ?: mutableListOf()
                                // Filter aktivitas yang berasal dari bot dan bukan dari user ini
                                it.activities.forEach { activity ->
                                    // Pastikan untuk tidak menambahkan pesan yang sudah ada
                                    if (activity.type == "message" && activity.from.id != userId) {
                                        // Hindari duplikasi pesan jika ada
                                        val newMessage = MessageChatBot(activity.text, false)
                                        if (!currentMessages.contains(newMessage)) { // Cek duplikasi sederhana
                                            currentMessages.add(newMessage)
                                        }
                                    }
                                }
                                _messages.value = currentMessages // Update LiveData
                                watermark = it.watermark
                            }
                        } else {
                            Log.e(TAG, "Gagal mengambil aktivitas: ${response.code()} ${response.errorBody()?.string()}")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error koneksi saat polling aktivitas: ${e.message}", e)
                    }
                }
                delay(3000) // Poll setiap 3 detik
            }
        }
    }

    // Fungsi baru untuk membersihkan pesan error dari ViewModel
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}