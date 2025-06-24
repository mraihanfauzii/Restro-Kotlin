package com.mraihanfauzii.restrokotlin.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mraihanfauzii.restrokotlin.api.ApiConfig
import com.mraihanfauzii.restrokotlin.model.DeleteResponse
import com.mraihanfauzii.restrokotlin.model.LoginRequest
import com.mraihanfauzii.restrokotlin.model.LoginResponse
import com.mraihanfauzii.restrokotlin.model.RegisterRequest
import com.mraihanfauzii.restrokotlin.model.RegisterResponse
import kotlinx.coroutines.launch
import retrofit2.HttpException

class AuthenticationViewModel : ViewModel() {
    private val _registerResponse = MutableLiveData<RegisterResponse>()
    val registerResponse : LiveData<RegisterResponse> = _registerResponse

    private val _loginResponse = MutableLiveData<LoginResponse>()
    val loginResponse : LiveData<LoginResponse> = _loginResponse

    private val _isLoginSuccess = MutableLiveData<Boolean>()
    val isLoginSuccess: LiveData<Boolean> = _isLoginSuccess

    private val _isRegisterSuccess = MutableLiveData<Boolean>()
    val isRegisterSuccess: LiveData<Boolean> = _isRegisterSuccess

    private val _logoutResponse = MutableLiveData<DeleteResponse>()
    val logoutResponse : LiveData<DeleteResponse> = _logoutResponse

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    fun register(registerRequest: RegisterRequest){
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = ApiConfig.getApiService().register(registerRequest)
                _registerResponse.value = response
                _isRegisterSuccess.value = true
                _errorMessage.value = null
            } catch (e: HttpException) { // Menggunakan HttpException
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("AuthViewModel", "Register HTTP Error: ${e.code()} - $errorBody")
                _isRegisterSuccess.value = false
                _errorMessage.value = "Email atau username sudah terdaftar!" // Atau parse errorBody
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Register Error: ${e.message.toString()}")
                _isRegisterSuccess.value = false
                _errorMessage.value = "Gagal mendaftar: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun login(loginRequest: LoginRequest){
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = ApiConfig.getApiService().login(loginRequest)
                _loginResponse.value = response
                _isLoginSuccess.value = true
                _errorMessage.value = null
            } catch (e: HttpException) { // Menggunakan HttpException
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("AuthViewModel", "Login HTTP Error: ${e.code()} - $errorBody")
                _isLoginSuccess.value = false
                _errorMessage.value = "Email dan password tidak sesuai!" // Atau parse errorBody
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Login Error: ${e.message.toString()}")
                _isLoginSuccess.value = false
                _errorMessage.value = "Gagal login: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout(token: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = ApiConfig.getApiService().logout("Bearer $token") // Pastikan format header
                _logoutResponse.value = response
                _errorMessage.value = null
            } catch (e: HttpException) { // Menggunakan HttpException
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("AuthViewModel", "Logout HTTP Error: ${e.code()} - $errorBody")
                _errorMessage.value = "Gagal logout: ${e.message}"
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Logout Error: ${e.message.toString()}")
                _errorMessage.value = "Gagal logout: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}