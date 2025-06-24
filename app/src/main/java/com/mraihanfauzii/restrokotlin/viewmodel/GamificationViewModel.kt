package com.mraihanfauzii.restrokotlin.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mraihanfauzii.restrokotlin.model.BadgeInfo
import com.mraihanfauzii.restrokotlin.model.LeaderboardEntry
import com.mraihanfauzii.restrokotlin.model.UserBadge
import com.mraihanfauzii.restrokotlin.utils.GamificationRepository
import kotlinx.coroutines.launch

class GamificationViewModel(private val repository: GamificationRepository) : ViewModel() {

    private val _leaderboard = MutableLiveData<List<LeaderboardEntry>>()
    val leaderboard: LiveData<List<LeaderboardEntry>> = _leaderboard

    private val _myBadges = MutableLiveData<List<UserBadge>>()
    val myBadges: LiveData<List<UserBadge>> = _myBadges

    private val _allBadges = MutableLiveData<List<BadgeInfo>>()
    val allBadges: LiveData<List<BadgeInfo>> = _allBadges

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun getLeaderboard(token: String, page: Int? = null, perPage: Int? = null) {
        _isLoading.value = true
        viewModelScope.launch {
            repository.getLeaderboard(token, page, perPage)
                .onSuccess {
                    _leaderboard.value = it.leaderboard
                    _errorMessage.value = null
                }
                .onFailure {
                    _errorMessage.value = it.message
                }
            _isLoading.value = false
        }
    }

    fun getMyBadges(token: String) {
        _isLoading.value = true
        viewModelScope.launch {
            repository.getMyBadges(token)
                .onSuccess {
                    _myBadges.value = it.myBadges
                    _errorMessage.value = null
                }
                .onFailure {
                    _errorMessage.value = it.message
                }
            _isLoading.value = false
        }
    }

    fun getAllBadges(token: String) {
        _isLoading.value = true
        viewModelScope.launch {
            repository.getAllBadges(token)
                .onSuccess {
                    _allBadges.value = it
                    _errorMessage.value = null
                }
                .onFailure {
                    _errorMessage.value = it.message
                }
            _isLoading.value = false
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}