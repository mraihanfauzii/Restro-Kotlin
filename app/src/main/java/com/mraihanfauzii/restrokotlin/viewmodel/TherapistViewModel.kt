package com.mraihanfauzii.restrokotlin.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mraihanfauzii.restrokotlin.model.Therapist
import kotlinx.coroutines.launch
import com.mraihanfauzii.restrokotlin.ui.authentication.AuthenticationManager // Import AuthenticationManager
import android.util.Log
import android.app.Application // Untuk mengakses AuthenticationManager
import androidx.lifecycle.AndroidViewModel
import com.mraihanfauzii.restrokotlin.api.ApiConfig

class TherapistViewModel(application: Application) : AndroidViewModel(application) { // Ganti ViewModel() menjadi AndroidViewModel(application)

    private val _therapists = MutableLiveData<List<Therapist>>()
    val therapists: LiveData<List<Therapist>> = _therapists

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val authManager = AuthenticationManager(application.applicationContext) // Inisialisasi AuthenticationManager

    fun getAllTherapists() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val token = authManager.getAccess(AuthenticationManager.TOKEN)
                if (token.isNullOrEmpty()) {
                    _errorMessage.value = "Token autentikasi tidak ditemukan. Harap login ulang."
                    _isLoading.value = false
                    return@launch
                }
                val fullToken = "Bearer $token"
                val response = ApiConfig.getApiService().getAllTherapists(fullToken)
                _therapists.value = response.terapisList
                Log.d("TherapistViewModel", "Successfully fetched therapists: ${response.terapisList.size}")
            } catch (e: Exception) {
                _errorMessage.value = "Gagal mengambil daftar terapis: ${e.message}"
                Log.e("TherapistViewModel", "Error fetching therapists", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}