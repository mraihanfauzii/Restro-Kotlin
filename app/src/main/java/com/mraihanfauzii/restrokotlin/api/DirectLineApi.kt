// com/mraihanfauzii/restrokotlin/network/DirectLineApi.kt
package com.mraihanfauzii.restrokotlin.api

import com.mraihanfauzii.restrokotlin.model.ActivitiesResponse
import com.mraihanfauzii.restrokotlin.model.Activity
import com.mraihanfauzii.restrokotlin.model.DirectLineTokenResponse
import retrofit2.Response
import retrofit2.http.*

interface DirectLineApi {

    @POST("v3/directline/tokens/generate")
    suspend fun generateToken(
        @Header("Authorization") directLineSecret: String // Ini adalah secret key Anda
    ): Response<DirectLineTokenResponse>

    @POST("v3/directline/conversations/{conversationId}/activities")
    suspend fun sendMessage(
        @Path("conversationId") conversationId: String,
        @Header("Authorization") token: String, // Ini adalah token yang didapat dari generateToken
        @Body activity: Activity
    ): Response<Unit> // Response bisa kosong jika sukses

    @GET("v3/directline/conversations/{conversationId}/activities")
    suspend fun getActivities(
        @Path("conversationId") conversationId: String,
        @Header("Authorization") token: String, // Ini adalah token yang didapat dari generateToken
        @Query("watermark") watermark: String? = null // Untuk mengambil pesan terbaru
    ): Response<ActivitiesResponse>
}