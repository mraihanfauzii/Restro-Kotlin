package com.mraihanfauzii.restrokotlin.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ExercisePlan(
    val actionName : String,
    var targetReps : Int
) {
    var completed = 0
    var failed    = 0

    /** Gerakan yang hanya butuh badan-atas → landmark kaki disembunyikan */
    fun upperBodyOnly(): Boolean = actionName in UPPER_BODY_ONLY

    companion object {
        private val UPPER_BODY_ONLY = setOf(
            "Rentangkan",
            "Naikan Kepalan Kedepan",
            "Angkat Tangan"
        )
    }
}