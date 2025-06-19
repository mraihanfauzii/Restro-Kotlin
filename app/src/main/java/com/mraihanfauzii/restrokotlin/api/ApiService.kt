package com.mraihanfauzii.restrokotlin.api

import com.mraihanfauzii.restrokotlin.model.DeleteResponse
import com.mraihanfauzii.restrokotlin.model.LoginRequest
import com.mraihanfauzii.restrokotlin.model.LoginResponse
import com.mraihanfauzii.restrokotlin.model.RegisterResponse
import com.mraihanfauzii.restrokotlin.model.RegisterRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    @POST("auth/pasien/register")
    fun register (
        @Body registerRequest: RegisterRequest
    ): Call<RegisterResponse>

    @POST("auth/pasien/login")
    fun login (
        @Body loginRequest: LoginRequest
    ): Call<LoginResponse>

    @DELETE("auth/logout")
    fun logout (
        @Header("Authorization") token: String
    ): Call<DeleteResponse>
}