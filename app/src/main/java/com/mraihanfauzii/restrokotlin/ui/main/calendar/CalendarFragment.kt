package com.mraihanfauzii.restrokotlin.ui.main.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mraihanfauzii.restrokotlin.R
import com.mraihanfauzii.restrokotlin.adapter.calendar.CalendarEventAdapter
import com.mraihanfauzii.restrokotlin.databinding.FragmentCalendarBinding
import com.mraihanfauzii.restrokotlin.model.CalendarProgramResponse
import com.mraihanfauzii.restrokotlin.ui.authentication.AuthenticationManager
import com.mraihanfauzii.restrokotlin.viewmodel.CalendarViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    private lateinit var calendarVM: CalendarViewModel
    private lateinit var auth:        AuthenticationManager
    private lateinit var adapter:     CalendarEventAdapter

    /** bulan yang sedang difokuskan di header */
    var currentFocusedMonth: Calendar = Calendar.getInstance()

    // ─────────────────────────────────────────────────────────────────────────────

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    // ─────────────────────────────────────────────────────────────────────────────

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth       = AuthenticationManager(requireContext())
        calendarVM = ViewModelProvider(this)[CalendarViewModel::class.java]

        initRecycler()
        initListeners()
        observeVM()

        loadCalendarProgramsForMonth(currentFocusedMonth)

        /* menerima hasil dari ProgramDetailBottomSheetFragment  */
        setFragmentResultListener(ProgramDetailBottomSheetFragment.REQUEST_KEY_PROGRAM_UPDATE)
        { _, bundle ->
            if (bundle.getBoolean(
                    ProgramDetailBottomSheetFragment.BUNDLE_KEY_UPDATE_SUCCESS, false
                )
            ) loadCalendarProgramsForMonth(currentFocusedMonth)
        }

        /* permintaan navigasi yang dikirim bottom-sheet */
        setFragmentResultListener(ProgramDetailBottomSheetFragment.REQUEST_KEY_NAVIGATE)
        { _, bundle ->
            when (bundle.getString(
                ProgramDetailBottomSheetFragment.BUNDLE_KEY_NAV_ACTION
            )) {
                "to_program_preparation" -> {
                    bundle.getParcelable<CalendarProgramResponse>(
                        ProgramDetailBottomSheetFragment.BUNDLE_KEY_PROGRAM_DETAIL
                    )?.let { prog ->
                        findNavController().navigate(
                            R.id.action_calendarFragment_to_programPreparationFragment,
                            Bundle().apply {
                                putParcelable(
                                    ProgramPreparationFragment.ARG_PROGRAM_DETAIL,
                                    prog
                                )
                            }
                        )
                    }
                }
                "to_report_history" -> {
                    val progId = bundle.getInt(
                        ProgramDetailBottomSheetFragment.BUNDLE_KEY_PROGRAM_ID_REPORT, -1
                    )
                    if (progId != -1) {
                        findNavController().navigate(
                            R.id.action_calendarFragment_to_reportHistoryFragment,
                            Bundle().apply { putInt("PROGRAM_ID_ARG", progId) }
                        )
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "ID Program tidak valid untuk riwayat laporan.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // UI helpers
    // ─────────────────────────────────────────────────────────────────────────────

    private fun initRecycler() {
        adapter = CalendarEventAdapter { program ->
            program.id?.let { showProgramDetail(it) }
                ?: Toast.makeText(
                    requireContext(), "ID Program tidak ditemukan.", Toast.LENGTH_SHORT
                ).show()
        }
        binding.rvCalendarEvents.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter       = this@CalendarFragment.adapter
        }
    }

    private fun initListeners() {
        updateMonthHeader(currentFocusedMonth)

        binding.btnPreviousMonth.setOnClickListener {
            currentFocusedMonth.add(Calendar.MONTH, -1)
            updateMonthHeader(currentFocusedMonth)
            loadCalendarProgramsForMonth(currentFocusedMonth)
        }
        binding.btnNextMonth.setOnClickListener {
            currentFocusedMonth.add(Calendar.MONTH, 1)
            updateMonthHeader(currentFocusedMonth)
            loadCalendarProgramsForMonth(currentFocusedMonth)
        }
    }

    private fun updateMonthHeader(cal: Calendar) {
        binding.tvMonthHeader.text =
            SimpleDateFormat("MMMM yyyy", Locale("id", "ID")).format(cal.time)
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // View-Model
    // ─────────────────────────────────────────────────────────────────────────────

    private fun observeVM() {
        calendarVM.calendarPrograms.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list ?: emptyList())
            binding.tvNoSchedule.visibility =
                if (list.isNullOrEmpty()) View.VISIBLE else View.GONE
            binding.rvCalendarEvents.visibility =
                if (list.isNullOrEmpty()) View.GONE else View.VISIBLE
        }

        calendarVM.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility   = if (loading) View.VISIBLE else View.GONE
            binding.btnPreviousMonth.isEnabled = !loading
            binding.btnNextMonth.isEnabled     = !loading
            binding.rvCalendarEvents.alpha     = if (loading) 0.5f else 1f
        }

        calendarVM.errorMessage.observe(viewLifecycleOwner) { msg ->
            msg?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                calendarVM.clearErrorMessage()
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Data
    // ─────────────────────────────────────────────────────────────────────────────

    private fun loadCalendarProgramsForMonth(cal: Calendar) {
        auth.getAccess(AuthenticationManager.TOKEN)?.let { token ->
            val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            val start = Calendar.getInstance().apply {
                time = cal.time; set(Calendar.DAY_OF_MONTH, 1)
            }
            val end = Calendar.getInstance().apply {
                time = cal.time; set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            }

            calendarVM.getCalendarPrograms(
                token,
                fmt.format(start.time),
                fmt.format(end.time)
            )
        } ?: Toast.makeText(
            requireContext(), "Token tidak ditemukan, silakan login ulang.",
            Toast.LENGTH_SHORT
        ).show()
    }

    // ─────────────────────────────────────────────────────────────────────────────

    private fun showProgramDetail(programId: Int) {
        ProgramDetailBottomSheetFragment
            .newInstance(programId)
            .show(parentFragmentManager, ProgramDetailBottomSheetFragment.TAG)
    }

    override fun onDestroyView() {
        super.onDestroyView(); _binding = null
    }
}
