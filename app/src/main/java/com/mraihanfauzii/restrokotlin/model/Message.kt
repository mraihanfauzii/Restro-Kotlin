package com.mraihanfauzii.restrokotlin.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.util.Date

@Entity(tableName = "chatbot_messages")
data class MessageChatBot(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0, // Primary key untuk Room
    val text: String,
    val isUser: Boolean, // True jika pesan dari user, False jika dari bot
    val timestamp: Long = System.currentTimeMillis(),
    val isThinking: Boolean = false // Tambahan untuk UI loading state
)

@Parcelize
data class ChatContact(
    val therapistFirebaseUid: String,
    val therapistName: String,
    val lastMessage: String?,
    val lastMessageTimestamp: Date?
) : Parcelable

data class MessageTerapis(
    var id: String = "",
    var senderId: String = "",
    var senderName: String = "",
    var text: String = "",
    @ServerTimestamp
    var timestamp: Timestamp? = null,
    var type: String = "text"
) {
    constructor() : this("", "", "", "", null, "")
}

// Data classes untuk Direct Line API (sesuaikan jika ada perubahan API di masa mendatang)
// Response untuk mendapatkan token Direct Line
data class DirectLineTokenResponse(
    @SerializedName("token") val token: String,
    @SerializedName("conversationId") val conversationId: String,
    @SerializedName("expires_in") val expiresIn: Int,
    @SerializedName("streamUrl") val streamUrl: String? // URL untuk WebSockets (opsional)
)

// Request untuk mengirim aktivitas (pesan) ke bot
data class Activity(
    @SerializedName("type") val type: String,
    @SerializedName("from") val from: From,
    @SerializedName("text") val text: String,
    @SerializedName("locale") val locale: String = "id-ID"
)

data class From(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String? = null
)

// Response untuk menerima aktivitas (pesan) dari bot
data class ActivitiesResponse(
    @SerializedName("activities") val activities: List<Activity>,
    @SerializedName("watermark") val watermark: String?
)