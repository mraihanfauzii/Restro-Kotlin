package com.mraihanfauzii.restrokotlin.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.mraihanfauzii.restrokotlin.api.ApiConfig
import com.mraihanfauzii.restrokotlin.model.ChatContact
import com.mraihanfauzii.restrokotlin.model.TherapistDetail
import com.mraihanfauzii.restrokotlin.ui.authentication.AuthenticationManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date
import retrofit2.HttpException

class ChatListViewModel(application: Application) : AndroidViewModel(application) {

    private val _chatContacts = MutableLiveData<List<ChatContact>>()
    val chatContacts: LiveData<List<ChatContact>> = _chatContacts

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val authManager = AuthenticationManager(application.applicationContext)
    private val db = FirebaseFirestore.getInstance()

    private val APP_ID = "app"

    fun loadChatContacts() {
        val patientFirebaseUid = authManager.getAccess(AuthenticationManager.FIREBASE_UID)
        val authToken = authManager.getAccess(AuthenticationManager.TOKEN)

        if (patientFirebaseUid.isNullOrEmpty() || authToken.isNullOrEmpty()) {
            _errorMessage.value = "Data pengguna tidak lengkap. Harap login ulang."
            _isLoading.value = false
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val fullAuthToken = "Bearer $authToken"
                val programHistoryResponse = ApiConfig.getApiService().getProgramHistory(fullAuthToken)
                val uniqueTherapists = mutableMapOf<String, TherapistDetail>() // Key: Firebase UID (yang sama dengan ID Backend)

                programHistoryResponse.programs.forEach { programItem ->
                    programItem.terapis.let { therapist ->
                        // Asumsi: therapist.id dari backend adalah sama dengan Firebase UID-nya.
                        val therapistBackendId = therapist.id.toString() // Convert Int ID to String for Firebase UID

                        if (!therapistBackendId.isNullOrEmpty() && !uniqueTherapists.containsKey(therapistBackendId)) {
                            // Tambahkan terapis ke daftar unik menggunakan ID backend sebagai kunci Firebase UID
                            uniqueTherapists[therapistBackendId] = therapist
                        } else if (therapistBackendId.isNullOrEmpty()) {
                            Log.w("ChatListViewModel", "Terapis '${therapist.fullName}' (ID: ${therapist.id}) memiliki ID backend kosong. Tidak akan dimasukkan ke daftar chat.")
                        }
                    }
                }

                val chatContactList = mutableListOf<ChatContact>()
                for ((therapistFirebaseUid, therapist) in uniqueTherapists) {
                    // Cek kedua kemungkinan chatRoomId sesuai aturan Firestore:
                    // 1. Terapis sebagai sender: chat_terapis_pasien_{therapistFirebaseUid}_{patientFirebaseUid}
                    // 2. Pasien sebagai sender (jika pasien memulai chat): chat_terapis_pasien_{patientFirebaseUid}_{therapistFirebaseUid}

                    // Prioritaskan chat room di mana terapis menjadi sender pertama, lalu chat room di mana pasien menjadi sender pertama.
                    // Ini penting agar konsisten dalam mencari chat history.
                    val chatRoomIdOption1 = "chat_terapis_pasien_${therapistFirebaseUid}_${patientFirebaseUid}"
                    val chatRoomIdOption2 = "chat_terapis_pasien_${patientFirebaseUid}_${therapistFirebaseUid}"

                    var lastMessage: String? = null
                    var lastMessageTimestamp: Date? = null
                    var actualChatRoomIdUsed: String? = null

                    // Coba cek chatRoomIdOption1 terlebih dahulu
                    try {
                        val latestMessageDoc1 = db.collection("artifacts")
                            .document(APP_ID)
                            .collection("public")
                            .document("data")
                            .collection("chat_rooms")
                            .document(chatRoomIdOption1)
                            .collection("messages")
                            .orderBy("timestamp", Query.Direction.DESCENDING)
                            .limit(1)
                            .get()
                            .await()

                        if (!latestMessageDoc1.isEmpty) {
                            val msg = latestMessageDoc1.documents[0].toObject(com.mraihanfauzii.restrokotlin.model.MessageTerapis::class.java)
                            lastMessage = msg?.text
                            lastMessageTimestamp = msg?.timestamp?.toDate()
                            actualChatRoomIdUsed = chatRoomIdOption1
                        }
                    } catch (e: Exception) {
                        Log.e("ChatListViewModel", "Error fetching last message for $chatRoomIdOption1: ${e.message}")
                    }

                    // Jika chatRoomIdOption1 tidak memiliki pesan, coba chatRoomIdOption2
                    if (lastMessage == null) {
                        try {
                            val latestMessageDoc2 = db.collection("artifacts")
                                .document(APP_ID)
                                .collection("public")
                                .document("data")
                                .collection("chat_rooms")
                                .document(chatRoomIdOption2)
                                .collection("messages")
                                .orderBy("timestamp", Query.Direction.DESCENDING)
                                .limit(1)
                                .get()
                                .await()

                            if (!latestMessageDoc2.isEmpty) {
                                val msg = latestMessageDoc2.documents[0].toObject(com.mraihanfauzii.restrokotlin.model.MessageTerapis::class.java)
                                lastMessage = msg?.text
                                lastMessageTimestamp = msg?.timestamp?.toDate()
                                actualChatRoomIdUsed = chatRoomIdOption2
                            }
                        } catch (e: Exception) {
                            Log.e("ChatListViewModel", "Error fetching last message for $chatRoomIdOption2: ${e.message}")
                        }
                    }

                    chatContactList.add(
                        ChatContact(
                            therapistFirebaseUid = therapistFirebaseUid, // Ini akan menjadi ID terapis dari backend, yang diasumsikan sama dengan Firebase UID
                            therapistName = therapist.fullName ?: "Terapis Tanpa Nama",
                            lastMessage = lastMessage,
                            lastMessageTimestamp = lastMessageTimestamp
                        )
                    )
                    Log.d("ChatListViewModel", "Added chat contact: ${therapist.fullName} (Firebase UID: $therapistFirebaseUid), Last Message: $lastMessage, Used Chat Room: $actualChatRoomIdUsed")
                }
                _chatContacts.value = chatContactList.sortedWith(compareByDescending { it.lastMessageTimestamp ?: Date(0) }) // Date(0) for null timestamps to put them at the end

                Log.d("ChatListViewModel", "Loaded ${chatContactList.size} unique chat contacts.")

            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                _errorMessage.value = "Gagal memuat daftar chat: ${e.code()} - ${e.message()} - $errorBody"
                Log.e("ChatListViewModel", "HTTP Error loading chat contacts: ${e.code()} - ${e.message()}", e)
            } catch (e: Exception) {
                _errorMessage.value = "Gagal memuat daftar chat: ${e.localizedMessage}"
                Log.e("ChatListViewModel", "General Error loading chat contacts", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}