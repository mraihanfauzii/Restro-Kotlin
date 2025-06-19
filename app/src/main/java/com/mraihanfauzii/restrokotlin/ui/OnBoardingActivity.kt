package com.mraihanfauzii.restrokotlin.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mraihanfauzii.restrokotlin.databinding.ActivityOnBoardingBinding
import com.mraihanfauzii.restrokotlin.ui.authentication.LoginActivity

class OnBoardingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOnBoardingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnBoardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.button.setOnClickListener {
            val login = Intent(this@OnBoardingActivity, LoginActivity::class.java)
            startActivity(login)
            finish()
        }
    }
}