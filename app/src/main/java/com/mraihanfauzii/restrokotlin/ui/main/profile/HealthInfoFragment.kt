package com.mraihanfauzii.restrokotlin.ui.main.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.mraihanfauzii.restrokotlin.R
import com.mraihanfauzii.restrokotlin.databinding.FragmentHealthInfoBinding
import com.mraihanfauzii.restrokotlin.model.HealthInfoRequest
import com.mraihanfauzii.restrokotlin.ui.authentication.AuthenticationManager
import com.mraihanfauzii.restrokotlin.viewmodel.ProfileViewModel
import com.mraihanfauzii.restrokotlin.api.ApiConfig // Import ApiConfig
import com.mraihanfauzii.restrokotlin.viewmodel.ProfileViewModelFactory // Import ProfileViewModelFactory

class HealthInfoFragment : Fragment() {

    private var _binding: FragmentHealthInfoBinding? = null
    private val binding get() = _binding!!

    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var authenticationManager: AuthenticationManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHealthInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authenticationManager = AuthenticationManager(requireContext())
        // Menggunakan ProfileViewModelFactory untuk menginisialisasi ProfileViewModel
        val apiService = ApiConfig.getApiService()
        val factory = ProfileViewModelFactory(apiService)
        profileViewModel = ViewModelProvider(requireActivity(), factory)[ProfileViewModel::class.java]

        setupDropdowns()
        setupListeners()
        observeViewModel()
        loadData()
    }

    private fun setupDropdowns() {
        val bloodTypeOptions = arrayOf("A", "B", "AB", "O")
        val bloodTypeAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, bloodTypeOptions)
        (binding.tilGolonganDarah.editText as? AutoCompleteTextView)?.setAdapter(bloodTypeAdapter)
    }

    private fun setupListeners() {
        binding.btnBackHealthInfo.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnSimpanHealth.setOnClickListener {
            saveHealthInfo()
        }
    }

    private fun observeViewModel() {
        profileViewModel.patientProfile.observe(viewLifecycleOwner) { profile ->
            profile?.let {
                binding.edtTinggiBadan.setText(it.height?.toString() ?: "")
                binding.edtBeratBadan.setText(it.weight?.toString() ?: "")
                (binding.tilGolonganDarah.editText as? AutoCompleteTextView)?.setText(it.bloodType, false)
                binding.edtRiwayatMedis.setText(it.medicalHistory)
                binding.edtRiwayatAlergi.setText(it.allergyHistory)
            }
        }

        profileViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.btnSimpanHealth.isEnabled = !isLoading
            binding.btnKembaliHealth.isEnabled = !isLoading
        }

        profileViewModel.isUpdateSuccess.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                Toast.makeText(requireContext(), "Informasi Kesehatan berhasil diperbarui", Toast.LENGTH_SHORT).show()
                val updatedProfile = profileViewModel.patientProfile.value
                updatedProfile?.let {
                    authenticationManager.updateProfileData(it)
                    profileViewModel.setProfileUpdated(it)
                    profileViewModel.resetNeedsRefresh()
                }
                findNavController().popBackStack()
                profileViewModel.resetUpdateStatus()
            }
        }

        profileViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                profileViewModel.clearErrorMessage()
            }
        }
    }

    private fun loadData() {
        val token = authenticationManager.getAccess(AuthenticationManager.TOKEN)
        if (token != null) {
            profileViewModel.getPatientProfile(token)
        } else {
            Toast.makeText(requireContext(), "Token tidak ditemukan, silakan login ulang.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveHealthInfo() {
        val height = binding.edtTinggiBadan.text.toString().trim().toIntOrNull()
        val weight = binding.edtBeratBadan.text.toString().trim().toDoubleOrNull()
        val bloodType = (binding.tilGolonganDarah.editText as? AutoCompleteTextView)?.text.toString().trim()
        val medicalHistory = binding.edtRiwayatMedis.text.toString().trim()
        val allergyHistory = binding.edtRiwayatAlergi.text.toString().trim()

        if (height == null || weight == null || bloodType.isEmpty()) { // Riwayat medis/alergi bisa kosong
            Toast.makeText(requireContext(), "Tinggi, Berat, dan Golongan Darah harus diisi dengan benar", Toast.LENGTH_SHORT).show()
            return
        }

        val request = HealthInfoRequest(height, weight, bloodType, medicalHistory, allergyHistory)
        val token = authenticationManager.getAccess(AuthenticationManager.TOKEN)

        if (token != null) {
            profileViewModel.updateHealthInfo(token, request)
        } else {
            Toast.makeText(requireContext(), "Token tidak ditemukan, silakan login ulang.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}