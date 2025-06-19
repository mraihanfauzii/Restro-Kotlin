package com.mraihanfauzii.restrokotlin.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.ui.setupWithNavController
import com.mraihanfauzii.restrokotlin.R
import com.mraihanfauzii.restrokotlin.databinding.ActivityMainBinding
import androidx.navigation.findNavController

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navController = this.findNavController(R.id.activityMainNavHostFragment)
        binding.bottomNavigationView.setupWithNavController(navController)

        // Check if there's an intent to navigate to a specific fragment
        val navigateTo = intent.getStringExtra("navigate_to")
        if (navigateTo == "ProfileFragment") {
            navController.navigate(R.id.profileFragment)
        }
    }
}