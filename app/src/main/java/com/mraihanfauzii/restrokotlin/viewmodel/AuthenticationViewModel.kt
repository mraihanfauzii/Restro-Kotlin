//package com.mraihanfauzii.restrokotlin.viewmodel
//
//import android.util.Log
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.ViewModel
//import com.hackathon.devlabsuser.api.ApiConfig
//import com.hackathon.devlabsuser.model.ApiResponse
//import com.hackathon.devlabsuser.model.DeleteResponse
//import com.hackathon.devlabsuser.model.LoginData
//import com.hackathon.devlabsuser.model.LoginRequest
//import com.hackathon.devlabsuser.model.RegisterData
//import com.hackathon.devlabsuser.model.RegisterRequest
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//
//class AuthenticationViewModel : ViewModel() {
//    private val _registerResponse = MutableLiveData<ApiResponse<RegisterData>>()
//    val registerResponse : LiveData<ApiResponse<RegisterData>> = _registerResponse
//
//    private val _loginResponse = MutableLiveData<ApiResponse<LoginData>>()
//    val loginResponse : LiveData<ApiResponse<LoginData>> = _loginResponse
//
//    private val _logoutResponse = MutableLiveData<DeleteResponse>()
//    val logoutResponse : LiveData<DeleteResponse> = _logoutResponse
//
//    private val _isLoading = MutableLiveData<Boolean>()
//    val isLoading: LiveData<Boolean> = _isLoading
//
//    private val _errorMessage = MutableLiveData<String>()
//    val errorMessage: LiveData<String> get() = _errorMessage
//
//    fun register(registerRequest: RegisterRequest){
//        _isLoading.value = true
//        ApiConfig.getApiService().register(registerRequest).enqueue(object :
//            Callback<ApiResponse<RegisterData>> {
//            override fun onResponse(
//                call: Call<ApiResponse<RegisterData>>,
//                response: Response<ApiResponse<RegisterData>>
//            ) {
//                _isLoading.value = false
//                if (response.isSuccessful) {
//                    _registerResponse.value = response.body()
//                } else {
//                    Log.e("RegisterViewModel", "onFailure: ${response.message()}")
//                    _registerResponse.value = response.body()
//                    _errorMessage.value = "Failed to register"
//                }
//            }
//
//            override fun onFailure(call: Call<ApiResponse<RegisterData>>, t: Throwable) {
//                _isLoading.value = false
//                Log.e("RegisterViewModel", "onFailure: ${t.message.toString()}")
//                _errorMessage.value = "Failed to register"
//            }
//        })
//    }
//
//    fun login(loginRequest: LoginRequest){
//        _isLoading.value = true
//        ApiConfig.getApiService().login(loginRequest).enqueue(object : Callback<ApiResponse<LoginData>> {
//            override fun onResponse(
//                call: Call<ApiResponse<LoginData>>,
//                response: Response<ApiResponse<LoginData>>
//            ) {
//                _isLoading.value = false
//                if (response.isSuccessful) {
//                    _loginResponse.value = response.body()
//                } else {
//                    Log.e("LoginViewModel", "onFailure: ${response.message()}")
//                    _loginResponse.value = response.body()
//                    _errorMessage.value = "Email dan password tidak sesuai!"
//                }
//            }
//
//            override fun onFailure(call: Call<ApiResponse<LoginData>>, t: Throwable) {
//                _isLoading.value = false
//                Log.e("LoginViewModel", "onFailure: ${t.message.toString()}")
//                _errorMessage.value = "Failed to login"
//            }
//        })
//    }
//
//    fun logout(token: String) {
//        _isLoading.value = true
//        ApiConfig.getApiService().logout(token).enqueue(object : Callback<DeleteResponse> {
//            override fun onResponse(
//                call: Call<DeleteResponse>,
//                response: Response<DeleteResponse>
//            ) {
//                _isLoading.value = false
//                if (response.isSuccessful) {
//                    _logoutResponse.value = response.body()
//                } else {
//                    Log.e("LoginViewModel", "onFailure: ${response.message()}")
//                    _logoutResponse.value = response.body()
//                    _errorMessage.value = "Failed to login"
//                }
//            }
//
//            override fun onFailure(call: Call<DeleteResponse>, t: Throwable) {
//                _isLoading.value = false
//                Log.e("LoginViewModel", "onFailure: ${t.message.toString()}")
//                _errorMessage.value = "Failed to login"
//            }
//        })
//    }
//}