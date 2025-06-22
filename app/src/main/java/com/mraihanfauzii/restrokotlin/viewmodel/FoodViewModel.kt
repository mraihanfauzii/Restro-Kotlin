package com.mraihanfauzii.restrokotlin.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mraihanfauzii.restrokotlin.api.ApiConfig
import com.mraihanfauzii.restrokotlin.model.DietPlanResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
        _dietPlan.value = null // Reset data sebelumnya

        ApiConfig.getApiService().getDietPlan("Bearer $token", date).enqueue(object : Callback<DietPlanResponse> {
            override fun onResponse(
                call: Call<DietPlanResponse>,
                response: Response<DietPlanResponse>
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _dietPlan.value = response.body()
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = "Gagal mengambil rencana pola makan: ${response.message()}"
                    Log.e("FoodViewModel", "$errorMessage - $errorBody")
                    _errorMessage.value = errorMessage
                }
            }

            override fun onFailure(call: Call<DietPlanResponse>, t: Throwable) {
                _isLoading.value = false
                val errorMessage = "Koneksi gagal: ${t.message.toString()}"
                Log.e("FoodViewModel", "GET Diet Plan Error: $errorMessage")
                _errorMessage.value = errorMessage
            }
        })
    }
}