package com.mraihanfauzii.restrokotlin.api

import com.mraihanfauzii.restrokotlin.model.BadgeInfo
import com.mraihanfauzii.restrokotlin.model.BadgesResponse
import com.mraihanfauzii.restrokotlin.model.CalendarProgramsListResponse
import com.mraihanfauzii.restrokotlin.model.DeleteResponse
import com.mraihanfauzii.restrokotlin.model.DietPlanResponse
import com.mraihanfauzii.restrokotlin.model.GeneralResponse
import com.mraihanfauzii.restrokotlin.model.LeaderboardResponse
import com.mraihanfauzii.restrokotlin.model.LoginRequest
import com.mraihanfauzii.restrokotlin.model.LoginResponse
import com.mraihanfauzii.restrokotlin.model.MyBadgesResponse
import com.mraihanfauzii.restrokotlin.model.PatientProfileResponse
import com.mraihanfauzii.restrokotlin.model.PatientReportHistoryResponse
import com.mraihanfauzii.restrokotlin.model.ProfilePictureUploadResponse
import com.mraihanfauzii.restrokotlin.model.ProfileUpdateRequest
import com.mraihanfauzii.restrokotlin.model.ProgramHistoryResponse
import com.mraihanfauzii.restrokotlin.model.RegisterResponse
import com.mraihanfauzii.restrokotlin.model.RegisterRequest
import com.mraihanfauzii.restrokotlin.model.SingleProgramDetailResponse
import com.mraihanfauzii.restrokotlin.model.SubmitReportRequest
import com.mraihanfauzii.restrokotlin.model.TherapistListResponse
import com.mraihanfauzii.restrokotlin.model.UpdateStatusProgramResponse
import okhttp3.MultipartBody
import retrofit2.Response

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("auth/pasien/register")
    suspend fun register (
        @Body registerRequest: RegisterRequest
    ): RegisterResponse

    @POST("auth/pasien/login")
    suspend fun login (
        @Body loginRequest: LoginRequest
    ): LoginResponse

    @DELETE("auth/logout")
    suspend fun logout (
        @Header("Authorization") token: String
    ): DeleteResponse

    @GET("api/patient/profile")
    suspend fun getPatientProfile(
        @Header("Authorization") token: String
    ): PatientProfileResponse

    @PUT("api/patient/profile")
    suspend fun updatePatientProfile(
        @Header("Authorization") token: String,
        @Body request: ProfileUpdateRequest
    ): GeneralResponse

    @GET("api/patient/diet-plan/{tanggal_str}")
    suspend fun getDietPlan(
        @Header("Authorization") token: String,
        @Path("tanggal_str") date: String
    ): DietPlanResponse

    @GET("api/patient/calendar-programs")
    suspend fun getCalendarPrograms(
        @Header("Authorization") token: String,
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null
    ): CalendarProgramsListResponse

    @GET("api/program/{program_id}")
    suspend fun getProgramDetail(
        @Header("Authorization") token: String,
        @Path("program_id") programId: Int
    ): SingleProgramDetailResponse

    @PUT("api/program/{program_id}/update-status")
    suspend fun updateProgramStatus(
        @Header("Authorization") token: String,
        @Path("program_id") programId: Int,
        @Body requestBody: Map<String, String>
    ): UpdateStatusProgramResponse

    @GET("terapis/list-all-terapis")
    suspend fun getAllTherapists(@Header("Authorization") token: String): TherapistListResponse

    @Multipart
    @POST("api/patient/profile/picture") // Gunakan POST atau PUT sesuai dokumentasi backend Anda
    suspend fun uploadPatientProfilePicture(
        @Header("Authorization") token: String,
        @Part fotoProfil: MultipartBody.Part
    ): ProfilePictureUploadResponse

    @GET("api/gamification/leaderboard")
    suspend fun getLeaderboard(
        @Header("Authorization") token: String,
        @Query("page") page: Int? = null,
        @Query("per_page") perPage: Int? = null
    ): Response<LeaderboardResponse>

    @GET("api/gamification/badges")
    suspend fun getAllBadges(
        @Header("Authorization") token: String
    ): Response<BadgesResponse>

    @GET("api/gamification/badges/{badge_id}")
    suspend fun getBadgeDetail(
        @Header("Authorization") token: String,
        @Path("badge_id") badgeId: Int
    ): Response<BadgeInfo>

    @GET("api/gamification/my-badges")
    suspend fun getMyBadges(
        @Header("Authorization") token: String
    ): Response<MyBadgesResponse>

    @GET("api/program/pasien/history")
    suspend fun getProgramHistory(
        @Header("Authorization") token: String,
        @Query("page") page: Int? = null,
        @Query("per_page") perPage: Int? = null
    ): ProgramHistoryResponse

    @POST("api/laporan/submit")
    suspend fun submitRehabReport(
        @Header("Authorization") token: String,
        @Body request: SubmitReportRequest
    ): Response<GeneralResponse>

    @GET("api/laporan/pasien/history")
    suspend fun getPatientHistory(
        @Header("Authorization") auth: String,
        @Query("page")     page: Int  = 1,
        @Query("per_page") perPage: Int = 50
    ): PatientReportHistoryResponse
}