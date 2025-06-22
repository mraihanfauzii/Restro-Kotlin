package com.mraihanfauzii.restrokotlin.ui.main.profile

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
import com.mraihanfauzii.restrokotlin.viewmodel.ProfileViewModel
import com.bumptech.glide.Glide

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
        profileViewModel = ViewModelProvider(requireActivity())[ProfileViewModel::class.java]

        setupListeners()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        // Panggil GET setiap kali ProfileFragment resume untuk memastikan data terbaru
        val token = authenticationManager.getAccess(AuthenticationManager.TOKEN)
        if (token != null) {
            profileViewModel.getPatientProfile(token)
        } else {
            Toast.makeText(requireContext(), "Token tidak ditemukan, silakan login ulang.", Toast.LENGTH_SHORT).show()
            // TODO: Arahkan ke layar login jika token tidak ada
        }
    }

    private fun setupListeners() {
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
            // Implementasi logout Anda (panggil ViewModel autentikasi, hapus sesi, dll.)
            // Contoh: authenticationManager.logOut()
            // Setelah logout, arahkan ke layar OnBoarding/Login
            // val intent = Intent(requireActivity(), OnBoardingActivity::class.java)
            // intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // startActivity(intent)
            // requireActivity().finish()
            Toast.makeText(requireContext(), "Logout clicked! (Implementasi pending)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        profileViewModel.patientProfile.observe(viewLifecycleOwner) { profile ->
            profile?.let {
                binding.tvProfileName.text = it.fullName ?: "Muhammad Raihan Fauzi"
                binding.tvProfileUsername.text = it.username ?: "mraihanf"

                // Muat gambar profil jika URL tersedia
                if (!it.profilePictureUrl.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(it.profilePictureUrl)
                        .placeholder(R.drawable.baseline_person_24) // Gambar placeholder
                        .error(R.drawable.baseline_error_24) // Gambar error
                        .into(binding.ivProfileAvatar)
                } else {
                    binding.ivProfileAvatar.setImageResource(R.drawable.baseline_person_24)
                }
            }
        }

        profileViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Tampilkan/sembunyikan indikator loading di ProfileFragment jika ada
            // Misalnya, ProgressBar di atas ImageView atau di tengah layout
        }

        profileViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                profileViewModel.clearErrorMessage()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}