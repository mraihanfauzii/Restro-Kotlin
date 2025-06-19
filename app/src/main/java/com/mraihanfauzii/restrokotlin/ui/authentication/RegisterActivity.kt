package com.mraihanfauzii.restrokotlin.ui.authentication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.mraihanfauzii.restrokotlin.databinding.ActivityRegisterBinding
import com.mraihanfauzii.restrokotlin.model.RegisterRequest
import com.mraihanfauzii.restrokotlin.viewmodel.AuthenticationViewModel

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var authenticationViewModel: AuthenticationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authenticationViewModel = ViewModelProvider(this)[AuthenticationViewModel::class.java]
        authenticationViewModel.isLoading.observe(this@RegisterActivity) { loading ->
            showLoading(loading)
        }
        authenticationViewModel.errorMessage.observe(this, Observer { errorMessage ->
            if (errorMessage != null) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            }
        })

        binding.apply {
            btnLogin.setOnClickListener {
                val register = Intent(this@RegisterActivity, LoginActivity::class.java)
                startActivity(register)
            }

            btnRegister.setOnClickListener {
                val username = edtUserName.text.toString().trim()
                val fullName = edtNamaLengkap.text.toString()
                val email = edtEmail.text.toString().trim()
                val password = edtPassword.text.toString().trim()
                val passwordConf = edtPassConf.text.toString().trim()

                val registerRequest = RegisterRequest(
                    username = username,
                    namaLengkap = fullName,
                    email = email,
                    password = password,
                )
                if (passwordConf == password) {
                    if (email.isNotEmpty() && password.isNotEmpty() && username.isNotEmpty() && fullName.isNotEmpty() && passwordConf.isNotEmpty()) {
                        authenticationViewModel.register(registerRequest)
                        register(authenticationViewModel)
                    } else if(email.isEmpty() || password.isEmpty() || fullName.isEmpty() || passwordConf.isEmpty()) {
                        Toast.makeText(this@RegisterActivity, "Semua inputan harus diisi!", Toast.LENGTH_SHORT).show()
                    }
                } else if (password.length < 8 || passwordConf.length < 8)
                    Toast.makeText(this@RegisterActivity, "Password tidak boleh kurang dari 8 karakter!", Toast.LENGTH_SHORT).show()
                else {
                    Toast.makeText(this@RegisterActivity, "Kata sandi konfirmasi harus sama dengan kata sandi!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun register(authenticationViewModel: AuthenticationViewModel) {
        authenticationViewModel.isRegisterSuccess.observe(this@RegisterActivity) {  isSuccess ->
            if(isSuccess) {
                Toast.makeText(this@RegisterActivity, "Successfully register", Toast.LENGTH_SHORT).show()
                navigateToLoginActivity()
            } else{
                Toast.makeText(this@RegisterActivity, "Failed to register", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToLoginActivity() {
        val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun showLoading(state: Boolean) {
        binding.progressBar.visibility = if (state) View.VISIBLE else View.GONE
    }
}