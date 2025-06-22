package com.mraihanfauzii.restrokotlin.ui.main.food

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.mraihanfauzii.restrokotlin.R
import com.mraihanfauzii.restrokotlin.databinding.FragmentFoodBinding // Akan kita buat nanti
import com.mraihanfauzii.restrokotlin.ui.authentication.AuthenticationManager
import com.mraihanfauzii.restrokotlin.viewmodel.FoodViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FoodFragment : Fragment() {

    private var _binding: FragmentFoodBinding? = null
    private val binding get() = _binding!!

    private lateinit var foodViewModel: FoodViewModel
    private lateinit var authenticationManager: AuthenticationManager

    // Untuk menyimpan tanggal yang dipilih (default hari ini)
    private var selectedDate: Calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFoodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authenticationManager = AuthenticationManager(requireContext())
        foodViewModel = ViewModelProvider(this)[FoodViewModel::class.java] // Scope fragment

        setupUI()
        observeViewModel()
        loadDietPlanForDate(selectedDate)
    }

    private fun setupUI() {
        // Implementasi klik pada tiap bagian Pola Makan untuk menampilkan detail
        // Anda mungkin ingin menavigasi ke detail atau menampilkan dialog/bottom sheet
        // Untuk saat ini, kita akan hanya menampilkan Toast sederhana
        binding.llMakanPagi.setOnClickListener { showMealDetail("Makan Pagi", binding.tvMenuPagi.text.toString()) }
        binding.llMakanSiang.setOnClickListener { showMealDetail("Makan Siang", binding.tvMenuSiang.text.toString()) }
        binding.llMakanMalam.setOnClickListener { showMealDetail("Makan Malam", binding.tvMenuMalam.text.toString()) }
        binding.llCemilan.setOnClickListener { showMealDetail("Cemilan", binding.tvCemilan.text.toString()) }

        binding.btnSelectDate.setOnClickListener {
            showDatePicker()
        }
    }

    private fun observeViewModel() {
        foodViewModel.dietPlan.observe(viewLifecycleOwner) { dietPlan ->
            dietPlan?.let {
                // Perbarui TextView di layout fragment_food dengan data yang diterima
                binding.tvMenuPagi.text = it.breakfastMenu ?: "Belum ada menu"
                binding.tvMenuSiang.text = it.lunchMenu ?: "Belum ada menu"
                binding.tvMenuMalam.text = it.dinnerMenu ?: "Belum ada menu"
                binding.tvCemilan.text = it.snackMenu ?: "Belum ada menu"
            } ?: run {
                // Jika dietPlan null (misalnya 404 Not Found), tampilkan pesan "Belum ada menu"
                binding.tvMenuPagi.text = "Belum ada menu"
                binding.tvMenuSiang.text = "Belum ada menu"
                binding.tvMenuMalam.text = "Belum ada menu"
                binding.tvCemilan.text = "Belum ada menu"
            }
        }

        foodViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            // Disable interaksi UI lainnya jika sedang loading
            binding.llMakanPagi.isEnabled = !isLoading
            binding.llMakanSiang.isEnabled = !isLoading
            binding.llMakanMalam.isEnabled = !isLoading
            binding.llCemilan.isEnabled = !isLoading
            binding.btnSelectDate.isEnabled = !isLoading
        }

        foodViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                foodViewModel.clearErrorMessage()
            }
        }
    }

    private fun loadDietPlanForDate(calendar: Calendar) {
        val token = authenticationManager.getAccess(AuthenticationManager.TOKEN)
        if (token != null) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale("id", "ID"))
            val dateString = dateFormat.format(calendar.time)
            binding.tvSelectedDate.text = dateFormat.format(calendar.time) // Tampilkan tanggal di UI
            foodViewModel.getDietPlan(token, dateString)
        } else {
            Toast.makeText(requireContext(), "Token tidak ditemukan, silakan login ulang.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showMealDetail(mealType: String, menu: String) {
        Toast.makeText(requireContext(), "$mealType: $menu", Toast.LENGTH_LONG).show()
        // Anda bisa mengganti ini dengan BottomSheetDialogFragment atau navigasi ke detail Fragment
    }

    private fun showDatePicker() {
        val year = selectedDate.get(Calendar.YEAR)
        val month = selectedDate.get(Calendar.MONTH)
        val day = selectedDate.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = android.app.DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val newDate = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay)
                }
                selectedDate = newDate
                loadDietPlanForDate(newDate)
            },
            year, month, day
        )
        datePickerDialog.show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}