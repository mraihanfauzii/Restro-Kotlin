package com.mraihanfauzii.restrokotlin.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mraihanfauzii.restrokotlin.api.ApiConfig
import com.mraihanfauzii.restrokotlin.model.ReportItem
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class ReportHistoryViewModel : ViewModel() {

    private val _report = MutableLiveData<ReportItem?>()
    val report: LiveData<ReportItem?> = _report

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun loadHistory(token: String, programId: Int) {
        _isLoading.value = true
        _errorMessage.value = null // Clear error message at the start of loading
        _report.value = null // Clear previous data

        viewModelScope.launch {
            try {
                val response = ApiConfig.getApiService().getPatientHistory(
                    auth = "Bearer $token",
                    page = 1,
                    perPage = 100
                )

                // Filter the reports to find the one matching programId
                val specificReport = response.laporan?.firstOrNull { it.programInfo?.id == programId }

                _report.value = specificReport // Set the specific report

                // No need to set _errorMessage here if specificReport is null.
                // The UI will handle displaying tvNotFound.

            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val msg = "HTTP Error ${e.code()}: ${e.message()} - $errorBody"
                Log.e("ReportHistoryViewModel", msg, e)
                _errorMessage.value = "Gagal memuat riwayat laporan: ${e.message()}" // Lebih ringkas untuk Toast
            } catch (e: IOException) {
                Log.e("ReportHistoryViewModel", "Network error", e)
                _errorMessage.value = "Kesalahan koneksi jaringan. Mohon periksa internet Anda."
            } catch (e: Exception) {
                Log.e("ReportHistoryViewModel", "Unknown error", e)
                _errorMessage.value = "Terjadi kesalahan tidak terduga saat memuat riwayat."
            } finally {
                _isLoading.value = false
            }
        }
    }
}