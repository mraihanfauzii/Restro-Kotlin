package com.mraihanfauzii.restrokotlin.ui

import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.ui.setupWithNavController
import com.mraihanfauzii.restrokotlin.R
import com.mraihanfauzii.restrokotlin.databinding.ActivityMainBinding
import androidx.navigation.findNavController
import androidx.core.view.isGone
import androidx.core.view.isVisible

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navController = this.findNavController(R.id.activityMainNavHostFragment)
        binding.bottomNavigationView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment,
                R.id.calendarFragment,
                R.id.chatFragment,
                R.id.foodFragment,
                R.id.profileFragment -> {
                    if (binding.bottomNavigationView.isGone) {
                        binding.bottomNavigationView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_up_bottom_nav))
                        binding.bottomNavigationView.visibility = View.VISIBLE
                    }
                } else -> {
                    if (binding.bottomNavigationView.isVisible) {
                        binding.bottomNavigationView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_down_bottom_nav))
                        binding.bottomNavigationView.visibility = View.GONE
                    }
                }
            }
        }
    }
}