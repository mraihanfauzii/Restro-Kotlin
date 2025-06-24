package com.mraihanfauzii.restrokotlin.ui.main.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.mraihanfauzii.restrokotlin.R
import com.mraihanfauzii.restrokotlin.databinding.FragmentProfileBinding
import com.mraihanfauzii.restrokotlin.ui.authentication.AuthenticationManager
import com.bumptech.glide.Glide
import com.mraihanfauzii.restrokotlin.api.ApiConfig
import com.mraihanfauzii.restrokotlin.model.PatientProfileResponse
import com.mraihanfauzii.restrokotlin.ui.OnBoardingActivity
import com.mraihanfauzii.restrokotlin.viewmodel.ProfileViewModel
import com.mraihanfauzii.restrokotlin.viewmodel.ProfileViewModelFactory

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var authenticationManager: AuthenticationManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authenticationManager = AuthenticationManager(requireContext())
        val apiService = ApiConfig.getApiService()
        val factory = ProfileViewModelFactory(apiService)
        profileViewModel = ViewModelProvider(requireActivity(), factory)[ProfileViewModel::class.java]

        setupListeners()
        observeViewModel()

        displayCachedProfileData()
    }

    override fun onResume() {
        super.onResume()
        val token = authenticationManager.getAccess(AuthenticationManager.TOKEN)
        if (token != null) {
            profileViewModel.needsRefresh.value?.let { needsRefresh ->
                if (needsRefresh) {
                    profileViewModel.getPatientProfile(token)
                    profileViewModel.resetNeedsRefresh()
                }
            } ?: run {
                if (profileViewModel.patientProfile.value == null) {
                    profileViewModel.getPatientProfile(token)
                }
            }
        } else {
            Toast.makeText(requireContext(), "Token tidak ditemukan, silakan login ulang.", Toast.LENGTH_SHORT).show()
            navigateToOnBoarding()
        }
    }

    private fun setupListeners() {
        binding.cardGamification.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_gamificationFragment)
        }

        binding.cardInfoAkun.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_accountInfoFragment)
        }

        binding.cardInfoPasien.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_patientInfoFragment)
        }

        binding.cardInfoKesehatan.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_healthInfoFragment)
        }

        binding.btnLogout.setOnClickListener {
            performLogout()
        }
    }

    private fun performLogout() {
        authenticationManager.logOut()
        Toast.makeText(requireContext(), "Berhasil keluar.", Toast.LENGTH_SHORT).show()
        navigateToOnBoarding()
    }

    private fun navigateToOnBoarding() {
        val intent = Intent(requireActivity(), OnBoardingActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    private fun displayCachedProfileData() {
        val cachedProfile = authenticationManager.getCachedPatientProfile()
        cachedProfile?.let {
            updateProfileUI(it)
        } ?: run {
            binding.tvProfileName.text = "Memuat..."
            binding.tvProfileUsername.text = "Memuat..."
            binding.ivProfileAvatar.setImageResource(R.drawable.baseline_person_24)
        }
    }

    private fun observeViewModel() {
        profileViewModel.patientProfile.observe(viewLifecycleOwner) { profile ->
            profile?.let {
                authenticationManager.updateProfileData(it)
                updateProfileUI(it)
            }
        }

        profileViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
        }

        profileViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                profileViewModel.clearErrorMessage()
            }
        }

        profileViewModel.profileUpdated.observe(viewLifecycleOwner) { updatedProfile ->
            updatedProfile?.let {
                updateProfileUI(it)
            }
        }
    }

    private fun updateProfileUI(profile: PatientProfileResponse) {
        binding.tvProfileName.text = profile.fullName ?: "Nama Lengkap"
        binding.tvProfileUsername.text = profile.username ?: "username"

        if (!profile.profilePictureUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(profile.profilePictureUrl)
                .placeholder(R.drawable.baseline_person_24)
                .error(R.drawable.baseline_error_24)
                .into(binding.ivProfileAvatar)
        } else {
            binding.ivProfileAvatar.setImageResource(R.drawable.baseline_person_24)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}