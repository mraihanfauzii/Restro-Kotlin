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

        viewModelScope.launch {
            try {
                val response = ApiConfig.getApiService().getProgramDetail("Bearer $token", programId)
                _programDetail.value = response.program
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val errorMessage = "Gagal mengambil detail program: ${e.code()} - ${e.message()} - $errorBody"
                Log.e("ProgramDetailViewModel", errorMessage)
                _errorMessage.value = errorMessage
                _programDetail.value = null
            } catch (e: Exception) {
                val errorMessage = "Koneksi gagal saat mengambil detail program: ${e.message.toString()}"
                Log.e("ProgramDetailViewModel", errorMessage)
                _errorMessage.value = errorMessage
                _programDetail.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProgramStatus(token: String, programId: Int, newStatus: String) {
        _isLoading.value = true
        _errorMessage.value = null
        _updateStatusSuccess.value = false

        val requestBody = mapOf("status" to newStatus)

        viewModelScope.launch {
            try {
                val response = ApiConfig.getApiService().updateProgramStatus("Bearer $token", programId, requestBody)
                _updateStatusSuccess.value = true
                _programDetail.value = response.program
                _errorMessage.value = response.msg
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val errorMessage = "Gagal memperbarui status program: ${e.code()} - ${e.message()} - $errorBody"
                Log.e("ProgramDetailViewModel", errorMessage)
                _errorMessage.value = errorMessage
                _updateStatusSuccess.value = false
            } catch (e: Exception) {
                val errorMessage = "Koneksi gagal saat memperbarui status program: ${e.message.toString()}"
                Log.e("ProgramDetailViewModel", errorMessage)
                _errorMessage.value = errorMessage
                _updateStatusSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }
}