package com.mraihanfauzii.restrokotlin.utils

import com.mraihanfauzii.restrokotlin.api.ApiService
import com.mraihanfauzii.restrokotlin.model.BadgeInfo
import com.mraihanfauzii.restrokotlin.model.LeaderboardResponse
import com.mraihanfauzii.restrokotlin.model.MyBadgesResponse

class GamificationRepository(private val apiService: ApiService) {

    suspend fun getLeaderboard(token: String, page: Int? = null, perPage: Int? = null): Result<LeaderboardResponse> {
        return try {
            val response = apiService.getLeaderboard("Bearer $token", page, perPage)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Response body is null"))
            } else {
                Result.failure(Exception("Failed to get leaderboard: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMyBadges(token: String): Result<MyBadgesResponse> {
        return try {
            val response = apiService.getMyBadges("Bearer $token")
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Response body is null"))
            } else {
                Result.failure(Exception("Failed to get my badges: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllBadges(token: String): Result<List<BadgeInfo>> {
        return try {
            val response = apiService.getAllBadges("Bearer $token")
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it.badges)
                } ?: Result.failure(Exception("Response body is null"))
            } else {
                Result.failure(Exception("Failed to get all badges: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}