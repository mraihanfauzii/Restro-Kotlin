package com.mraihanfauzii.restrokotlin.model

data class ExercisePerformance(
    val actionName: String,
    val targetReps: Int,
    val gerakanId: Int,             // ID gerakan dari library
    val orderInProgram: Int,        // Urutan gerakan dalam program
    var completedReps: Int = 0,
    var failedAttempts: Int = 0,
    var undetectedCount: Int = 0,   // Jumlah tidak terdeteksi
    var actualDurationMs: Long = 0L // Waktu aktual per gerakan dalam milidetik
) {
    fun recordAttempt(result: String) {
        when (result) {
            "Sempurna" -> completedReps++
            "Waktu Habis" -> failedAttempts++
            // Anda mungkin perlu logika lebih lanjut untuk "Tidak Terdeteksi"
            // jika bukan dari hasil deteksi PoseLandmarker yang null.
            // Untuk sekarang, undetectedCount++ sudah di handle di onResults()
        }
    }
}