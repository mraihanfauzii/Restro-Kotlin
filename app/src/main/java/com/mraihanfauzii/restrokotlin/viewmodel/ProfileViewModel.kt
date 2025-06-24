package com.mraihanfauzii.restrokotlin.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mraihanfauzii.restrokotlin.api.ApiService
import com.mraihanfauzii.restrokotlin.model.*
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import java.io.File

class ProfileViewModel(private val apiService: ApiService) : ViewModel() {

    private val _patientProfile = MutableLiveData<PatientProfileResponse?>()
    val patientProfile: LiveData<PatientProfileResponse?> = _patientProfile

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isUpdateSuccess = MutableLiveData<Boolean>()
    val isUpdateSuccess: LiveData<Boolean> = _isUpdateSuccess

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _needsRefresh = MutableLiveData<Boolean>()
    val needsRefresh: LiveData<Boolean> get() = _needsRefresh

    private val _profileUpdated = MutableLiveData<PatientProfileResponse?>()
    val profileUpdated: LiveData<PatientProfileResponse?> get() = _profileUpdated

    private val _isImageUploadSuccess = MutableLiveData<Boolean>()
    val isImageUploadSuccess: LiveData<Boolean> = _isImageUploadSuccess

    private val _imageUploadMessage = MutableLiveData<String?>()
    val imageUploadMessage: LiveData<String?> = _imageUploadMessage

    fun resetUpdateStatus() {
        _isUpdateSuccess.value = false
        _isImageUploadSuccess.value = false
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
        _imageUploadMessage.value = null
    }

    fun resetNeedsRefresh() {
        _needsRefresh.value = false
    }

    fun setProfileUpdated(profile: PatientProfileResponse) {
        _profileUpdated.value = profile
    }

    fun getPatientProfile(token: String) {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val response = apiService.getPatientProfile("Bearer $token")
                _patientProfile.value = response
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val errorMessage = "GET Profile Failed: ${e.code()} - ${e.message()} - $errorBody"
                Log.e("ProfileViewModel", errorMessage)
                _errorMessage.value = "Gagal mengambil data profil: ${e.message()}"
            } catch (e: Exception) {
                val errorMessage = "GET Profile Error: ${e.message.toString()}"
                Log.e("ProfileViewModel", errorMessage)
                _errorMessage.value = "Koneksi gagal: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateAccountInfo(token: String, request: AccountInfoRequest) {
        _isLoading.value = true
        _isUpdateSuccess.value = false
        _errorMessage.value = null

        val currentProfile = _patientProfile.value

        val updateRequest = ProfileUpdateRequest(
            username = request.username,
            email = request.email,
            phoneNumber = request.phoneNumber,
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
            height = currentProfile?.height
        )

        viewModelScope.launch {
            try {
                val response = apiService.updatePatientProfile("Bearer $token", updateRequest)
                _isUpdateSuccess.value = true
                _errorMessage.value = null
                getPatientProfile(token)
                _needsRefresh.value = true
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val errorMessage = "Update Account Info Failed: ${e.code()} - ${e.message()} - $errorBody"
                Log.e("ProfileViewModel", errorMessage)
                _isUpdateSuccess.value = false
                _errorMessage.value = "Gagal memperbarui informasi akun: ${e.message()}"
            } catch (e: Exception) {
                val errorMessage = "Update Account Info Error: ${e.message.toString()}"
                Log.e("ProfileViewModel", errorMessage)
                _isLoading.value = false
                _isUpdateSuccess.value = false
                _errorMessage.value = "Koneksi gagal: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

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
            username = currentProfile?.username,
            email = currentProfile?.email,
            phoneNumber = currentProfile?.phoneNumber,
            weight = currentProfile?.weight,
            bloodType = currentProfile?.bloodType,
            allergyHistory = currentProfile?.allergyHistory,
            medicalHistory = currentProfile?.medicalHistory,
            height = currentProfile?.height
        )

        viewModelScope.launch {
            try {
                val response = apiService.updatePatientProfile("Bearer $token", updateRequest)
                _isUpdateSuccess.value = true
                _errorMessage.value = null
                getPatientProfile(token)
                _needsRefresh.value = true
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val errorMessage = "Update Patient Info Failed: ${e.code()} - ${e.message()} - $errorBody"
                Log.e("ProfileViewModel", errorMessage)
                _isUpdateSuccess.value = false
                _errorMessage.value = "Gagal memperbarui informasi pasien: ${e.message()}"
            } catch (e: Exception) {
                val errorMessage = "Update Patient Info Error: ${e.message.toString()}"
                Log.e("ProfileViewModel", errorMessage)
                _isLoading.value = false
                _isUpdateSuccess.value = false
                _errorMessage.value = "Koneksi gagal: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

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
            username = currentProfile?.username,
            email = currentProfile?.email,
            phoneNumber = currentProfile?.phoneNumber,
            address = currentProfile?.address,
            gender = currentProfile?.gender,
            fullName = currentProfile?.fullName,
            companionName = currentProfile?.companionName,
            dateOfBirth = currentProfile?.dateOfBirth,
            placeOfBirth = currentProfile?.placeOfBirth
        )

        viewModelScope.launch {
            try {
                val response = apiService.updatePatientProfile("Bearer $token", updateRequest)
                _isUpdateSuccess.value = true
                _errorMessage.value = null
                getPatientProfile(token)
                _needsRefresh.value = true
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val errorMessage = "Update Health Info Failed: ${e.code()} - ${e.message()} - $errorBody"
                Log.e("ProfileViewModel", errorMessage)
                _isUpdateSuccess.value = false
                _errorMessage.value = "Gagal memperbarui informasi kesehatan: ${e.message()}"
            } catch (e: Exception) {
                val errorMessage = "Update Health Info Error: ${e.message.toString()}"
                Log.e("ProfileViewModel", errorMessage)
                _isLoading.value = false
                _isUpdateSuccess.value = false
                _errorMessage.value = "Koneksi gagal: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun uploadProfilePicture(token: String, imageFile: File) {
        _isLoading.value = true
        _isImageUploadSuccess.value = false
        _imageUploadMessage.value = null

        val requestImageFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
        val multipartBody = MultipartBody.Part.createFormData("foto_profil", imageFile.name, requestImageFile)

        viewModelScope.launch {
            try {
                val response = apiService.uploadPatientProfilePicture("Bearer $token", multipartBody)
                _isImageUploadSuccess.value = true
                _imageUploadMessage.value = response.msg
                getPatientProfile(token)
                _needsRefresh.value = true
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val errorMessage = "Upload Profile Picture Failed: ${e.code()} - ${e.message()} - $errorBody"
                Log.e("ProfileViewModel", errorMessage)
                _isImageUploadSuccess.value = false
                _imageUploadMessage.value = "Gagal mengunggah foto profil: ${e.message()}"
            } catch (e: Exception) {
                val errorMessage = "Upload Profile Picture Error: ${e.message.toString()}"
                Log.e("ProfileViewModel", errorMessage)
                _isImageUploadSuccess.value = false
                _imageUploadMessage.value = "Koneksi gagal saat mengunggah foto: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

class ProfileViewModelFactory(private val apiService: ApiService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(apiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}