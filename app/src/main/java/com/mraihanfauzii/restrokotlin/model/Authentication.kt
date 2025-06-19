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
    @field:SerializedName("user")
    val user: User
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
    val username: String
) : Parcelable