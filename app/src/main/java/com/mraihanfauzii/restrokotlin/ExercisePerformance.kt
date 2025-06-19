package com.mraihanfauzii.restrokotlin

// Data class to track exercise performance, similar to Flutter's ExercisePerformance
data class ExercisePerformance(
    val actionName: String,
    val targetReps: Int,
    var completedReps: Int = 0,
    var failedAttempts: Int = 0
) {
    fun recordAttempt(resultType: String) {
        if (resultType == "Sempurna") {
            completedReps++
        } else {
            failedAttempts++
        }
    }
}