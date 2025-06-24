package com.mraihanfauzii.restrokotlin.ui.main.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.mraihanfauzii.restrokotlin.adapter.calendar.MovementDetailAdapter
import com.mraihanfauzii.restrokotlin.databinding.FragmentReportHistoryBinding
import com.mraihanfauzii.restrokotlin.ui.authentication.AuthenticationManager
import com.mraihanfauzii.restrokotlin.viewmodel.ReportHistoryViewModel

class ReportHistoryFragment : Fragment() {

    private lateinit var binding: FragmentReportHistoryBinding
    private lateinit var vm: ReportHistoryViewModel
    private lateinit var auth: AuthenticationManager
    private var programId: Int = -1

    private lateinit var movementDetailAdapter: MovementDetailAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        programId = requireArguments().getInt("PROGRAM_ID_ARG", -1)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentReportHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup Toolbar
        binding.toolbar.setNavigationOnClickListener {
            // Ini akan kembali ke fragment sebelumnya (ProgramDetailBottomSheetFragment)
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // Setup ViewModel & token
        auth = AuthenticationManager(requireContext())
        vm   = ViewModelProvider(this)[ReportHistoryViewModel::class.java]

        // Setup RecyclerView
        movementDetailAdapter = MovementDetailAdapter()
        binding.rvMovementDetails.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = movementDetailAdapter
            setHasFixedSize(true) // Untuk performa
        }

        // Observe data
        vm.report.observe(viewLifecycleOwner) { rpt ->
            binding.progressBar.isVisible = false
            hideAllContent() // Sembunyikan semua konten sebelum menampilkan yang baru

            if (rpt == null) {
                binding.tvNotFound.isVisible = true
                return@observe
            }

            // Tampilkan CardViews
            binding.cardProgramInfo.isVisible = true
            binding.cardSummary.isVisible = true
            binding.cardMovementDetails.isVisible = true

            // Isi data program info
            binding.tvProgramName.text = rpt.programInfo?.namaProgram ?: "-"
            binding.tvTherapist.text   = "Terapis: ${rpt.programInfo?.namaTerapisProgram ?: "-"}"
            binding.tvSubmitDate.text  = "Tanggal Laporan: ${rpt.submittedAt ?: "-"}"

            // Isi data summary
            binding.tvTotalTime.text = "Total Durasi: ${rpt.totalSeconds ?: 0}s"
            binding.tvPoints.text    = "Poin Diperoleh: ${rpt.points ?: 0}"
            binding.tvSummaryOk.text = "Sempurna: ${rpt.summary?.ok ?: 0}"
            binding.tvSummaryNg.text = "Tidak Sempurna: ${rpt.summary?.ng ?: 0}"
            binding.tvSummaryUndetected.text = "Tidak Terdeteksi: ${rpt.summary?.undetected ?: 0}"
            binding.tvNote.text      = "Catatan: ${rpt.note ?: "-"}"

            // Isi data detail gerakan ke RecyclerView
            movementDetailAdapter.submitList(rpt.details)
        }

        vm.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.isVisible = loading
            if (loading) {
                hideAllContent()
                binding.tvNotFound.isVisible = false // Sembunyikan pesan not found saat loading
            }
        }

        vm.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                vm.clearErrorMessage()
                binding.progressBar.isVisible = false
                hideAllContent()
                binding.tvNotFound.isVisible = true
            }
        }


        // Load history when fragment is created
        if (programId != -1) {
            auth.getAccess(AuthenticationManager.TOKEN)?.let { token ->
                vm.loadHistory(token, programId)
            } ?: run {
                Toast.makeText(requireContext(), "Token tidak ditemukan, silakan login ulang.", Toast.LENGTH_SHORT).show()
                binding.progressBar.isVisible = false
                binding.tvNotFound.isVisible = true
            }
        } else {
            Toast.makeText(requireContext(), "ID Program tidak valid.", Toast.LENGTH_SHORT).show()
            binding.progressBar.isVisible = false
            binding.tvNotFound.isVisible = true
        }
    }

    private fun hideAllContent() {
        binding.cardProgramInfo.isVisible = false
        binding.cardSummary.isVisible = false
        binding.cardMovementDetails.isVisible = false
        binding.tvNotFound.isVisible = false
    }
}