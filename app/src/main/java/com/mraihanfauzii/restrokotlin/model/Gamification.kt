package com.mraihanfauzii.restrokotlin.model

import com.google.gson.annotations.SerializedName

data class BadgeInfo(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("point_threshold") val pointThreshold: Int,
    @SerializedName("image_url") val imageUrl: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)

data class LeaderboardResponse(
    @SerializedName("leaderboard") val leaderboard: List<LeaderboardEntry>,
    @SerializedName("total_items") val totalItems: Int,
    @SerializedName("total_pages") val totalPages: Int,
    @SerializedName("current_page") val currentPage: Int
)

data class LeaderboardEntry(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("username") val username: String,
    @SerializedName("nama_lengkap") val fullName: String,
    @SerializedName("total_points") val totalPoints: Int,
    @SerializedName("highest_badge_info") val highestBadgeInfo: BadgeInfo?
)

data class BadgesResponse(
    @SerializedName("badges") val badges: List<BadgeInfo>
)

data class MyBadgesResponse(
    @SerializedName("my_badges") val myBadges: List<UserBadge>
)

data class UserBadge(
    @SerializedName("id") val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("badge_info") val badgeInfo: BadgeInfo,
    @SerializedName("awarded_at") val awardedAt: String
)