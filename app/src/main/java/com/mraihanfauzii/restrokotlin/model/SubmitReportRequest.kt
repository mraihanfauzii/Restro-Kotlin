package com.mraihanfauzii.restrokotlin.model

import com.google.gson.annotations.SerializedName

data class SubmitReportRequest(
    @SerializedName("program_rehabilitasi_id")
    val programRehabilitasiId: Int,
    @SerializedName("tanggal_laporan")
    val tanggalLaporan: String,
    @SerializedName("total_waktu_rehabilitasi_detik")
    val totalWaktuRehabilitasiDetik: Int,
    @SerializedName("catatan_pasien_laporan")
    val catatanPasienLaporan: String,
    @SerializedName("detail_hasil_gerakan")
    val detailHasilGerakan: List<DetailHasilGerakan>
)