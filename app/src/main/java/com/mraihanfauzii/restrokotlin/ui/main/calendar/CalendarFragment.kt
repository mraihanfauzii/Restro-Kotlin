package com.mraihanfauzii.restrokotlin.ui.main.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.mraihanfauzii.restrokotlin.adapter.calendar.CalendarEventAdapter
import com.mraihanfauzii.restrokotlin.databinding.FragmentCalendarBinding
import com.mraihanfauzii.restrokotlin.ui.authentication.AuthenticationManager
import com.mraihanfauzii.restrokotlin.viewmodel.CalendarViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    private lateinit var calendarViewModel: CalendarViewModel
    private lateinit var authenticationManager: AuthenticationManager
    lateinit var calendarEventAdapter: CalendarEventAdapter

    var currentFocusedMonth: Calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authenticationManager = AuthenticationManager(requireContext())
        calendarViewModel = ViewModelProvider(this)[CalendarViewModel::class.java]

        setupRecyclerView()
        setupUI()
        observeViewModel()
        loadCalendarProgramsForMonth(currentFocusedMonth)
    }

    private fun setupRecyclerView() {
        calendarEventAdapter = CalendarEventAdapter { program ->
            program.id?.let {
                showProgramDetail(it)
            } ?: Toast.makeText(requireContext(), "ID Program tidak ditemukan.", Toast.LENGTH_SHORT).show()
        }
        binding.rvCalendarEvents.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = calendarEventAdapter
        }
    }

    private fun setupUI() {
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

    private fun updateMonthHeader(calendar: Calendar) {
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale("id", "ID"))
        binding.tvMonthHeader.text = monthFormat.format(calendar.time)
    }

    private fun observeViewModel() {
        calendarViewModel.calendarPrograms.observe(viewLifecycleOwner) { programs ->
            calendarEventAdapter.submitList(programs ?: emptyList())
            binding.tvNoSchedule.visibility = if (programs.isNullOrEmpty()) View.VISIBLE else View.GONE
            binding.rvCalendarEvents.visibility = if (programs.isNullOrEmpty()) View.GONE else View.VISIBLE
        }

        calendarViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnPreviousMonth.isEnabled = !isLoading
            binding.btnNextMonth.isEnabled = !isLoading
            binding.rvCalendarEvents.alpha = if (isLoading) 0.5f else 1.0f
        }

        calendarViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                calendarViewModel.clearErrorMessage()
            }
        }
    }

    fun loadCalendarProgramsForMonth(calendar: Calendar) {
        val token = authenticationManager.getAccess(AuthenticationManager.TOKEN)
        if (token != null) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            val startOfMonth = Calendar.getInstance().apply {
                time = calendar.time
                set(Calendar.DAY_OF_MONTH, 1)
            }
            val endOfMonth = Calendar.getInstance().apply {
                time = calendar.time
                set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            }

            val startDateString = dateFormat.format(startOfMonth.time)
            val endDateString = dateFormat.format(endOfMonth.time)

            calendarViewModel.getCalendarPrograms(token, startDateString, endDateString)
        } else {
            Toast.makeText(requireContext(), "Token tidak ditemukan, silakan login ulang.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showProgramDetail(programId: Int) {
        val bottomSheet = ProgramDetailBottomSheetFragment.newInstance(programId)
        bottomSheet.show(parentFragmentManager, ProgramDetailBottomSheetFragment.TAG)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}