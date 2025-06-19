package com.mraihanfauzii.restrokotlin.ui.authentication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.mraihanfauzii.restrokotlin.ui.MainActivity
import com.mraihanfauzii.restrokotlin.databinding.ActivityLoginBinding
import com.mraihanfauzii.restrokotlin.model.LoginRequest
import com.mraihanfauzii.restrokotlin.viewmodel.AuthenticationViewModel

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var authenticationViewModel: AuthenticationViewModel
    private lateinit var authenticationManager: AuthenticationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authenticationManager = AuthenticationManager(this)
        authenticationViewModel = ViewModelProvider(this)[AuthenticationViewModel::class.java]

        authenticationViewModel.isLoading.observe(this@LoginActivity) { loading ->
            showLoading(loading)
        }
        authenticationViewModel.errorMessage.observe(this, Observer { errorMessage ->
            if (errorMessage != null) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
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
                    login(authenticationViewModel)
                } else if (email.isEmpty() && password.isEmpty()){
                    Toast.makeText(this@LoginActivity, "Email dan Password harus diisi!", Toast.LENGTH_SHORT).show()
                } else if (email.isEmpty()) {
                    Toast.makeText(this@LoginActivity, "Email harus diisi!", Toast.LENGTH_SHORT).show()
                } else if (password.isEmpty()) {
                    Toast.makeText(this@LoginActivity, "Password harus diisi!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun login(authenticationViewModel: AuthenticationViewModel) {
        authenticationViewModel.isLoginSuccess.observe(this@LoginActivity) { isSuccess ->
            if(isSuccess) {
                val loginResponse = authenticationViewModel.loginResponse.value
                loginResponse?.let {
                    authenticationManager.apply {
                        setSession(AuthenticationManager.SESSION, true)
                        login(AuthenticationManager.TOKEN, it.accessToken)
                        loginInt(AuthenticationManager.ID, it.user.id)
                        login(AuthenticationManager.EMAIL, it.user.email)
                        login(AuthenticationManager.USERNAME, it.user.username)
                        login(AuthenticationManager.NAMALENGKAP, it.user.namaLengkap)
                        login(AuthenticationManager.ROLE, it.user.role)
                    }
                }
                Toast.makeText(this@LoginActivity, "Successfully login", Toast.LENGTH_SHORT).show()
                navigateToMainActivity()
            } else {
                Toast.makeText(this@LoginActivity, "Email dan password tidak sesuai!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun showLoading(state: Boolean) {
        binding.progressBar.visibility = if (state) View.VISIBLE else View.GONE
    }
}