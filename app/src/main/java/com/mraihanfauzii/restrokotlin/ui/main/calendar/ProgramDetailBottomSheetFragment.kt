package com.mraihanfauzii.restrokotlin.ui.main.calendar

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mraihanfauzii.restrokotlin.R
import com.mraihanfauzii.restrokotlin.databinding.FragmentProgramDetailBottomSheetBinding
import com.mraihanfauzii.restrokotlin.model.CalendarProgramResponse
import com.mraihanfauzii.restrokotlin.ui.authentication.AuthenticationManager
import com.mraihanfauzii.restrokotlin.viewmodel.ProgramDetailViewModel
import android.os.Parcelable

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
                Toast.makeText(requireContext(), "Gagal memuat detail program.", Toast.LENGTH_SHORT).show()
            }
        }

        programDetailViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBarDetail.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnActionProgram.isEnabled = !isLoading
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
        binding.tvDetailTerapis.text = program.therapist?.fullName ?: "-"
        binding.tvDetailStatusProgram.text = program.status?.replace("_", " ")?.capitalize() ?: "-"
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
                        val plannedBundles = program.plannedMovements?.map { m ->
                            Bundle().apply {
                                putString("actionName",  m.movementName)
                                putInt   ("targetReps",  m.jumlahRepetisiDirencanakan ?: 1)
                            }
                        } ?: emptyList()

                        val maxDuration = program.plannedMovements?.firstOrNull()?.durationSeconds ?: 20

                        if (plannedBundles.isNotEmpty()) {
                            val navBundle = Bundle().apply {
                                putParcelableArray("plannedExercises", plannedBundles.toTypedArray())
                                putInt   ("maxDurationPerRep", maxDuration)
                                putString("programName", program.programName)
                                putInt   ("programId",   program.id ?: -1)
                            }
                            Log.d(TAG, "Navigating with array: $plannedBundles, maxDur=$maxDuration")
                            requireActivity()
                                .findNavController(R.id.activityMainNavHostFragment)
                                .navigate(R.id.action_calendarFragment_to_detectActivity, navBundle)
                            dismiss()
                        } else {
                            Toast.makeText(requireContext(),
                                "Tidak ada gerakan yang direncanakan.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            "selesai", "dibatalkan" -> {
                binding.btnActionProgram.visibility = View.GONE
            }
            else -> {
                binding.btnActionProgram.visibility = View.GONE
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