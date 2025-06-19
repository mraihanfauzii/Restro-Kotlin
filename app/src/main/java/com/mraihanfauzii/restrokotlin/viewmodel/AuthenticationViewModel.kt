package com.mraihanfauzii.restrokotlin.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mraihanfauzii.restrokotlin.api.ApiConfig
import com.mraihanfauzii.restrokotlin.model.DeleteResponse
import com.mraihanfauzii.restrokotlin.model.LoginRequest
import com.mraihanfauzii.restrokotlin.model.LoginResponse
import com.mraihanfauzii.restrokotlin.model.RegisterRequest
import com.mraihanfauzii.restrokotlin.model.RegisterResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
        ApiConfig.getApiService().register(registerRequest).enqueue(object :
            Callback<RegisterResponse> {
            override fun onResponse(
                call: Call<RegisterResponse>,
                response: Response<RegisterResponse>
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _registerResponse.value = response.body()
                    _isRegisterSuccess.value = true
                    _errorMessage.value = null
                } else {
                    Log.e("RegisterViewModel", "onFailure: ${response.message()}")
                    _registerResponse.value = response.body()
                    _isRegisterSuccess.value = false
                    _errorMessage.value = "Email atau username sudah terdaftar!"
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                _isLoading.value = false
                Log.e("RegisterViewModel", "onFailure: ${t.message.toString()}")
                _isRegisterSuccess.value = false
                _errorMessage.value = "Failed to register"
            }
        })
    }

    fun login(loginRequest: LoginRequest){
        _isLoading.value = true
        ApiConfig.getApiService().login(loginRequest).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _loginResponse.value = response.body()
                    _isLoginSuccess.value = true
                    _errorMessage.value = null
                } else {
                    Log.e("LoginViewModel", "onFailure: ${response.message()}")
                    _loginResponse.value = response.body()
                    _isLoginSuccess.value = false
                    _errorMessage.value = "Email dan password tidak sesuai!"
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                _isLoading.value = false
                _isLoginSuccess.value = false
                Log.e("LoginViewModel", "onFailure: ${t.message.toString()}")
                _errorMessage.value = "Failed to login"
            }
        })
    }

    fun logout(token: String) {
        _isLoading.value = true
        ApiConfig.getApiService().logout(token).enqueue(object : Callback<DeleteResponse> {
            override fun onResponse(
                call: Call<DeleteResponse>,
                response: Response<DeleteResponse>
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _logoutResponse.value = response.body()
                    _errorMessage.value = null
                } else {
                    Log.e("LoginViewModel", "onFailure: ${response.message()}")
                    _logoutResponse.value = response.body()
                    _errorMessage.value = "Failed to login"
                }
            }

            override fun onFailure(call: Call<DeleteResponse>, t: Throwable) {
                _isLoading.value = false
                Log.e("LoginViewModel", "onFailure: ${t.message.toString()}")
                _errorMessage.value = "Failed to login"
            }
        })
    }
}