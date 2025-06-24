package com.mraihanfauzii.restrokotlin.model

import com.google.gson.annotations.SerializedName

data class Therapist(
    @SerializedName("id") val id: Int, // ID dari Azure DB, tidak digunakan langsung untuk Firebase
    @SerializedName("username") val username: String,
    @SerializedName("nama_lengkap") val namaLengkap: String,
    @SerializedName("email") val email: String,
    @SerializedName("firebase_uid") val firebaseUid: String? // Tambahkan ini jika backend Anda menyediakan UID Firebase di endpoint ini
)