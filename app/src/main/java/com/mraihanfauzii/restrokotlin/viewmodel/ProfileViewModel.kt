package com.mraihanfauzii.restrokotlin.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mraihanfauzii.restrokotlin.api.ApiConfig
import com.mraihanfauzii.restrokotlin.model.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileViewModel : ViewModel() {

    private val _patientProfile = MutableLiveData<PatientProfileResponse?>()
    val patientProfile: LiveData<PatientProfileResponse?> = _patientProfile

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isUpdateSuccess = MutableLiveData<Boolean>()
    val isUpdateSuccess: LiveData<Boolean> = _isUpdateSuccess

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun resetUpdateStatus() {
        _isUpdateSuccess.value = false
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun getPatientProfile(token: String) {
        _isLoading.value = true
        _errorMessage.value = null

        ApiConfig.getApiService().getPatientProfile("Bearer $token").enqueue(object : Callback<PatientProfileResponse> {
            override fun onResponse(
                call: Call<PatientProfileResponse>,
                response: Response<PatientProfileResponse>
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _patientProfile.value = response.body()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("ProfileViewModel", "GET Profile Failed: ${response.message()} - $errorBody")
                    _errorMessage.value = "Gagal mengambil data profil: ${response.message()}"
                }
            }

            override fun onFailure(call: Call<PatientProfileResponse>, t: Throwable) {
                _isLoading.value = false
                Log.e("ProfileViewModel", "GET Profile Error: ${t.message.toString()}")
                _errorMessage.value = "Koneksi gagal: ${t.message}"
            }
        })
    }

    // Fungsi untuk memperbarui informasi akun
    fun updateAccountInfo(token: String, request: AccountInfoRequest) {
        _isLoading.value = true
        _isUpdateSuccess.value = false
        _errorMessage.value = null

        // Buat ProfileUpdateRequest hanya dengan field yang relevan untuk akun
        // Ambil data profile yang sudah ada agar tidak menimpa field lain yang tidak diupdate
        val currentProfile = _patientProfile.value

        val updateRequest = ProfileUpdateRequest(
            username = request.username,
            email = request.email,
            phoneNumber = request.phoneNumber,
            // Sertakan field lain dari currentProfile agar tidak di-null-kan oleh PUT
            // jika backend Anda membutuhkan semua field atau akan mengeset null jika tidak ada
            address = currentProfile?.address,
            weight = currentProfile?.weight,
            bloodType = currentProfile?.bloodType,
            gender = currentProfile?.gender,
            fullName = currentProfile?.fullName,
            companionName = currentProfile?.companionName,
            allergyHistory = currentProfile?.allergyHistory,
            medicalHistory = currentProfile?.medicalHistory,
            dateOfBirth = currentProfile?.dateOfBirth,
            placeOfBirth = currentProfile?.placeOfBirth,
            height = currentProfile?.height,
        )

        ApiConfig.getApiService().updatePatientProfile("Bearer $token", updateRequest).enqueue(object : Callback<GeneralResponse> {
            override fun onResponse(
                call: Call<GeneralResponse>,
                response: Response<GeneralResponse>
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _isUpdateSuccess.value = true
                    _errorMessage.value = null
                    getPatientProfile(token)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("ProfileViewModel", "Update Account Info Failed: ${response.code()} - ${response.message()} - $errorBody")
                    _isUpdateSuccess.value = false
                    _errorMessage.value = "Gagal memperbarui informasi akun: ${response.message()}"
                }
            }

            override fun onFailure(call: Call<GeneralResponse>, t: Throwable) {
                _isLoading.value = false
                _isUpdateSuccess.value = false
                Log.e("ProfileViewModel", "Update Account Info Error: ${t.message.toString()}")
                _errorMessage.value = "Koneksi gagal: ${t.message}"
            }
        })
    }

    // Fungsi untuk memperbarui informasi pasien
    fun updatePatientInfo(token: String, request: PatientInfoRequest) {
        _isLoading.value = true
        _isUpdateSuccess.value = false
        _errorMessage.value = null

        val currentProfile = _patientProfile.value

        val updateRequest = ProfileUpdateRequest(
            fullName = request.fullName,
            gender = request.gender,
            dateOfBirth = request.dateOfBirth,
            placeOfBirth = request.placeOfBirth,
            address = request.address,
            companionName = request.companionName,
            // Sertakan field lain dari currentProfile
            username = currentProfile?.username,
            email = currentProfile?.email,
            phoneNumber = currentProfile?.phoneNumber,
            weight = currentProfile?.weight,
            bloodType = currentProfile?.bloodType,
            allergyHistory = currentProfile?.allergyHistory,
            medicalHistory = currentProfile?.medicalHistory,
            height = currentProfile?.height,
        )

        ApiConfig.getApiService().updatePatientProfile("Bearer $token", updateRequest).enqueue(object : Callback<GeneralResponse> {
            override fun onResponse(
                call: Call<GeneralResponse>,
                response: Response<GeneralResponse>
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _isUpdateSuccess.value = true
                    _errorMessage.value = null
                    getPatientProfile(token)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("ProfileViewModel", "Update Patient Info Failed: ${response.code()} - ${response.message()} - $errorBody")
                    _isUpdateSuccess.value = false
                    _errorMessage.value = "Gagal memperbarui informasi pasien: ${response.message()}"
                }
            }

            override fun onFailure(call: Call<GeneralResponse>, t: Throwable) {
                _isLoading.value = false
                _isUpdateSuccess.value = false
                Log.e("ProfileViewModel", "Update Patient Info Error: ${t.message.toString()}")
                _errorMessage.value = "Koneksi gagal: ${t.message}"
            }
        })
    }

    // Fungsi untuk memperbarui informasi kesehatan
    fun updateHealthInfo(token: String, request: HealthInfoRequest) {
        _isLoading.value = true
        _isUpdateSuccess.value = false
        _errorMessage.value = null

        val currentProfile = _patientProfile.value

        val updateRequest = ProfileUpdateRequest(
            height = request.height,
            weight = request.weight,
            bloodType = request.bloodType,
            medicalHistory = request.medicalHistory,
            allergyHistory = request.allergyHistory,
            // Sertakan field lain dari currentProfile
            username = currentProfile?.username,
            email = currentProfile?.email,
            phoneNumber = currentProfile?.phoneNumber,
            address = currentProfile?.address,
            gender = currentProfile?.gender,
            fullName = currentProfile?.fullName,
            companionName = currentProfile?.companionName,
            dateOfBirth = currentProfile?.dateOfBirth,
            placeOfBirth = currentProfile?.placeOfBirth,
        )

        ApiConfig.getApiService().updatePatientProfile("Bearer $token", updateRequest).enqueue(object : Callback<GeneralResponse> {
            override fun onResponse(
                call: Call<GeneralResponse>,
                response: Response<GeneralResponse>
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _isUpdateSuccess.value = true
                    _errorMessage.value = null
                    getPatientProfile(token)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("ProfileViewModel", "Update Health Info Failed: ${response.code()} - ${response.message()} - $errorBody")
                    _isUpdateSuccess.value = false
                    _errorMessage.value = "Gagal memperbarui informasi kesehatan: ${response.message()}"
                }
            }

            override fun onFailure(call: Call<GeneralResponse>, t: Throwable) {
                _isLoading.value = false
                _isUpdateSuccess.value = false
                Log.e("ProfileViewModel", "Update Health Info Error: ${t.message.toString()}")
                _errorMessage.value = "Koneksi gagal: ${t.message}"
            }
        })
    }
}