package com.mraihanfauzii.restrokotlin.ui.main.profile

import android.app.DatePickerDialog
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
import com.mraihanfauzii.restrokotlin.databinding.FragmentPatientInfoBinding
import com.mraihanfauzii.restrokotlin.model.PatientInfoRequest
import com.mraihanfauzii.restrokotlin.ui.authentication.AuthenticationManager
import com.mraihanfauzii.restrokotlin.viewmodel.ProfileViewModel
import java.text.SimpleDateFormat
import java.util.*

class PatientInfoFragment : Fragment() {

    private var _binding: FragmentPatientInfoBinding? = null
    private val binding get() = _binding!!

    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var authenticationManager: AuthenticationManager

    private val calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPatientInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authenticationManager = AuthenticationManager(requireContext())
        profileViewModel = ViewModelProvider(requireActivity())[ProfileViewModel::class.java]

        setupDropdowns()
        setupListeners()
        observeViewModel()
        loadData()
    }

    private fun setupDropdowns() {
        val genderOptions = arrayOf("Laki-laki", "Perempuan")
        val genderAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, genderOptions) // Buat dropdown_item.xml
        (binding.tilJenisKelamin.editText as? AutoCompleteTextView)?.setAdapter(genderAdapter)
    }

    private fun setupListeners() {
        binding.btnBackPatientInfo.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnLanjutkanPatient.setOnClickListener {
            savePatientInfo()
        }

        binding.edtTanggalLahir.setOnClickListener {
            showDatePicker()
        }
        binding.tilTanggalLahir.setEndIconOnClickListener {
            showDatePicker()
        }
    }

    private fun observeViewModel() {
        profileViewModel.patientProfile.observe(viewLifecycleOwner) { profile ->
            profile?.let {
                binding.edtNamaLengkap.setText(it.fullName)
                (binding.tilJenisKelamin.editText as? AutoCompleteTextView)?.setText(it.gender, false)
                binding.edtTanggalLahir.setText(it.dateOfBirth) // Pastikan format tanggal sesuai
                binding.edtTempatLahir.setText(it.placeOfBirth)
                binding.edtAlamat.setText(it.address)
                binding.edtNamaPendamping.setText(it.companionName)
            }
        }

        profileViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.btnLanjutkanPatient.isEnabled = !isLoading
            binding.btnKembaliPatient.isEnabled = !isLoading
        }

        profileViewModel.isUpdateSuccess.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                Toast.makeText(requireContext(), "Informasi Pasien berhasil diperbarui", Toast.LENGTH_SHORT).show()
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

    private fun savePatientInfo() {
        val fullName = binding.edtNamaLengkap.text.toString().trim()
        val gender = (binding.tilJenisKelamin.editText as? AutoCompleteTextView)?.text.toString().trim()
        val dateOfBirth = binding.edtTanggalLahir.text.toString().trim() // Pastikan format sesuai backend
        val placeOfBirth = binding.edtTempatLahir.text.toString().trim()
        val address = binding.edtAlamat.text.toString().trim()
        val companionName = binding.edtNamaPendamping.text.toString().trim()

        if (fullName.isEmpty() || gender.isEmpty() || dateOfBirth.isEmpty() || placeOfBirth.isEmpty() || address.isEmpty()) {
            Toast.makeText(requireContext(), "Semua bidang harus diisi", Toast.LENGTH_SHORT).show()
            return
        }

        val request = PatientInfoRequest(fullName, gender, dateOfBirth, placeOfBirth, address, companionName)
        val token = authenticationManager.getAccess(AuthenticationManager.TOKEN)

        if (token != null) {
            profileViewModel.updatePatientInfo(token, request)
        } else {
            Toast.makeText(requireContext(), "Token tidak ditemukan, silakan login ulang.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDatePicker() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, monthOfYear)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }

        DatePickerDialog(
            requireContext(),
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateDateInView() {
        // Sesuaikan format tanggal dengan yang diharapkan oleh backend Anda
        // Contoh: "yyyy-MM-dd"
        val myFormat = "yyyy-MM-dd"
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        binding.edtTanggalLahir.setText(sdf.format(calendar.time))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}