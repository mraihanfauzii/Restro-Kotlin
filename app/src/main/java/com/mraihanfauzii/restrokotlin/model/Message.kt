package com.mraihanfauzii.restrokotlin.model

import com.google.gson.annotations.SerializedName
import java.util.Date

data class MessageChatBot(
    val text: String,
    val isUser: Boolean, // True jika pesan dari user, False jika dari bot
    val timestamp: Long = System.currentTimeMillis()
)

data class MessageTerapis(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val text: String = "",
    val timestamp: Date? = null,
    val senderName: String? = null
) {
    // Konstruktor tanpa argumen diperlukan untuk deserialisasi Firestore
    constructor() : this("", "", "", "", null, null)
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