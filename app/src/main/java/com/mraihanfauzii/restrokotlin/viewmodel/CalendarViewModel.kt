package com.mraihanfauzii.restrokotlin.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mraihanfauzii.restrokotlin.api.ApiConfig
import com.mraihanfauzii.restrokotlin.model.CalendarProgramResponse
import com.mraihanfauzii.restrokotlin.model.CalendarProgramsListResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CalendarViewModel : ViewModel() {

    private val _calendarPrograms = MutableLiveData<List<CalendarProgramResponse>?>()
    val calendarPrograms: LiveData<List<CalendarProgramResponse>?> = _calendarPrograms

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    // Fungsi untuk mendapatkan program dengan rentang tanggal opsional
    fun getCalendarPrograms(token: String, startDate: String? = null, endDate: String? = null) {
        _isLoading.value = true
        _errorMessage.value = null
        // Tidak perlu mereset _calendarPrograms.value = null agar data sebelumnya tetap terlihat
        // sampai data baru berhasil diambil, atau bisa juga direset jika ingin UI kosong saat loading.

        ApiConfig.getApiService().getCalendarPrograms("Bearer $token", startDate, endDate).enqueue(object : Callback<CalendarProgramsListResponse> {
            override fun onResponse(
                call: Call<CalendarProgramsListResponse>,
                response: Response<CalendarProgramsListResponse>
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _calendarPrograms.value = response.body()?.programs
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = "Gagal mengambil program kalender: ${response.message()}"
                    Log.e("CalendarViewModel", "$errorMessage - $errorBody")
                    _errorMessage.value = errorMessage
                }
            }

            override fun onFailure(call: Call<CalendarProgramsListResponse>, t: Throwable) {
                _isLoading.value = false
                val errorMessage = "Koneksi gagal: ${t.message.toString()}"
                Log.e("CalendarViewModel", "GET Calendar Programs Error: $errorMessage")
                _errorMessage.value = errorMessage
            }
        })
    }
}