package com.mraihanfauzii.restrokotlin.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class RegisterRequest(
    val username: String,
    @field:SerializedName("nama_lengkap")
    val namaLengkap: String,
    val email: String,
    val password: String,
) : Parcelable

@Parcelize
data class RegisterResponse(
    @field:SerializedName("msg")
    val msg: String,
    @field:SerializedName("user")
    val user: User
) : Parcelable

data class LoginRequest(
    val identifier: String,
    val password: String,
)

@Parcelize
data class LoginResponse(
    @field:SerializedName("access_token")
    val accessToken: String,
    @field:SerializedName("firebase_client_config")
    val firebaseClientConfig: FirebaseClientConfig,
    @field:SerializedName("firebase_custom_token")
    val firebaseCustomToken: String,
    @field:SerializedName("user")
    val user: User
) : Parcelable

@Parcelize
data class FirebaseClientConfig(
    @field:SerializedName("apiKey")
    val apiKey: String,
    @field:SerializedName("appId")
    val appId: String,
    @field:SerializedName("authDomain")
    val authDomain: String,
    @field:SerializedName("measurementId")
    val measurementId: String,
    @field:SerializedName("messagingSenderId")
    val messagingSenderId: String,
    @field:SerializedName("projectId")
    val projectId: String,
    @field:SerializedName("storageBucket")
    val storageBucket: String
) : Parcelable


@Parcelize
data class User(
    @field:SerializedName("email")
    val email: String,
    @field:SerializedName("id")
    val id: Int,
    @field:SerializedName("nama_lengkap")
    val namaLengkap: String,
    @field:SerializedName("role")
    val role: String,
    @field:SerializedName("username")
    val username: String,
    @field:SerializedName("total_points")
    val totalPoints: Int = 0
) : Parcelable