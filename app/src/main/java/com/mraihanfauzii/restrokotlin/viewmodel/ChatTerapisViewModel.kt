package com.mraihanfauzii.restrokotlin.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.mraihanfauzii.restrokotlin.model.MessageTerapis
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.mraihanfauzii.restrokotlin.ui.authentication.AuthenticationManager
import kotlinx.coroutines.launch
import java.util.Date

class ChatTerapisViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FirebaseFirestore.getInstance()
    private var chatRoomListener: ListenerRegistration? = null

    private val _messages = MutableLiveData<List<MessageTerapis>>()
    val messages: LiveData<List<MessageTerapis>> = _messages

    private val _sendMessageStatus = MutableLiveData<Boolean>()
    val sendMessageStatus: LiveData<Boolean> = _sendMessageStatus

    // IDs of the current patient and therapist for the chat session
    var currentUserId: String? = null
        private set
    var therapistId: String? = null
        private set
    var patientName: String? = null
        private set

    private val APP_ID = "app" // Ganti dengan ID aplikasi Flask Anda, misal: "ELEVAITE" atau "FlaskApp"

    fun setChatParticipants(patientFirebaseUid: String, therapistFirebaseUid: String, patientAzureName: String) {
        if (currentUserId == patientFirebaseUid && therapistId == therapistFirebaseUid) {
            Log.d("ChatTerapisViewModel", "Chat participants already set and are the same.")
            return
        }

        this.currentUserId = patientFirebaseUid
        this.therapistId = therapistFirebaseUid
        this.patientName = patientAzureName

        // Stop previous listener if exists
        chatRoomListener?.remove()
        chatRoomListener = null

        // Start new listener for the new chat room
        listenForMessages()
    }

    private fun listenForMessages() {
        if (currentUserId == null || therapistId == null) {
            Log.e("ChatTerapisViewModel", "Cannot listen for messages: currentUserId or therapistId is null.")
            return
        }

        // Construct chat room ID: ensure consistent order (e.g., therapist_id_patient_id)
        // This must match the backend's chat room ID generation and Firestore rules
        val chatRoomId = "chat_terapis_pasien_${therapistId}_${currentUserId}"
        Log.d("ChatTerapisViewModel", "Listening for messages in chat room: $chatRoomId")

        chatRoomListener = db.collection("artifacts")
            .document(APP_ID)
            .collection("public")
            .document("data")
            .collection("chat_rooms")
            .document(chatRoomId)
            .collection("messages")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("ChatTerapisViewModel", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshots != null && !snapshots.isEmpty) {
                    val messagesList = snapshots.documents.mapNotNull { it.toObject(MessageTerapis::class.java) }
                    _messages.value = messagesList
                    Log.d("ChatTerapisViewModel", "Received ${messagesList.size} messages.")
                } else {
                    _messages.value = emptyList()
                    Log.d("ChatTerapisViewModel", "No messages in chat room yet.")
                }
            }
    }

    fun sendMessage(text: String) {
        if (currentUserId == null || therapistId == null || patientName == null) {
            Log.e("ChatTerapisViewModel", "Cannot send message: currentUserId, therapistId, or patientName is null.")
            _sendMessageStatus.value = false
            return
        }

        val message = MessageTerapis(
            senderId = currentUserId!!,
            senderName = patientName!!, // Use patient's name from Azure DB
            text = text,
            timestamp = Timestamp(Date()),
            type = "patient_message" // Atau "terapis_message" dari sisi terapis
        )

        val chatRoomId = "chat_terapis_pasien_${therapistId}_${currentUserId}"

        db.collection("artifacts")
            .document(APP_ID)
            .collection("public")
            .document("data")
            .collection("chat_rooms")
            .document(chatRoomId)
            .collection("messages")
            .add(message)
            .addOnSuccessListener {
                Log.d("ChatTerapisViewModel", "Pesan berhasil dikirim!")
                _sendMessageStatus.value = true
            }
            .addOnFailureListener { e ->
                Log.e("ChatTerapisViewModel", "Gagal mengirim pesan", e)
                _sendMessageStatus.value = false
            }
    }

    // Fungsi untuk mendapatkan UID Firebase pengguna saat ini
    fun getFirebaseCurrentUserUid(): String? {
        val auth = FirebaseAuth.getInstance()
        return auth.currentUser?.uid
    }

    override fun onCleared() {
        super.onCleared()
        chatRoomListener?.remove()
        Log.d("ChatTerapisViewModel", "Chat listener removed.")
    }
}