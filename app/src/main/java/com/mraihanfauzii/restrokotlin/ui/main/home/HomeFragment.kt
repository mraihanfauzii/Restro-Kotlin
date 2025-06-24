package com.mraihanfauzii.restrokotlin.ui.main.home

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.mraihanfauzii.restrokotlin.R
import com.mraihanfauzii.restrokotlin.databinding.FragmentHomeBinding
import com.mraihanfauzii.restrokotlin.ui.authentication.AuthenticationManager // Import ini

class HomeFragment : Fragment() {

    private val REQUIRED_PERMISSIONS = arrayOf(android.Manifest.permission.CAMERA)
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var authenticationManager: AuthenticationManager

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            if (!authenticationManager.isCameraPermissionToastShown()) {
                Toast.makeText(requireContext(), "Izin kamera diberikan.", Toast.LENGTH_SHORT).show()
                authenticationManager.setCameraPermissionToastShown(true)
            }
        } else {
            Toast.makeText(requireContext(), "Izin kamera ditolak. Beberapa fitur mungkin tidak berfungsi.", Toast.LENGTH_LONG).show()
            authenticationManager.setCameraPermissionToastShown(false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        authenticationManager = AuthenticationManager(requireContext())
        checkAndRequestPermissions()

        binding.btnLanjutkanProgram.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_debugFragment)
        }
    }

    private fun checkAndRequestPermissions() {
        if (!allPermissionsGranted()) {
            requestPermissionLauncher.launch(REQUIRED_PERMISSIONS)
        } else {
            if (!authenticationManager.isCameraPermissionToastShown()) {
                Toast.makeText(requireContext(), "Izin kamera sudah diberikan.", Toast.LENGTH_SHORT).show()
                authenticationManager.setCameraPermissionToastShown(true)
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}