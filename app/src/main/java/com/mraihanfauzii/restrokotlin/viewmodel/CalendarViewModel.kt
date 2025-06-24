package com.mraihanfauzii.restrokotlin.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mraihanfauzii.restrokotlin.api.ApiConfig
import com.mraihanfauzii.restrokotlin.model.CalendarProgramResponse
import kotlinx.coroutines.launch
import retrofit2.HttpException

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

    fun getCalendarPrograms(token: String, startDate: String? = null, endDate: String? = null) {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val response = ApiConfig.getApiService().getCalendarPrograms("Bearer $token", startDate, endDate)
                _calendarPrograms.value = response.programs
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val errorMessage = "Gagal mengambil program kalender: ${e.code()} - ${e.message()} - $errorBody"
                Log.e("CalendarViewModel", errorMessage)
                _errorMessage.value = errorMessage
            } catch (e: Exception) {
                val errorMessage = "Koneksi gagal: ${e.message.toString()}"
                Log.e("CalendarViewModel", "GET Calendar Programs Error: $errorMessage")
                _errorMessage.value = errorMessage
            } finally {
                _isLoading.value = false
            }
        }
    }
}