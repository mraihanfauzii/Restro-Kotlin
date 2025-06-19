package com.mraihanfauzii.restrokotlin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.mraihanfauzii.restrokotlin.utils.Setting
import kotlinx.coroutines.launch

class SettingViewModel(private val preferences: Setting) : ViewModel() {

    fun getSetting() = preferences.getSetting().asLiveData()

    fun saveSetting(isDarkMode: Boolean) {
        viewModelScope.launch {
            preferences.saveSetting(isDarkMode)
        }
    }

    class Factory(private val preferences: Setting) : ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(model: Class<T>): T = SettingViewModel(preferences) as T
    }
}