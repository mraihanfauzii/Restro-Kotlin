package com.mraihanfauzii.restrokotlin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mraihanfauzii.restrokotlin.api.ApiService
import com.mraihanfauzii.restrokotlin.utils.GamificationRepository

class GamificationViewModelFactory(private val apiService: ApiService) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GamificationViewModel::class.java)) {
            return GamificationViewModel(GamificationRepository(apiService)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}