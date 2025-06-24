package com.mraihanfauzii.restrokotlin.model

import com.google.gson.annotations.SerializedName

data class DetailHasilGerakan(
    @SerializedName("gerakan_id")
    val gerakanId: Int,
    @SerializedName("urutan_gerakan_dalam_program")
    val urutanGerakanDalamProgram: Int,
    @SerializedName("jumlah_sempurna")
    val jumlahSempurna: Int,
    @SerializedName("jumlah_tidak_sempurna")
    val jumlahTidakSempurna: Int,
    @SerializedName("jumlah_tidak_terdeteksi")
    val jumlahTidakTerdeteksi: Int,
    @SerializedName("waktu_aktual_per_gerakan_detik")
    val waktuAktualPerGerakanDetik: Int
)