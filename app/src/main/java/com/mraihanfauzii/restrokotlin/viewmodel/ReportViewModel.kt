package com.mraihanfauzii.restrokotlin.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mraihanfauzii.restrokotlin.api.ApiConfig
import com.mraihanfauzii.restrokotlin.model.SubmitReportRequest
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

class ReportViewModel : ViewModel() {

    private val _submitReportStatus = MutableLiveData<ReportSubmitStatus>()
    val submitReportStatus: LiveData<ReportSubmitStatus> = _submitReportStatus

    fun submitRehabReport(token: String, request: SubmitReportRequest) {
        _submitReportStatus.value = ReportSubmitStatus.Loading

        viewModelScope.launch {
            try {
                val apiService = ApiConfig.getApiService()
                val response = apiService.submitRehabReport(token, request)

                if (response.isSuccessful) {
                    _submitReportStatus.value = ReportSubmitStatus.Success(response.message())
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = try {
                        // Coba parse error body jika ada format JSON yang diketahui
                        // Misalnya, jika errorBody adalah {"msg": "Error message"}
                        JSONObject(errorBody).getString("msg")
                    } catch (e: Exception) {
                        errorBody ?: "Terjadi kesalahan tidak diketahui (Kode: ${response.code()})"
                    }
                    _submitReportStatus.value =
                        ReportSubmitStatus.Error(errorMessage, response.code())
                }
            } catch (e: HttpException) {
                val errorMessage = e.response()?.errorBody()?.string() ?: e.message() ?: "Kesalahan HTTP"
                _submitReportStatus.value = ReportSubmitStatus.Error(errorMessage, e.code())
            } catch (e: IOException) {
                _submitReportStatus.value =
                    ReportSubmitStatus.Error("Kesalahan jaringan: ${e.message}", -1)
            } catch (e: Exception) {
                _submitReportStatus.value =
                    ReportSubmitStatus.Error("Terjadi kesalahan tak terduga: ${e.message}", -2)
            }
        }
    }

    fun resetStatus() {
        _submitReportStatus.value = ReportSubmitStatus.Idle
    }
}

sealed class ReportSubmitStatus {
    object Idle : ReportSubmitStatus()
    object Loading : ReportSubmitStatus()
    data class Success(val message: String?) : ReportSubmitStatus()
    data class Error(val message: String, val errorCode: Int) : ReportSubmitStatus()
}