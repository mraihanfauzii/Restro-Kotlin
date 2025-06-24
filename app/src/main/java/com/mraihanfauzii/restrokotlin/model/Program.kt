package com.mraihanfauzii.restrokotlin.model

import com.google.gson.annotations.SerializedName

data class ProgramHistoryResponse(
    @SerializedName("current_page") val currentPage: Int,
    @SerializedName("programs") val programs: List<ProgramHistoryItem>,
    @SerializedName("total_items") val totalItems: Int,
    @SerializedName("total_pages") val totalPages: Int
)

data class ProgramHistoryItem(
    @SerializedName("catatan_terapis") val catatanTerapis: String?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("estimated_total_duration_minutes") val estimatedTotalDurationMinutes: Int,
    @SerializedName("id") val id: Int,
    @SerializedName("laporan_terkait") val laporanTerkait: Any?, // Bisa null atau tipe lain
    @SerializedName("list_gerakan_direncanakan") val listGerakanDirencanakan: List<GerakanDirencanakanItem>,
    @SerializedName("nama_program") val namaProgram: String,
    @SerializedName("pasien") val pasien: User,
    @SerializedName("status") val status: String,
    @SerializedName("tanggal_program") val tanggalProgram: String,
    @SerializedName("terapis") val terapis: TherapistDetail, // Menggunakan TherapistDetail yang sudah ada
    @SerializedName("total_planned_movements") val totalPlannedMovements: Int,
    @SerializedName("updated_at") val updatedAt: String
)

data class GerakanDirencanakanItem(
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("created_by_terapis") val createdByTerapis: TherapistDetail,
    @SerializedName("deskripsi") val deskripsi: String,
    @SerializedName("id") val id: Int,
    @SerializedName("jumlah_repetisi_direncanakan") val jumlahRepetisiDirencanakan: Int,
    @SerializedName("nama_gerakan") val namaGerakan: String,
    @SerializedName("program_gerakan_detail_id") val programGerakanDetailId: Int,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("url_foto") val urlFoto: String,
    @SerializedName("url_model_tflite") val urlModelTflite: String?,
    @SerializedName("url_video") val urlVideo: String,
    @SerializedName("urutan_dalam_program") val urutanDalamProgram: Int
)