package com.mraihanfauzii.restrokotlin.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import com.mraihanfauzii.restrokotlin.R
import com.mraihanfauzii.restrokotlin.ui.authentication.AuthenticationManager
import com.mraihanfauzii.restrokotlin.utils.Setting
import com.mraihanfauzii.restrokotlin.viewmodel.SettingViewModel

class SplashScreenActivity : AppCompatActivity() {
    private lateinit var authenticationManager: AuthenticationManager
    private lateinit var settingViewModel: SettingViewModel
    private val SPLASH_TIME_OUT: Long = 1000 //1 detik
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        authenticationManager = AuthenticationManager(this)

        settingViewModel = ViewModelProvider(this, SettingViewModel.Factory(Setting(this)))[SettingViewModel::class.java]
        settingViewModel.getSetting().observe(this@SplashScreenActivity) {
            if (it) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        Handler().postDelayed({
            if (authenticationManager.checkSession(AuthenticationManager.SESSION) == true) {
                val loginSuccess = Intent(this@SplashScreenActivity, MainActivity::class.java)
                val getToken = authenticationManager.getAccess(AuthenticationManager.TOKEN).toString()
                val token = "Bearer $getToken"
                startActivity(loginSuccess)
                finishAffinity()
            } else {
                startActivity(Intent(this@SplashScreenActivity, OnBoardingActivity::class.java))
                finishAffinity()
            }
        }, SPLASH_TIME_OUT)
    }
}