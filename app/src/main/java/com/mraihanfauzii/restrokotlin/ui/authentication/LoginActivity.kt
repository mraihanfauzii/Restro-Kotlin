package com.mraihanfauzii.restrokotlin.ui.authentication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.mraihanfauzii.restrokotlin.ui.MainActivity
import com.mraihanfauzii.restrokotlin.databinding.ActivityLoginBinding
import com.mraihanfauzii.restrokotlin.model.LoginRequest
import com.mraihanfauzii.restrokotlin.viewmodel.AuthenticationViewModel
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var authenticationViewModel: AuthenticationViewModel
    private lateinit var authenticationManager: AuthenticationManager
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authenticationManager = AuthenticationManager(this)
        authenticationViewModel = ViewModelProvider(this)[AuthenticationViewModel::class.java]
        firebaseAuth = FirebaseAuth.getInstance()

        if (authenticationManager.checkSession(AuthenticationManager.SESSION)) {
            navigateToMainActivity()
            return
        }


        authenticationViewModel.isLoading.observe(this@LoginActivity) { loading ->
            showLoading(loading)
        }
        authenticationViewModel.errorMessage.observe(this, Observer { errorMessage ->
            if (errorMessage != null) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                authenticationViewModel.clearErrorMessage()
            }
        })

        binding.apply {
            btnRegister.setOnClickListener {
                val register = Intent(this@LoginActivity, RegisterActivity::class.java)
                startActivity(register)
            }

            btnLogin.setOnClickListener {
                val email = edtEmail.text.toString().trim()
                val password = edtPassword.text.toString().trim()
                val loginRequest = LoginRequest(
                    identifier = email,
                    password = password,
                )
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    authenticationViewModel.login(loginRequest)
                } else if (email.isEmpty() && password.isEmpty()){
                    Toast.makeText(this@LoginActivity, "Email dan Password harus diisi!", Toast.LENGTH_SHORT).show()
                } else if (email.isEmpty()) {
                    Toast.makeText(this@LoginActivity, "Email harus diisi!", Toast.LENGTH_SHORT).show()
                } else if (password.isEmpty()) {
                    Toast.makeText(this@LoginActivity, "Password harus diisi!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        authenticationViewModel.isLoginSuccess.observe(this@LoginActivity) { isSuccess ->
            if(isSuccess) {
                val loginResponse = authenticationViewModel.loginResponse.value
                loginResponse?.let { azureResponse ->
                    authenticationManager.saveLoginResponseData(azureResponse)

                    authenticationManager.setCameraPermissionToastShown(false)

                    val firebaseCustomToken = azureResponse.firebaseCustomToken

                    if (firebaseCustomToken.isNotEmpty()) {
                        firebaseAuth.signInWithCustomToken(firebaseCustomToken)
                            .addOnCompleteListener(this@LoginActivity) { task ->
                                if (task.isSuccessful) {
                                    val firebaseUser = firebaseAuth.currentUser
                                    firebaseUser?.uid?.let { firebaseUid ->
                                        authenticationManager.saveAccess(AuthenticationManager.FIREBASE_UID, firebaseUid)
                                        Log.d("LoginActivity", "Successfully signed in to Firebase with custom token. UID: $firebaseUid")
                                        Toast.makeText(this@LoginActivity, "Login berhasil dan Firebase diintegrasikan.", Toast.LENGTH_SHORT).show()
                                        navigateToMainActivity()
                                    } ?: run {
                                        Log.e("LoginActivity", "Firebase user UID is null after custom token sign-in. This shouldn't happen if sign-in was successful.")
                                        Toast.makeText(this@LoginActivity, "Login berhasil, tapi gagal mendapatkan UID Firebase.", Toast.LENGTH_LONG).show()
                                        navigateToMainActivity()
                                    }
                                } else {
                                    Log.e("LoginActivity", "Firebase custom token sign-in failed. Error: ${task.exception?.message}")
                                    Toast.makeText(this@LoginActivity, "Login berhasil ke Azure, namun gagal mengautentikasi Firebase. Fitur chat terapis mungkin tidak berfungsi. Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                    navigateToMainActivity()
                                }
                            }
                    } else {
                        Log.e("LoginActivity", "Firebase custom token is empty from backend response.")
                        Toast.makeText(this@LoginActivity, "Login berhasil ke Azure, namun token Firebase tidak ditemukan.", Toast.LENGTH_SHORT).show()
                        navigateToMainActivity()
                    }
                }
            }
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showLoading(state: Boolean) {
        binding.progressBar.visibility = if (state) View.VISIBLE else View.GONE
    }
}