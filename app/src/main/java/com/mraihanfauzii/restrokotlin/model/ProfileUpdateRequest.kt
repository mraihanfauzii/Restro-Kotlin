package com.mraihanfauzii.restrokotlin.model

import com.google.gson.annotations.SerializedName

data class ProfileUpdateRequest(
    @SerializedName("alamat")
    val address: String?,
    @SerializedName("berat_badan")
    val weight: Double?,
    @SerializedName("email")
    val email: String?,
    @SerializedName("golongan_darah")
    val bloodType: String?,
    @SerializedName("jenis_kelamin")
    val gender: String?,
    @SerializedName("nama_lengkap")
    val fullName: String?,
    @SerializedName("nama_pendamping")
    val companionName: String?,
    @SerializedName("nomor_telepon")
    val phoneNumber: String?,
    @SerializedName("riwayat_alergi")
    val allergyHistory: String?,
    @SerializedName("riwayat_medis")
    val medicalHistory: String?,
    @SerializedName("tanggal_lahir")
    val dateOfBirth: String?, // Pastikan format YYYY-MM-DD
    @SerializedName("tempat_lahir")
    val placeOfBirth: String?,
    @SerializedName("tinggi_badan")
    val height: Int?,
    @SerializedName("username")
    val username: String? 
)