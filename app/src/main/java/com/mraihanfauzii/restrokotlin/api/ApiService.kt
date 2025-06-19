//package com.mraihanfauzii.restrokotlin.api
//
//import com.hackathon.devlabsuser.model.AddMessageData
//import com.hackathon.devlabsuser.model.AddMessageRequest
//import com.hackathon.devlabsuser.model.ApiResponse
//import com.hackathon.devlabsuser.model.AverageRating
//import com.hackathon.devlabsuser.model.DeleteResponse
//import com.hackathon.devlabsuser.model.LastMessage
//import com.hackathon.devlabsuser.model.LoginData
//import com.hackathon.devlabsuser.model.LoginRequest
//import com.hackathon.devlabsuser.model.Message
//import com.hackathon.devlabsuser.model.Portfolio
//import com.hackathon.devlabsuser.model.Project
//import com.hackathon.devlabsuser.model.ProjectRequest
//import com.hackathon.devlabsuser.model.ProjectResponse
//import com.hackathon.devlabsuser.model.Promo
//import com.hackathon.devlabsuser.model.Rating
//import com.hackathon.devlabsuser.model.RecArchitect
//import com.hackathon.devlabsuser.model.RecommendedArchitectsRequest
//import com.hackathon.devlabsuser.model.RegisterData
//import com.hackathon.devlabsuser.model.RegisterRequest
//import com.hackathon.devlabsuser.model.Theme
//import com.hackathon.devlabsuser.model.UpdateProfileData
//import com.hackathon.devlabsuser.model.UserData
//import okhttp3.MultipartBody
//import okhttp3.RequestBody
//import retrofit2.Call
//import retrofit2.http.Body
//import retrofit2.http.DELETE
//import retrofit2.http.GET
//import retrofit2.http.Header
//import retrofit2.http.Multipart
//import retrofit2.http.POST
//import retrofit2.http.PUT
//import retrofit2.http.Part
//import retrofit2.http.Path
//import retrofit2.http.Query
//
//interface ApiService {
//    @POST("users/register")
//    fun register (
//        @Body registerRequest: RegisterRequest
//    ): Call<ApiResponse<RegisterData>>
//
//    @POST("users/login")
//    fun login (
//        @Body loginRequest: LoginRequest
//    ): Call<ApiResponse<LoginData>>
//
//    @DELETE("users/logout")
//    fun logout (
//        @Header("Authorization") token: String
//    ): Call<DeleteResponse>
//
//    @GET("users")
//    fun getProfile (
//        @Header("Authorization") token: String,
//    ): Call<ApiResponse<UserData>>
//
//    @Multipart
//    @PUT("users")
//    fun putProfile (
//        @Header("Authorization") token: String,
//        @Part("profile_name") profileName: RequestBody,
//        @Part("profile_description") profileDescription: RequestBody,
//        @Part("phonenumber") phoneNumber: RequestBody,
//        @Part profile_picture: MultipartBody.Part
//    ): Call<ApiResponse<UpdateProfileData>>
//
//    @GET("promo")
//    fun getPromo (
//        @Header("Authorization") token: String,
//    ): Call<ApiResponse<List<Promo>>>
//
//    @GET("messages/last")
//    fun getLastMessage (
//        @Header("Authorization") token: String,
//    ): Call<ApiResponse<List<LastMessage>>>
//
//    @GET("messages")
//    fun getMessage (
//        @Header("Authorization") token: String,
//        @Query("first_user_id") firstUserId: String,
//        @Query("second_user_id") secondUserId: String,
//    ): Call<ApiResponse<List<Message>>>
//
//    @POST("messages")
//    fun addMessage (
//        @Header("Authorization") token: String,
//        @Body addMessageRequest: AddMessageRequest
//    ): Call<ApiResponse<AddMessageData>>
//
//    @GET("users/list")
//    fun getAllUsers (
//        @Header("Authorization") token: String
//    ): Call<ApiResponse<List<UserData>>>
//
//    @GET("themes")
//    fun getAllThemes(
//        @Header("Authorization") token: String
//    ): Call<ApiResponse<List<Theme>>>
//
//    @GET("portofolios/trending")
//    fun getTrendingPortfolios(
//        @Header("Authorization") token: String
//    ): Call<ApiResponse<List<Portfolio>>>
//
//    @GET("portofolios/recent")
//    fun getRecentPortfolios(
//        @Header("Authorization") token: String
//    ): Call<ApiResponse<List<Portfolio>>>
//
//    @POST("projects/{userId}")
//    fun createProject(
//        @Header("Authorization") token: String,
//        @Path("userId") userId: String,
//        @Body request: ProjectRequest
//    ): Call<ProjectResponse>
//
//    @GET("portofolios")
//    fun getPortfolio (
//        @Header("Authorization") token: String,
//        @Query("architect_id") architectId: String,
//    ): Call<ApiResponse<List<Portfolio>>>
//
//    @GET("ratings/ratee/{architect_id}")
//    fun getRatings(
//        @Header("Authorization") token: String,
//        @Path("architect_id") architectId: String
//    ): Call<ApiResponse<List<Rating>>>
//
//    @GET("ratings/ratee/{architect_id}/average")
//    fun getRatingsAverage(
//        @Header("Authorization") token: String,
//        @Path("architect_id") architectId: String
//    ): Call<ApiResponse<AverageRating>>
//
//    @GET("projects")
//    fun getProjectsByUserId(
//        @Header("Authorization") token: String
//    ): Call<ApiResponse<List<Project>>>
//
//    @GET("projects/{id}")
//    fun getProjectById(
//        @Header("Authorization") token: String,
//        @Path("id") projectId: String
//    ): Call<ApiResponse<List<Project>>>
//
//    @POST("projects/recommended-architects")
//    fun getRecommendedArchitects(
//        @Header("Authorization") token: String,
//        @Body request: RecommendedArchitectsRequest
//    ): Call<ApiResponse<List<RecArchitect>>>
//
//    @GET("payments")
//    fun getPayments (
//        @Header("Authorization") token: String,
//    ): Call<ApiResponse<List<Portfolio>>>
//}