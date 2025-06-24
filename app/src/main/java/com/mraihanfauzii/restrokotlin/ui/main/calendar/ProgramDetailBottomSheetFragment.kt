package com.mraihanfauzii.restrokotlin.ui.main.calendar

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mraihanfauzii.restrokotlin.databinding.FragmentProgramDetailBottomSheetBinding
import com.mraihanfauzii.restrokotlin.model.CalendarProgramResponse
import com.mraihanfauzii.restrokotlin.model.ReportItem
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

        // Keys untuk Fragment Result API
        const val REQUEST_KEY_PROGRAM_UPDATE = "request_key_program_update"
        const val BUNDLE_KEY_UPDATE_SUCCESS = "bundle_key_update_success"

        // === TAMBAHKAN KEYS BARU UNTUK NAVIGASI ===
        const val REQUEST_KEY_NAVIGATE = "request_key_navigate_from_bottom_sheet"
        const val BUNDLE_KEY_NAV_ACTION = "bundle_key_nav_action"
        const val BUNDLE_KEY_PROGRAM_DETAIL = "bundle_key_program_detail" // Untuk ProgramPreparationFragment
        const val BUNDLE_KEY_PROGRAM_ID_REPORT = "bundle_key_program_id_report" // Untuk ReportHistoryFragment

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
                // Kirim hasil ke target fragment (CalendarFragment)
                setFragmentResult(REQUEST_KEY_PROGRAM_UPDATE, Bundle().apply {
                    putBoolean(BUNDLE_KEY_UPDATE_SUCCESS, true)
                })
                programId?.let { id ->
                    val token = authenticationManager.getAccess(AuthenticationManager.TOKEN)
                    if (token != null) {
                        programDetailViewModel.getProgramDetail(token, id)
                    }
                }
            }
        }

        programDetailViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                programDetailViewModel.clearErrorMessage()
            }
        }

        programDetailViewModel.historyForProgram.observe(viewLifecycleOwner) { list ->
            if (list != null) showHistoryDialog(list)
        }
    }

    private fun showHistoryDialog(items: List<ReportItem>) {
        val msg = buildString {
            items.forEachIndexed { idx, rpt ->
                append("» Laporan #${idx + 1}  ( ${rpt.submittedAt} )\n")
                append("   • Durasi total : ${rpt.totalSeconds ?: 0}s\n")
                append("   • Poin         : ${rpt.points ?: 0}\n")
                rpt.summary?.let {
                    append("   • Ringkasan    : ✔️ ${it.ok}  ✖️ ${it.ng}  ⚠️ ${it.undetected}\n")
                }
                rpt.details?.forEach { d ->
                    append("        - ${d.nama}  ➜  ✔️${d.ok}/✖️${d.ng}/⚠️${d.undetected}  (${d.durasi}s)\n")
                }
                rpt.note?.let { append("   • Catatan      : $it\n") }
                append("\n")
            }
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Riwayat Laporan (${items.size})")
            .setMessage(msg.trimEnd())
            .setPositiveButton("Tutup", null)
            .show()
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
                        if (program.plannedMovements.isNullOrEmpty()) {
                            Toast.makeText(requireContext(), "Program ini tidak memiliki gerakan yang direncanakan.", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        // Kirim request navigasi ke CalendarFragment melalui Fragment Result API
                        setFragmentResult(REQUEST_KEY_NAVIGATE, Bundle().apply {
                            putString(BUNDLE_KEY_NAV_ACTION, "to_program_preparation")
                            putParcelable(BUNDLE_KEY_PROGRAM_DETAIL, program)
                        })

                        dismiss() // Tutup bottom sheet setelah request dikirim
                    }
                }
            }
            "selesai" -> {
                binding.btnActionProgram.apply {
                    text = "Lihat Riwayat Laporan"
                    visibility = View.VISIBLE
                    setOnClickListener {
                        // **** PERBAIKAN UTAMA DI SINI ****
                        // Kirim request navigasi ke CalendarFragment melalui Fragment Result API
                        setFragmentResult(REQUEST_KEY_NAVIGATE, Bundle().apply {
                            putString(BUNDLE_KEY_NAV_ACTION, "to_report_history")
                            putInt(BUNDLE_KEY_PROGRAM_ID_REPORT, program.id!!)
                        })

                        dismiss() // Tutup bottom sheet setelah request dikirim
                    }
                }
            }

            "dibatalkan" -> binding.btnActionProgram.visibility = View.GONE
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