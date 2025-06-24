package com.mraihanfauzii.restrokotlin.model

import com.google.gson.annotations.SerializedName

data class ProfilePictureUploadResponse(
    @SerializedName("msg")
    val msg: String,
    @SerializedName("url_foto_profil")
    val urlFotoProfil: String?
)