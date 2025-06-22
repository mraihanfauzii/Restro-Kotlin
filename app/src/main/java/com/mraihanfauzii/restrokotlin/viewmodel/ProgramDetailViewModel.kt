package com.mraihanfauzii.restrokotlin.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mraihanfauzii.restrokotlin.api.ApiConfig
import com.mraihanfauzii.restrokotlin.model.CalendarProgramResponse
import com.mraihanfauzii.restrokotlin.model.SingleProgramDetailResponse
import com.mraihanfauzii.restrokotlin.model.UpdateStatusProgramResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProgramDetailViewModel : ViewModel() {

    private val _programDetail = MutableLiveData<CalendarProgramResponse?>()
    val programDetail: LiveData<CalendarProgramResponse?> = _programDetail

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _updateStatusSuccess = MutableLiveData<Boolean>()
    val updateStatusSuccess: LiveData<Boolean> = _updateStatusSuccess

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun getProgramDetail(token: String, programId: Int) {
        _isLoading.value = true
        _errorMessage.value = null

        ApiConfig.getApiService().getProgramDetail("Bearer $token", programId).enqueue(object : Callback<SingleProgramDetailResponse> {
            override fun onResponse(
                call: Call<SingleProgramDetailResponse>,
                response: Response<SingleProgramDetailResponse>
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _programDetail.value = response.body()?.program
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = "Gagal mengambil detail program: ${response.message()} - $errorBody"
                    Log.e("ProgramDetailViewModel", errorMessage)
                    _errorMessage.value = errorMessage
                    _programDetail.value = null // Reset detail jika gagal
                }
            }

            override fun onFailure(call: Call<SingleProgramDetailResponse>, t: Throwable) {
                _isLoading.value = false
                val errorMessage = "Koneksi gagal saat mengambil detail program: ${t.message.toString()}"
                Log.e("ProgramDetailViewModel", errorMessage)
                _errorMessage.value = errorMessage
                _programDetail.value = null // Reset detail jika gagal
            }
        })
    }

    fun updateProgramStatus(token: String, programId: Int, newStatus: String) {
        _isLoading.value = true
        _errorMessage.value = null
        _updateStatusSuccess.value = false // Reset status

        val requestBody = mapOf("status" to newStatus)

        ApiConfig.getApiService().updateProgramStatus("Bearer $token", programId, requestBody).enqueue(object : Callback<UpdateStatusProgramResponse> {
            override fun onResponse(
                call: Call<UpdateStatusProgramResponse>,
                response: Response<UpdateStatusProgramResponse>
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _updateStatusSuccess.value = true
                    // Perbarui detail program di LiveData setelah update sukses
                    _programDetail.value = response.body()?.program
                    _errorMessage.value = response.body()?.msg // Tampilkan pesan sukses dari API
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = "Gagal memperbarui status program: ${response.message()} - $errorBody"
                    Log.e("ProgramDetailViewModel", errorMessage)
                    _errorMessage.value = errorMessage
                    _updateStatusSuccess.value = false
                }
            }

            override fun onFailure(call: Call<UpdateStatusProgramResponse>, t: Throwable) {
                _isLoading.value = false
                val errorMessage = "Koneksi gagal saat memperbarui status program: ${t.message.toString()}"
                Log.e("ProgramDetailViewModel", errorMessage)
                _errorMessage.value = errorMessage
                _updateStatusSuccess.value = false
            }
        })
    }
}