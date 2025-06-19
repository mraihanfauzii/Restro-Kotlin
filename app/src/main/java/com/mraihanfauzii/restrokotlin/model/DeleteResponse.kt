package com.mraihanfauzii.restrokotlin.model

data class DeleteResponse(
    val success: Boolean,
    val message: String,
    val code: Int
)