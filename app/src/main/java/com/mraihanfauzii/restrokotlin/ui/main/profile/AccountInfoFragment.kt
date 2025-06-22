package com.mraihanfauzii.restrokotlin.ui.main.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.mraihanfauzii.restrokotlin.databinding.FragmentAccountInfoBinding
import com.mraihanfauzii.restrokotlin.model.AccountInfoRequest
import com.mraihanfauzii.restrokotlin.ui.authentication.AuthenticationManager
import com.mraihanfauzii.restrokotlin.viewmodel.ProfileViewModel

class AccountInfoFragment : Fragment() {

    private var _binding: FragmentAccountInfoBinding? = null
    private val binding get() = _binding!!

    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var authenticationManager: AuthenticationManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAccountInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authenticationManager = AuthenticationManager(requireContext())
        // Inisialisasi ViewModel yang dibagikan dengan scope Activity atau NavGraph
        // Untuk contoh ini, saya akan menggunakan activityViewModels()
        // Pastikan Anda sudah menambahkan implementasi 'androidx.fragment:fragment-ktx:1.x.x'
        // di build.gradle jika menggunakan activityViewModels()
        profileViewModel = ViewModelProvider(requireActivity())[ProfileViewModel::class.java]

        setupListeners()
        observeViewModel()
        loadData()
    }

    private fun setupListeners() {
        binding.btnBackAccountInfo.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnLanjutkanAccount.setOnClickListener {
            saveAccountInfo()
        }
    }

    private fun observeViewModel() {
        // Amati data profil dari ViewModel
        profileViewModel.patientProfile.observe(viewLifecycleOwner) { profile ->
            profile?.let {
                binding.edtUsername.setText(it.username)
                binding.edtEmail.setText(it.email)
                binding.edtPhone.setText(it.phoneNumber)
            }
        }

        // Amati status loading
        profileViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Tampilkan/sembunyikan progress bar jika ada
            // binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnLanjutkanAccount.isEnabled = !isLoading // Disable tombol saat loading
            binding.btnKembaliAccount.isEnabled = !isLoading
        }

        // Amati status update
        profileViewModel.isUpdateSuccess.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                Toast.makeText(requireContext(), "Informasi Akun berhasil diperbarui", Toast.LENGTH_SHORT).show()
                // Navigasi kembali setelah berhasil
                findNavController().popBackStack()
                // Penting: Setel ulang isUpdateSuccess agar tidak memicu lagi saat rotasi/kembali
                profileViewModel.resetUpdateStatus()
            }
        }

        // Amati pesan error
        profileViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                profileViewModel.clearErrorMessage()
            }
        }
    }

    private fun loadData() {
        // Ambil token dari AuthenticationManager
        val token = authenticationManager.getAccess(AuthenticationManager.TOKEN)
        if (token != null) {
            profileViewModel.getPatientProfile(token)
        } else {
            Toast.makeText(requireContext(), "Token tidak ditemukan, silakan login ulang.", Toast.LENGTH_SHORT).show()
            // Mungkin arahkan ke layar login
        }
    }

    private fun saveAccountInfo() {
        val username = binding.edtUsername.text.toString().trim()
        val email = binding.edtEmail.text.toString().trim()
        val phoneNumber = binding.edtPhone.text.toString().trim()

        if (username.isEmpty() || email.isEmpty() || phoneNumber.isEmpty()) {
            Toast.makeText(requireContext(), "Semua bidang harus diisi", Toast.LENGTH_SHORT).show()
            return
        }

        val request = AccountInfoRequest(username, email, phoneNumber)
        val token = authenticationManager.getAccess(AuthenticationManager.TOKEN)

        if (token != null) {
            profileViewModel.updateAccountInfo(token, request)
        } else {
            Toast.makeText(requireContext(), "Token tidak ditemukan, silakan login ulang.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}