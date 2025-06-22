package com.mraihanfauzii.restrokotlin.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class CalendarProgramResponse(
    @SerializedName("id")
    val id: Int?,
    @SerializedName("nama_program")
    val programName: String?,
    @SerializedName("tanggal_program")
    val programDate: String?, // yyyy-MM-DD
    @SerializedName("catatan_terapis")
    val therapistNotes: String?,
    @SerializedName("status")
    val status: String?, // e.g., "belum_dimulai", "berjalan", "selesai", "dibatalkan"
    @SerializedName("terapis")
    val therapist: TherapistDetail?,
    @SerializedName("list_gerakan_direncanakan")
    val plannedMovements: List<MovementDetail>?
) : Parcelable

@Parcelize
data class TherapistDetail(
    @SerializedName("id") val id: Int?,
    @SerializedName("username") val username: String?,
    @SerializedName("nama_lengkap") val fullName: String?, // Ini yang kita butuhkan
    @SerializedName("email") val email: String?,
    @SerializedName("role") val role: String?
) : Parcelable

@Parcelize
data class MovementDetail(
    @SerializedName("id") val id: Int?,
    @SerializedName("nama_gerakan") val movementName: String?,
    @SerializedName("deskripsi") val description: String?,
    @SerializedName("durasi_detik") val durationSeconds: Int?
) : Parcelable

@Parcelize
data class CalendarProgramsListResponse(
    @SerializedName("msg")
    val msg: String?,
    @SerializedName("programs")
    val programs: List<CalendarProgramResponse>?
) : Parcelable

data class SingleProgramDetailResponse(
    @SerializedName("msg")
    val msg: String?,
    @SerializedName("program")
    val program: CalendarProgramResponse?
)

data class UpdateStatusProgramResponse(
    @SerializedName("msg")
    val msg: String?,
    @SerializedName("program")
    val program: CalendarProgramResponse?
)