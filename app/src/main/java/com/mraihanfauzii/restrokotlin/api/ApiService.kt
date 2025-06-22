package com.mraihanfauzii.restrokotlin.api

import com.google.gson.JsonElement
import com.mraihanfauzii.restrokotlin.model.CalendarProgramsListResponse
import com.mraihanfauzii.restrokotlin.model.DeleteResponse
import com.mraihanfauzii.restrokotlin.model.DietPlanResponse
import com.mraihanfauzii.restrokotlin.model.GeneralResponse
import com.mraihanfauzii.restrokotlin.model.LoginRequest
import com.mraihanfauzii.restrokotlin.model.LoginResponse
import com.mraihanfauzii.restrokotlin.model.PatientProfileResponse
import com.mraihanfauzii.restrokotlin.model.ProfileUpdateRequest
import com.mraihanfauzii.restrokotlin.model.RegisterResponse
import com.mraihanfauzii.restrokotlin.model.RegisterRequest
import com.mraihanfauzii.restrokotlin.model.SingleProgramDetailResponse
import com.mraihanfauzii.restrokotlin.model.UpdateStatusProgramResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

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

    @GET("api/patient/profile")
    fun getPatientProfile(
        @Header("Authorization") token: String
    ): Call<PatientProfileResponse>

    @PUT("api/patient/profile")
    fun updatePatientProfile(
        @Header("Authorization") token: String,
        @Body request: ProfileUpdateRequest
    ): Call<GeneralResponse>

    @GET("api/patient/diet-plan/{tanggal_str}")
    fun getDietPlan(
        @Header("Authorization") token: String,
        @Path("tanggal_str") date: String
    ): Call<DietPlanResponse>

    @GET("api/patient/calendar-programs")
    fun getCalendarPrograms(
        @Header("Authorization") token: String,
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null
    ): Call<CalendarProgramsListResponse>

    @GET("api/program/{program_id}")
    fun getProgramDetail(
        @Header("Authorization") token: String,
        @Path("program_id") programId: Int
    ): Call<SingleProgramDetailResponse>

    @PUT("api/program/{program_id}/update-status")
    fun updateProgramStatus(
        @Header("Authorization") token: String,
        @Path("program_id") programId: Int,
        @Body requestBody: Map<String, String>
    ): Call<UpdateStatusProgramResponse>
}