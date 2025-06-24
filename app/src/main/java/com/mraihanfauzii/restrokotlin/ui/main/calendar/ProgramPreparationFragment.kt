package com.mraihanfauzii.restrokotlin.ui.main.calendar

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mraihanfauzii.restrokotlin.R
import com.mraihanfauzii.restrokotlin.adapter.calendar.MovementListAdapter
import com.mraihanfauzii.restrokotlin.databinding.FragmentProgramPreparationBinding
import com.mraihanfauzii.restrokotlin.model.CalendarProgramResponse

class ProgramPreparationFragment : Fragment() {

    private var _binding: FragmentProgramPreparationBinding? = null
    private val binding get() = _binding!!

    private lateinit var movementListAdapter: MovementListAdapter

    // Gunakan objek CalendarProgramResponse sebagai argumen utama
    private var program: CalendarProgramResponse? = null

    companion object {
        const val ARG_PROGRAM_DETAIL = "program_detail_arg"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            program = it.getParcelable(ARG_PROGRAM_DETAIL)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProgramPreparationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (program == null) {
            Toast.makeText(requireContext(), "Detail program tidak ditemukan.", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack() // Kembali jika data tidak ada
            return
        }

        setupRecyclerView()
        displayProgramDetails()
        setupButtons()
    }

    private fun setupRecyclerView() {
        movementListAdapter = MovementListAdapter()
        binding.rvMovementList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = movementListAdapter
        }
        program?.plannedMovements?.let {
            movementListAdapter.submitList(it.sortedBy { m -> m.urutanDalamProgram }) // Urutkan berdasarkan urutan
        }
    }

    private fun displayProgramDetails() {
        binding.tvProgramTitle.text = program?.programName ?: "Nama Program"
        binding.tvProgramDescription.text = program?.therapistNotes ?: "Tidak ada catatan terapis."
    }

    private fun setupButtons() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack() // Kembali ke fragment sebelumnya (ProgramDetailBottomSheetFragment)
        }

        binding.btnStartProgram.setOnClickListener {
            // Logika untuk memulai program, mirip dengan yang ada di ProgramDetailBottomSheetFragment
            val plannedBundles = program?.plannedMovements?.map { m ->
                Bundle().apply {
                    putString("actionName", m.movementName)
                    putInt("targetReps", m.jumlahRepetisiDirencanakan ?: 1)
                    putInt("gerakanId", m.id ?: -1)
                    putInt("orderInProgram", m.urutanDalamProgram ?: -1)
                }
            } ?: emptyList()

            val maxDuration = program?.plannedMovements?.firstOrNull()?.durationSeconds ?: 20

            if (plannedBundles.isNotEmpty()) {
                val navBundle = Bundle().apply {
                    putParcelableArray("plannedExercises", plannedBundles.toTypedArray())
                    putInt("maxDurationPerRep", maxDuration)
                    putString("programName", program?.programName)
                    putInt("programId", program?.id ?: -1)
                }
                Log.d("ProgramPreparation", "Navigating to DetectActivity with array: $plannedBundles, maxDur=$maxDuration")
                findNavController().navigate(R.id.action_programPreparationFragment_to_detectActivity, navBundle)
            } else {
                Toast.makeText(requireContext(), "Tidak ada gerakan yang direncanakan untuk program ini.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}