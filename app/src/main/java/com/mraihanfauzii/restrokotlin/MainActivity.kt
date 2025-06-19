package com.mraihanfauzii.restrokotlin

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
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
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {

    private lateinit var actions: List<String>
    private val plannedExercises = mutableListOf<MutableMap<String, Any>>() // {actionName: String, targetReps: Int, (optional) gerakanId: Int}
    private var maxDurationPerRep: Int = 20 // Default value
    private lateinit var exercisesContainer: LinearLayout // Container for dynamically added exercise views

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 100
        private val REQUIRED_PERMISSIONS = arrayOf(android.Manifest.permission.CAMERA)
        private const val TAG = "DebugActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Use the new debug layout

        exercisesContainer = findViewById(R.id.exercisesContainer)

        loadActions()
        setupMaxDurationSlider()
        setupAddExerciseSpinner()
        setupStartButton()

        // Check and request permissions on app start
        if (!allPermissionsGranted()) {
            requestPermissions()
        }
    }

    /**
     * Loads the list of exercise actions from `assets/actions.txt`.
     */
    private fun loadActions() {
        try {
            assets.open("actions.txt").use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    actions = reader.readLines().map { it.trim() }.filter { it.isNotEmpty() }
                }
            }
            Log.d(TAG, "Actions loaded: $actions")
        } catch (e: Exception) {
            Toast.makeText(this, "Error loading actions.txt: ${e.message}", Toast.LENGTH_LONG).show()
            Log.e(TAG, "Error loading actions.txt", e)
            actions = emptyList() // Ensure actions is not null
        }
    }

    /**
     * Sets up the slider for maximum duration per repetition.
     */
    private fun setupMaxDurationSlider() {
        val maxDurationTv: TextView = findViewById(R.id.maxDurationValue)
        maxDurationTv.text = "Durasi Maksimal per Repetisi (detik): $maxDurationPerRep"

        val maxDurationSlider: SeekBar = findViewById(R.id.maxDurationSlider)
        maxDurationSlider.progress = maxDurationPerRep
        maxDurationSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Ensure progress stays within 5-60 seconds range
                maxDurationPerRep = progress.coerceIn(5, 60)
                maxDurationTv.text = "Durasi Maksimal per Repetisi (detik): $maxDurationPerRep"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    /**
     * Sets up the spinner for selecting and adding exercises.
     */
    private fun setupAddExerciseSpinner() {
        val spinner: Spinner = findViewById(R.id.actionSpinner)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, actions)
        spinner.adapter = adapter

        findViewById<Button>(R.id.addExerciseButton).setOnClickListener {
            // Ensure actions list is not empty before accessing selectedItem
            if (actions.isNotEmpty()) {
                val selectedAction = spinner.selectedItem as String
                showAddExerciseDialog(selectedAction)
            } else {
                Toast.makeText(this, "Tidak ada gerakan yang dimuat.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Shows a dialog to input the number of repetitions for a selected exercise.
     */
    private fun showAddExerciseDialog(actionName: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Tambahkan $actionName")

        val input = EditText(this)
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
                    Toast.makeText(this, "Repetisi harus lebih dari 0", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Jumlah repetisi tidak boleh kosong", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Batal") { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }

    /**
     * Dynamically updates the UI to display the list of planned exercises.
     */
    private fun updatePlannedExercisesUI() {
        exercisesContainer.removeAllViews() // Clear existing views

        if (plannedExercises.isEmpty()) {
            val textView = TextView(this)
            textView.text = "Belum ada latihan yang dipilih."
            textView.textSize = 16f
            exercisesContainer.addView(textView)
            findViewById<Button>(R.id.startButton).isEnabled = false
            return
        }

        findViewById<Button>(R.id.startButton).isEnabled = true

        plannedExercises.forEachIndexed { index, exercise ->
            // Inflate the custom item layout for each exercise
            val exerciseView = layoutInflater.inflate(R.layout.item_planned_exercise, exercisesContainer, false) as CardView
            val actionNameTv: TextView = exerciseView.findViewById(R.id.actionNameTv)
            val repsTv: TextView = exerciseView.findViewById(R.id.repsTv)
            val removeBtn: ImageButton = exerciseView.findViewById(R.id.removeBtn) // Changed to ImageButton
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
                // Use a dialog to confirm removal
                AlertDialog.Builder(this)
                    .setTitle("Hapus Latihan?")
                    .setMessage("Anda yakin ingin menghapus '${exercise["actionName"]}' dari daftar?")
                    .setPositiveButton("Hapus") { dialog, _ ->
                        plannedExercises.removeAt(index)
                        updatePlannedExercisesUI() // Rebuild UI after removal
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

    /**
     * Sets up the start button to navigate to the DetectActivity.
     */
    private fun setupStartButton() {
        findViewById<Button>(R.id.startButton).setOnClickListener {
            if (plannedExercises.isNotEmpty()) {
                if (allPermissionsGranted()) {
                    startDetectActivity()
                } else {
                    requestPermissions()
                }
            } else {
                Toast.makeText(this, "Pilih setidaknya satu latihan!", Toast.LENGTH_SHORT).show()
            }
        }
        findViewById<Button>(R.id.startButton).isEnabled = false // Disable initially
    }

    /**
     * Checks if all required permissions are granted.
     */
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Requests necessary permissions from the user.
     */
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CAMERA_PERMISSION)
    }

    /**
     * Handles the result of permission requests.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (allPermissionsGranted()) {
                Toast.makeText(this, "Izin kamera diberikan", Toast.LENGTH_SHORT).show()
                startDetectActivity()
            } else {
                Toast.makeText(this, "Izin kamera ditolak", Toast.LENGTH_SHORT).show()
                // Optionally disable start button or show message to enable permissions
            }
        }
    }

    /**
     * Starts the DetectActivity, passing the planned exercises and max duration.
     */
    private fun startDetectActivity() {
        val intent = Intent(this, DetectActivity::class.java).apply {
            // Convert plannedExercises to ArrayList of Bundles for Parcelable transfer
            val exercisesBundleList = ArrayList<Bundle>()
            plannedExercises.forEach { exercise ->
                val bundle = Bundle()
                bundle.putString("actionName", exercise["actionName"] as String)
                bundle.putInt("targetReps", exercise["targetReps"] as Int)
                // If you have 'gerakanId' add it here too
                // if (exercise.containsKey("gerakanId")) {
                //     bundle.putInt("gerakanId", exercise["gerakanId"] as Int)
                // }
                exercisesBundleList.add(bundle)
            }
            putParcelableArrayListExtra("plannedExercises", exercisesBundleList)
            putExtra("maxDurationPerRep", maxDurationPerRep)
        }
        startActivity(intent)
    }
}