package com.mraihanfauzii.restrokotlin.model

import com.google.gson.annotations.SerializedName

// Model untuk respons GET /api/patient/profile
data class PatientProfileResponse(
    @field:SerializedName("id")
    val id: Int,

    @field:SerializedName("username")
    val username: String?,

    @field:SerializedName("email")
    val email: String?,

    @field:SerializedName("nomor_telepon")
    val phoneNumber: String?,

    @field:SerializedName("nama_lengkap")
    val fullName: String?,

    @field:SerializedName("jenis_kelamin")
    val gender: String?,

    @field:SerializedName("tanggal_lahir")
    val dateOfBirth: String?,

    @field:SerializedName("tempat_lahir")
    val placeOfBirth: String?,

    @field:SerializedName("alamat")
    val address: String?,

    @field:SerializedName("nama_pendamping")
    val companionName: String?,

    @field:SerializedName("tinggi_badan")
    val height: Int?,

    @field:SerializedName("berat_badan")
    val weight: Double?,

    @field:SerializedName("golongan_darah")
    val bloodType: String?,

    @field:SerializedName("riwayat_medis")
    val medicalHistory: String?,

    @field:SerializedName("riwayat_alergi")
    val allergyHistory: String?,

    @field:SerializedName("url_foto_profil")
    val profilePictureUrl: String?,

    @field:SerializedName("deskripsi_profil")
    val profileDescription: String?
)

// Model untuk request body PUT/PATCH (Informasi Akun)
data class AccountInfoRequest(
    @field:SerializedName("username")
    val username: String?,
    @field:SerializedName("email")
    val email: String?,
    @field:SerializedName("nomor_telepon")
    val phoneNumber: String?
)

// Model untuk request body PUT/PATCH (Informasi Pasien)
data class PatientInfoRequest(
    @field:SerializedName("nama_lengkap")
    val fullName: String?,
    @field:SerializedName("jenis_kelamin")
    val gender: String?,
    @field:SerializedName("tanggal_lahir")
    val dateOfBirth: String?,
    @field:SerializedName("tempat_lahir")
    val placeOfBirth: String?,
    @field:SerializedName("alamat")
    val address: String?,
    @field:SerializedName("nama_pendamping")
    val companionName: String?
)

// Model untuk request body PUT/PATCH (Informasi Kesehatan)
data class HealthInfoRequest(
    @field:SerializedName("tinggi_badan")
    val height: Int?,
    @field:SerializedName("berat_badan")
    val weight: Double?,
    @field:SerializedName("golongan_darah")
    val bloodType: String?,
    @field:SerializedName("riwayat_medis")
    val medicalHistory: String?,
    @field:SerializedName("riwayat_alergi")
    val allergyHistory: String?
)

// Contoh respons umum untuk operasi PUT/PATCH yang berhasil
data class GeneralResponse(
    @field:SerializedName("message")
    val message: String
)