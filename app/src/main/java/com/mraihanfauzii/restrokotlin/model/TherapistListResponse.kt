package com.mraihanfauzii.restrokotlin.model

import com.google.gson.annotations.SerializedName

data class TherapistListResponse(
    @SerializedName("terapis_list") val terapisList: List<Therapist>
)