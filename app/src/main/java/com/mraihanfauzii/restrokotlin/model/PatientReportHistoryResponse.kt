package com.mraihanfauzii.restrokotlin.model

import com.google.gson.annotations.SerializedName

data class PatientReportHistoryResponse(
    @SerializedName("current_page")
    val currentPage: Int?,
    @SerializedName("laporan")
    val laporan: List<ReportItem>?,
    @SerializedName("total_items")
    val totalItems: Int?,
    @SerializedName("total_pages")
    val totalPages: Int?
)

data class ReportItem(
    @SerializedName("catatan_pasien_laporan")
    val note: String?,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("detail_hasil_gerakan")
    val details: List<DetailGerakan>?,
    @SerializedName("laporan_id")
    val reportId: Int?,
    @SerializedName("pasien_info")
    val patientInfo: PatientInfo?,
    @SerializedName("points_earned")
    val points: Int?,
    @SerializedName("program_info")
    val programInfo: ProgramInfo?,
    @SerializedName("summary_total_hitungan")
    val summary: SummaryTotalHitungan?,
    @SerializedName("tanggal_laporan_disubmit")
    val submittedAt: String?,
    @SerializedName("tanggal_program_direncanakan")
    val programPlannedDate: String?,
    @SerializedName("total_waktu_rehabilitasi_detik")
    val totalSeconds: Int?,
    @SerializedName("total_waktu_rehabilitasi_string")
    val totalTimeString: String?
)

data class DetailGerakan(
    @SerializedName("jumlah_repetisi_direncanakan")
    val jumlahRepetisiDirencanakan: String?, // Perhatikan ini String di API response Anda
    @SerializedName("jumlah_sempurna")
    val ok: Int?,
    @SerializedName("jumlah_tidak_sempurna")
    val ng: Int?,
    @SerializedName("jumlah_tidak_terdeteksi")
    val undetected: Int?,
    @SerializedName("laporan_gerakan_id")
    val laporanGerakanId: Int?,
    @SerializedName("nama_gerakan")
    val nama: String?,
    @SerializedName("waktu_aktual_per_gerakan_detik")
    val durasi: Int?
)

data class PatientInfo(
    @SerializedName("email")
    val email: String?,
    @SerializedName("id")
    val id: Int?,
    @SerializedName("nama_lengkap")
    val fullName: String?,
    @SerializedName("role")
    val role: String?,
    @SerializedName("total_points")
    val totalPoints: Int?,
    @SerializedName("username")
    val username: String?
)

data class ProgramInfo(
    @SerializedName("id")
    val id: Int?,
    @SerializedName("nama_program")
    val namaProgram: String?,
    @SerializedName("nama_terapis_program")
    val namaTerapisProgram: String?
)

data class SummaryTotalHitungan(
    @SerializedName("sempurna")
    val ok: Int?,
    @SerializedName("tidak_sempurna")
    val ng: Int?,
    @SerializedName("tidak_terdeteksi")
    val undetected: Int?
)