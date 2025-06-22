//package com.mraihanfauzii.restrokotlin.viewmodel
//
//import android.util.Log
//import androidx.activity.result.launch
//import androidx.compose.animation.core.copy
//import androidx.core.util.remove
//import androidx.core.util.size
//import androidx.datastore.core.Message
//import androidx.fragment.app.add
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import android.util.Log
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FieldValue
//import com.google.firebase.firestore.FirebaseFirestore
//import com.google.firebase.firestore.ListenerRegistration
//import com.google.firebase.firestore.Query
//import com.mraihanfauzii.restrokotlin.model.Message // Sesuaikan dengan package Anda
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.tasks.await
//
//class ChatTerapisViewModel:ViewModel() {
//
//    private val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
//    private val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
//
//    private val _messages = MutableLiveData<List<Message>>()
//    val messages: LiveData<List<android.os.Message>> = _messages
//
//    private val _sendMessageStatus = MutableLiveData<Boolean>()
//    val sendMessageStatus: LiveData<Boolean> = _sendMessageStatus
//
//    private var messagesListener: com.google.firebase.firestore.ListenerRegistration? = null
//
//    // Ganti dengan ID aplikasi Anda di Firebase
//    private val appId = "ELEVAITE" // atau "FlaskApp" sesuai konfigurasi Anda
//
//    // ID pengguna saat ini (pasien) dan ID terapis yang diajak chat
//    // Anda perlu mendapatkan ID ini dari sumber lain (misalnya, dari argumen Fragment atau SharedViewModel)
//    var currentUserId: String? = auth.currentUser?.uid
//    var therapistId: String? = null // Ini harus di-set dari luar ViewModel
//
//    private var chatRoomId: String? = null
//
//    fun setChatParticipants(patientId: String, therapistId: String) {
//        this.currentUserId = patientId
//        this.therapistId = therapistId
//        // Buat chatRoomId yang konsisten. Contoh: urutkan ID secara alfabetis
//        val ids = listOf(patientId, therapistId).sorted()
//        this.chatRoomId = "chat_terapis_pasien_${ids[0]}_${ids[1]}" // Sesuaikan format ini
//        Log.d("ChatViewModel", "ChatRoomID set to: ${this.chatRoomId}")
//        listenForMessages()
//    }
//
//    private fun listenForMessages() {
//        if (chatRoomId == null || currentUserId == null) {
//            Log.w("ChatViewModel", "ChatRoomId or CurrentUserId is null. Cannot listen for messages.")
//            _messages.value = emptyList() // Atau tampilkan pesan error
//            return
//        }
//
//        messagesListener?.remove() // Hapus listener sebelumnya jika ada
//
//        val messagesCollection = db.collection("artifacts")
//            .document(appId)
//            .collection("public")
//            .document("data")
//            .collection("chat_rooms")
//            .document(chatRoomId!!) // chatRoomId sudah dipastikan tidak null di sini
//            .collection("messages")
//            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING) // Urutkan pesan berdasarkan waktu
//
//        messagesListener = messagesCollection.addSnapshotListener { snapshots, e ->
//            if (e != null) {
//                Log.w("ChatViewModel", "Listen failed.", e)
//                _messages.value = emptyList() // Atau tampilkan pesan error
//                return@addSnapshotListener
//            }
//
//            val messageList = mutableListOf<Message>()
//            for (doc in snapshots!!) {
//                try {
//                    val message = doc.toObject(Message::class.java).copy(id = doc.id)
//                    messageList.add(message)
//                } catch (ex: Exception) {
//                    Log.e("ChatViewModel", "Error converting message", ex)
//                }
//            }
//            _messages.value = messageList
//            Log.d("ChatViewModel", "Messages loaded: ${messageList.size}")
//        }
//    }
//
//    fun sendMessage(text: String, receiverId: String) {
//        if (text.isBlank() || chatRoomId == null || currentUserId == null) {
//            _sendMessageStatus.value = false
//            Log.w("ChatViewModel", "Cannot send message: text is blank, chatRoomId or currentUserId is null.")
//            return
//        }
//
//        val message = Message(
//            senderId = currentUserId!!,
//            receiverId = receiverId,
//            text = text,
//            timestamp = null, // Akan diisi oleh server
//            senderName = auth.currentUser?.displayName // Opsional, bisa juga nama dari profil Anda
//        )
//
//        viewModelScope.launch {
//            try {
//                db.collection("artifacts")
//                    .document(appId)
//                    .collection("public")
//                    .document("data")
//                    .collection("chat_rooms")
//                    .document(chatRoomId!!)
//                    .collection("messages")
//                    .add(message)
//                    .await()
//                _sendMessageStatus.value = true
//                Log.d("ChatViewModel", "Message sent successfully.")
//            } catch (e: Exception) {
//                Log.e("ChatViewModel", "Error sending message", e)
//                _sendMessageStatus.value = false
//            }
//        }
//    }
//
//    override fun onCleared() {
//        super.onCleared()
//        messagesListener?.remove() // Penting untuk menghapus listener agar tidak terjadi memory leak
//    }
//}