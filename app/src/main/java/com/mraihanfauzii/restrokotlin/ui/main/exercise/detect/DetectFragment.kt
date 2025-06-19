package com.mraihanfauzii.restrokotlin.ui.main.exercise.detect

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.mraihanfauzii.restrokotlin.R
import java.io.BufferedReader
import java.io.InputStreamReader

class DetectFragment : Fragment() {

    private lateinit var actions: List<String>
    private val plannedExercises = mutableListOf<MutableMap<String, Any>>() // {actionName: String, targetReps: Int, (optional) gerakanId: Int}
    private var maxDurationPerRep: Int = 20 // Default value
    private lateinit var exercisesContainer: LinearLayout // Container for dynamically added exercise views
    private lateinit var rootView: View // Menyimpan referensi ke root view fragment

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 100
        private val REQUIRED_PERMISSIONS = arrayOf(android.Manifest.permission.CAMERA)
        private const val TAG = "DetectFragment" // Ubah TAG
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_detect, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        exercisesContainer = view.findViewById(R.id.exercisesContainer)

        loadActions()
        setupMaxDurationSlider()
        setupAddExerciseSpinner()
        setupStartButton()

        // Check and request permissions on app start
        if (!allPermissionsGranted()) {
            requestPermissions()
        }
    }

    private fun loadActions() {
        try {
            // Gunakan requireContext().assets
            requireContext().assets.open("actions.txt").use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    actions = reader.readLines().map { it.trim() }.filter { it.isNotEmpty() }
                }
            }
            Log.d(TAG, "Actions loaded: $actions")
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error loading actions.txt: ${e.message}", Toast.LENGTH_LONG).show()
            Log.e(TAG, "Error loading actions.txt", e)
            actions = emptyList()
        }
    }

    private fun setupMaxDurationSlider() {
        // Gunakan rootView.findViewById
        val maxDurationTv: TextView = rootView.findViewById(R.id.maxDurationValue)
        maxDurationTv.text = "Durasi Maksimal per Repetisi (detik): $maxDurationPerRep"

        val maxDurationSlider: SeekBar = rootView.findViewById(R.id.maxDurationSlider)
        maxDurationSlider.progress = maxDurationPerRep
        maxDurationSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                maxDurationPerRep = progress.coerceIn(5, 60)
                maxDurationTv.text = "Durasi Maksimal per Repetisi (detik): $maxDurationPerRep"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupAddExerciseSpinner() {
        // Gunakan rootView.findViewById dan requireContext()
        val spinner: Spinner = rootView.findViewById(R.id.actionSpinner)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, actions)
        spinner.adapter = adapter

        rootView.findViewById<Button>(R.id.addExerciseButton).setOnClickListener {
            if (actions.isNotEmpty()) {
                val selectedAction = spinner.selectedItem as String
                showAddExerciseDialog(selectedAction)
            } else {
                Toast.makeText(requireContext(), "Tidak ada gerakan yang dimuat.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAddExerciseDialog(actionName: String) {
        // Gunakan requireContext()
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Tambahkan $actionName")

        val input = EditText(requireContext())
        input.hint = "Jumlah repetisi"
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        builder.setView(input)

        builder.setPositiveButton("Tambah") { dialog, _ ->
            val repsText = input.text.toString()
            if (repsText.isNotEmpty()) {
                val reps = repsText.toInt()
                if (reps > 0) {
                    plannedExercises.add(mutableMapOf("actionName" to actionName, "targetReps" to reps))
                    updatePlannedExercisesUI()
                } else {
                    Toast.makeText(requireContext(), "Repetisi harus lebih dari 0", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Jumlah repetisi tidak boleh kosong", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Batal") { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }

    private fun updatePlannedExercisesUI() {
        exercisesContainer.removeAllViews()

        if (plannedExercises.isEmpty()) {
            val textView = TextView(requireContext())
            textView.text = "Belum ada latihan yang dipilih."
            textView.textSize = 16f
            exercisesContainer.addView(textView)
            rootView.findViewById<Button>(R.id.startButton).isEnabled = false
            return
        }

        rootView.findViewById<Button>(R.id.startButton).isEnabled = true

        plannedExercises.forEachIndexed { index, exercise ->
            // Gunakan requireContext() untuk layoutInflater
            val exerciseView = layoutInflater.inflate(R.layout.item_planned_exercise, exercisesContainer, false) as CardView
            val actionNameTv: TextView = exerciseView.findViewById(R.id.actionNameTv)
            val repsTv: TextView = exerciseView.findViewById(R.id.repsTv)
            val removeBtn: ImageButton = exerciseView.findViewById(R.id.removeBtn)
            val addRepsBtn: Button = exerciseView.findViewById(R.id.addRepsBtn)
            val minusRepsBtn: Button = exerciseView.findViewById(R.id.minusRepsBtn)

            actionNameTv.text = exercise["actionName"].toString()
            repsTv.text = "${exercise["targetReps"]} repetisi"

            addRepsBtn.setOnClickListener {
                var currentReps = exercise["targetReps"] as Int
                currentReps++
                exercise["targetReps"] = currentReps
                repsTv.text = "$currentReps repetisi"
            }

            minusRepsBtn.setOnClickListener {
                var currentReps = exercise["targetReps"] as Int
                if (currentReps > 1) {
                    currentReps--
                    exercise["targetReps"] = currentReps
                    repsTv.text = "$currentReps repetisi"
                }
            }

            removeBtn.setOnClickListener {
                // Gunakan requireContext()
                AlertDialog.Builder(requireContext())
                    .setTitle("Hapus Latihan?")
                    .setMessage("Anda yakin ingin menghapus '${exercise["actionName"]}' dari daftar?")
                    .setPositiveButton("Hapus") { dialog, _ ->
                        plannedExercises.removeAt(index)
                        updatePlannedExercisesUI()
                        dialog.dismiss()
                    }
                    .setNegativeButton("Batal") { dialog, _ ->
                        dialog.cancel()
                    }
                    .show()
            }
            exercisesContainer.addView(exerciseView)
        }
    }

    private fun setupStartButton() {
        // Gunakan rootView.findViewById
        rootView.findViewById<Button>(R.id.startButton).setOnClickListener {
            if (plannedExercises.isNotEmpty()) {
                if (allPermissionsGranted()) {
                    startDetectActivity()
                } else {
                    requestPermissions()
                }
            } else {
                Toast.makeText(requireContext(), "Pilih setidaknya satu latihan!", Toast.LENGTH_SHORT).show()
            }
        }
        rootView.findViewById<Button>(R.id.startButton).isEnabled = false
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        // Gunakan requireContext()
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        // Panggil requestPermissions dari Fragment
        requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CAMERA_PERMISSION)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (allPermissionsGranted()) {
                Toast.makeText(requireContext(), "Izin kamera diberikan", Toast.LENGTH_SHORT).show()
                startDetectActivity()
            } else {
                Toast.makeText(requireContext(), "Izin kamera ditolak", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startDetectActivity() {
        // Gunakan requireContext() untuk Intent
        val intent = Intent(requireContext(), DetectActivity::class.java).apply {
            val exercisesBundleList = ArrayList<Bundle>()
            plannedExercises.forEach { exercise ->
                val bundle = Bundle()
                bundle.putString("actionName", exercise["actionName"] as String)
                bundle.putInt("targetReps", exercise["targetReps"] as Int)
                exercisesBundleList.add(bundle)
            }
            putParcelableArrayListExtra("plannedExercises", exercisesBundleList)
            putExtra("maxDurationPerRep", maxDurationPerRep)
        }
        startActivity(intent)
    }
}