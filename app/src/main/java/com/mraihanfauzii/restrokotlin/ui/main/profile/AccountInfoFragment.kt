package com.mraihanfauzii.restrokotlin.ui.main.profile

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.mraihanfauzii.restrokotlin.R
import com.mraihanfauzii.restrokotlin.api.ApiConfig
import com.mraihanfauzii.restrokotlin.databinding.FragmentAccountInfoBinding
import com.mraihanfauzii.restrokotlin.model.AccountInfoRequest
import com.mraihanfauzii.restrokotlin.ui.authentication.AuthenticationManager
import com.mraihanfauzii.restrokotlin.viewmodel.ProfileViewModel
import com.mraihanfauzii.restrokotlin.viewmodel.ProfileViewModelFactory
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class AccountInfoFragment : Fragment() {

    private var _binding: FragmentAccountInfoBinding? = null
    private val binding get() = _binding!!

    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var authenticationManager: AuthenticationManager

    private lateinit var currentPhotoPath: String
    private var getFile: File? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            showImagePickerDialog()
        } else {
            Toast.makeText(
                requireContext(),
                "Izin akses kamera atau penyimpanan tidak diberikan.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val myFile = File(currentPhotoPath)
            getFile = myFile
            Glide.with(this).load(myFile).into(binding.ivProfileAvatarEdit)
            uploadImageToServer()
        }
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedImg: Uri = result.data?.data as Uri
            val myFile = uriToFile(selectedImg, requireContext())
            getFile = myFile
            Glide.with(this).load(selectedImg).into(binding.ivProfileAvatarEdit)
            uploadImageToServer()
        }
    }

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
        val apiService = ApiConfig.getApiService()
        val factory = ProfileViewModelFactory(apiService)
        profileViewModel = ViewModelProvider(requireActivity(), factory)[ProfileViewModel::class.java]

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

        binding.fabEditAvatar.setOnClickListener {
            checkPermissionsAndShowDialog()
        }
    }

    private fun observeViewModel() {
        profileViewModel.patientProfile.observe(viewLifecycleOwner) { profile ->
            profile?.let {
                if (binding.edtUsername.text.toString() != it.username) {
                    binding.edtUsername.setText(it.username)
                }
                if (binding.edtEmail.text.toString() != it.email) {
                    binding.edtEmail.setText(it.email)
                }
                if (binding.edtPhone.text.toString() != it.phoneNumber) {
                    binding.edtPhone.setText(it.phoneNumber)
                }

                if (!it.profilePictureUrl.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(it.profilePictureUrl)
                        .placeholder(R.drawable.baseline_person_24)
                        .error(R.drawable.baseline_error_24)
                        .into(binding.ivProfileAvatarEdit)
                } else {
                    binding.ivProfileAvatarEdit.setImageResource(R.drawable.baseline_person_24)
                }
            }
        }

        profileViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.btnLanjutkanAccount.isEnabled = !isLoading
            binding.fabEditAvatar.isEnabled = !isLoading
        }

        profileViewModel.isUpdateSuccess.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                Toast.makeText(requireContext(), "Informasi Akun berhasil diperbarui", Toast.LENGTH_SHORT).show()
                val updatedProfile = profileViewModel.patientProfile.value
                updatedProfile?.let {
                    authenticationManager.updateProfileData(it)
                    profileViewModel.setProfileUpdated(it)
                    profileViewModel.resetNeedsRefresh()
                }

                profileViewModel.resetUpdateStatus()
            }
        }

        profileViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                profileViewModel.clearErrorMessage()
            }
        }

        profileViewModel.isImageUploadSuccess.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                Toast.makeText(requireContext(), "Foto profil berhasil diperbarui!", Toast.LENGTH_SHORT).show()
            }
        }

        profileViewModel.imageUploadMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                profileViewModel.clearErrorMessage()
            }
        }
    }

    private fun loadData() {
        val token = authenticationManager.getAccess(AuthenticationManager.TOKEN)
        if (token != null) {
            val cachedProfile = profileViewModel.patientProfile.value
            if (cachedProfile != null) {
                binding.edtUsername.setText(cachedProfile.username)
                binding.edtEmail.setText(cachedProfile.email)
                binding.edtPhone.setText(cachedProfile.phoneNumber)
                if (!cachedProfile.profilePictureUrl.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(cachedProfile.profilePictureUrl)
                        .placeholder(R.drawable.baseline_person_24)
                        .error(R.drawable.baseline_error_24)
                        .into(binding.ivProfileAvatarEdit)
                } else {
                    binding.ivProfileAvatarEdit.setImageResource(R.drawable.baseline_person_24)
                }
            } else {
                profileViewModel.getPatientProfile(token)
            }
        } else {
            Toast.makeText(requireContext(), "Token tidak ditemukan, silakan login ulang.", Toast.LENGTH_SHORT).show()
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

    private fun checkPermissionsAndShowDialog() {
        val permissionsToRequest = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES)
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.CAMERA)
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            showImagePickerDialog()
        }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf<CharSequence>("Ambil Foto", "Pilih dari Galeri", "Batal")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Pilih Sumber Gambar")
        builder.setItems(options) { dialog, item ->
            when {
                options[item] == "Ambil Foto" -> startTakePhoto()
                options[item] == "Pilih dari Galeri" -> startGallery()
                options[item] == "Batal" -> dialog.dismiss()
            }
        }
        builder.show()
    }

    private fun startTakePhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.resolveActivity(requireActivity().packageManager)
        createCustomTempFile(requireContext()).also {
            val photoURI: Uri = FileProvider.getUriForFile(
                requireContext(),
                "com.mraihanfauzii.restrokotlin.fileprovider",
                it
            )
            currentPhotoPath = it.absolutePath
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            launcherIntentCamera.launch(intent)
        }
    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, "Pilih Gambar")
        launcherIntentGallery.launch(chooser)
    }

    private fun createCustomTempFile(context: android.content.Context): File {
        val storageDir: File? = context.getExternalFilesDir(null)
        return File.createTempFile(System.currentTimeMillis().toString(), ".jpg", storageDir)
    }

    private fun uriToFile(selectedImg: Uri, context: android.content.Context): File {
        val contentResolver = context.contentResolver
        val myFile = createCustomTempFile(context)

        val inputStream = contentResolver.openInputStream(selectedImg) as InputStream
        val outputStream = FileOutputStream(myFile)
        val buffer = ByteArray(1024)
        var length: Int
        while (inputStream.read(buffer).also { length = it } > 0) {
            outputStream.write(buffer, 0, length)
        }
        outputStream.close()
        inputStream.close()
        return myFile
    }

    private fun uploadImageToServer() {
        if (getFile != null) {
            val token = authenticationManager.getAccess(AuthenticationManager.TOKEN)
            if (token != null) {
                profileViewModel.uploadProfilePicture(token, getFile as File)
            } else {
                Toast.makeText(requireContext(), "Token tidak ditemukan, silakan login ulang.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "Silakan pilih gambar terlebih dahulu.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}