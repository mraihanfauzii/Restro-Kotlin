package com.mraihanfauzii.restrokotlin.ui.main.calendar

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mraihanfauzii.restrokotlin.R
import com.mraihanfauzii.restrokotlin.databinding.FragmentProgramDetailBottomSheetBinding // Sesuaikan dengan nama layout Anda
import com.mraihanfauzii.restrokotlin.model.CalendarProgramResponse
import com.mraihanfauzii.restrokotlin.ui.authentication.AuthenticationManager
import com.mraihanfauzii.restrokotlin.viewmodel.ProgramDetailViewModel

class ProgramDetailBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentProgramDetailBottomSheetBinding? = null
    private val binding get() = _binding!!

    private lateinit var programDetailViewModel: ProgramDetailViewModel
    private lateinit var authenticationManager: AuthenticationManager

    private var programId: Int? = null

    companion object {
        const val TAG = "ProgramDetailBottomSheet"
        private const val ARG_PROGRAM_ID = "program_id_arg"

        fun newInstance(programId: Int): ProgramDetailBottomSheetFragment {
            val fragment = ProgramDetailBottomSheetFragment()
            val args = Bundle().apply {
                putInt(ARG_PROGRAM_ID, programId)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            programId = it.getInt(ARG_PROGRAM_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProgramDetailBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authenticationManager = AuthenticationManager(requireContext())
        programDetailViewModel = ViewModelProvider(this)[ProgramDetailViewModel::class.java]

        observeViewModel()

        programId?.let { id ->
            val token = authenticationManager.getAccess(AuthenticationManager.TOKEN)
            if (token != null) {
                programDetailViewModel.getProgramDetail(token, id)
            } else {
                Toast.makeText(requireContext(), "Token tidak ditemukan, silakan login ulang.", Toast.LENGTH_SHORT).show()
                dismiss()
            }
        } ?: run {
            Toast.makeText(requireContext(), "ID Program tidak valid.", Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }

    private fun observeViewModel() {
        programDetailViewModel.programDetail.observe(viewLifecycleOwner) { program ->
            program?.let {
                displayProgramDetails(it)
                setupActionButton(it)
            } ?: run {
                // Jika program null (misal: gagal diambil dari API)
                // Toast.makeText(requireContext(), "Gagal memuat detail program.", Toast.LENGTH_SHORT).show()
                // dismiss() // Opsional: tutup jika gagal total
            }
        }

        programDetailViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBarDetail.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnActionProgram.isEnabled = !isLoading
            // Anda bisa mengatur alpha atau disable view lain saat loading
            binding.tvDetailJenisProgram.alpha = if (isLoading) 0.5f else 1.0f
            binding.tvDetailCatatanTerapis.alpha = if (isLoading) 0.5f else 1.0f
            binding.tvDetailTerapis.alpha = if (isLoading) 0.5f else 1.0f
            binding.tvDetailStatusProgram.alpha = if (isLoading) 0.5f else 1.0f
        }

        programDetailViewModel.updateStatusSuccess.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                Toast.makeText(requireContext(), "Status program berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                (parentFragment as? CalendarFragment)?.loadCalendarProgramsForMonth(
                    (parentFragment as CalendarFragment).currentFocusedMonth
                )
            }
        }

        programDetailViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                programDetailViewModel.clearErrorMessage()
            }
        }
    }

    private fun displayProgramDetails(program: CalendarProgramResponse) {
        binding.tvDetailJenisProgram.text = program.programName ?: "-"
        binding.tvDetailCatatanTerapis.text = program.therapistNotes ?: "-"
        binding.tvDetailTerapis.text = program.therapist?.fullName ?: "-" // Ambil dari objek terapis
        binding.tvDetailStatusProgram.text = program.status?.replace("_", " ")?.capitalize() ?: "-" // Tampilkan status
    }

    private fun setupActionButton(program: CalendarProgramResponse) {
        when (program.status) {
            "belum_dimulai" -> {
                binding.btnActionProgram.apply {
                    text = "Mulai Program"
                    visibility = View.VISIBLE
                    setOnClickListener {
                        showStartProgramConfirmationDialog(program.id!!)
                    }
                }
            }
            "berjalan" -> {
                binding.btnActionProgram.apply {
                    text = "Lanjutkan Program"
                    visibility = View.VISIBLE
                    setOnClickListener {
                        // TODO: Implementasi logika untuk melanjutkan program.
                        // Mungkin navigasi ke layar aktivitas program atau detail lainnya.
                        Toast.makeText(requireContext(), "Mengarahkan ke halaman program berjalan...", Toast.LENGTH_SHORT).show()
                        dismiss()
                    }
                }
            }
            "selesai", "dibatalkan" -> {
                binding.btnActionProgram.visibility = View.GONE // Sembunyikan tombol
            }
            else -> {
                binding.btnActionProgram.visibility = View.GONE // Status tidak dikenal, sembunyikan
            }
        }
    }

    private fun showStartProgramConfirmationDialog(programId: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Konfirmasi")
            .setMessage("Apakah Anda yakin ingin memulai program ini?")
            .setPositiveButton("Ya") { dialog, _ ->
                val token = authenticationManager.getAccess(AuthenticationManager.TOKEN)
                if (token != null) {
                    programDetailViewModel.updateProgramStatus(token, programId, "berjalan")
                } else {
                    Toast.makeText(requireContext(), "Token tidak ditemukan, silakan login ulang.", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}