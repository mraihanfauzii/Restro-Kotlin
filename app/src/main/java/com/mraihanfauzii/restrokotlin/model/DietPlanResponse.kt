package com.mraihanfauzii.restrokotlin.model

import com.google.gson.annotations.SerializedName

data class DietPlanResponse(
    @SerializedName("id")
    val id: Int?,
    @SerializedName("pasien_id")
    val patientId: Int?,
    @SerializedName("nama_pasien")
    val patientName: String?,
    @SerializedName("terapis_id")
    val therapistId: Int?,
    @SerializedName("nama_terapis")
    val therapistName: String?,
    @SerializedName("tanggal_makan")
    val mealDate: String?, // YYYY-MM-DD
    @SerializedName("menu_pagi")
    val breakfastMenu: String?,
    @SerializedName("menu_siang")
    val lunchMenu: String?,
    @SerializedName("menu_malam")
    val dinnerMenu: String?,
    @SerializedName("cemilan")
    val snackMenu: String?,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("updated_at")
    val updatedAt: String?
)