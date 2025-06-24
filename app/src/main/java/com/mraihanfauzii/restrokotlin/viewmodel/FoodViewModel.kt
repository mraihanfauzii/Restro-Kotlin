package com.mraihanfauzii.restrokotlin.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mraihanfauzii.restrokotlin.api.ApiConfig
import com.mraihanfauzii.restrokotlin.model.DietPlanResponse
import kotlinx.coroutines.launch
import retrofit2.HttpException
import org.json.JSONObject

class FoodViewModel : ViewModel() {

    private val _dietPlan = MutableLiveData<DietPlanResponse?>()
    val dietPlan: LiveData<DietPlanResponse?> = _dietPlan

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun getDietPlan(token: String, date: String) {
        _isLoading.value = true
        _errorMessage.value = null
        _dietPlan.value = null

        viewModelScope.launch {
            try {
                val response = ApiConfig.getApiService().getDietPlan("Bearer $token", date)
                _dietPlan.value = response
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val statusCode = e.code()
                Log.e("FoodViewModel", "HTTP Error for Diet Plan ($statusCode): $errorBody")

                when (statusCode) {
                    404 -> {
                        try {
                            val jsonError = JSONObject(errorBody)
                            val msg = jsonError.optString("msg", "Tidak ada rencana pola makan untuk tanggal ini.")
                            _errorMessage.value = msg
                        } catch (jsonException: Exception) {
                            _errorMessage.value = "Tidak ada rencana pola makan untuk tanggal ini."
                        }
                    }
                    401 -> {
                        _errorMessage.value = "Sesi Anda telah berakhir, silakan login ulang."
                    }
                    else -> {
                        _errorMessage.value = "Terjadi kesalahan saat mengambil rencana pola makan. Silakan coba lagi."
                    }
                }
            } catch (e: Exception) {
                Log.e("FoodViewModel", "GET Diet Plan Error: ${e.message}", e)
                _errorMessage.value = "Koneksi gagal atau terjadi kesalahan tak terduga."
            } finally {
                _isLoading.value = false
            }
        }
    }
}