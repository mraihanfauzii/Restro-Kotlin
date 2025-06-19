package com.mraihanfauzii.restrokotlin.ui.main.exercise.detect

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.google.common.util.concurrent.ListenableFuture
import com.google.mediapipe.tasks.components.containers.Landmark
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.mraihanfauzii.restrokotlin.ExercisePerformance
import com.mraihanfauzii.restrokotlin.PoseLandmarkerHelper
import com.mraihanfauzii.restrokotlin.R
import com.mraihanfauzii.restrokotlin.databinding.ActivityDetectBinding
import org.tensorflow.lite.Interpreter
import java.nio.channels.FileChannel
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.min

class DetectActivity : AppCompatActivity(), PoseLandmarkerHelper.LandmarkerListener {

    /* ───── View binding ───── */
    private lateinit var binding: ActivityDetectBinding

    /* ───── Hyper-parameter model ───── */
    private val seqLen   = 60
    private val numFeat  = 33 * 2            // 33 landmark × (x,y)
    private val needConf = 0.80f             // ambang “Sempurna”
    private val holdSec  = 2.0               // tahan 2 d
    private val prepSec  = 5                 // hitung-mundur 3 d
    private val TH_VIS = 0.35f

    /* Gerakan yg hanya butuh tubuh atas → kaki disembunyikan di OverlayView */
    private val upperBodyOnlyActions = setOf(
        "Rentangkan",
        "Naikan Kepalan Kedepan",
        "Angkat Tangan"
    )

    /* ───── Indeks landmark kaki (hip–foot_index) ───── */
    private val LEG_INDICES = intArrayOf(
        23, 24,   // hip
        25, 26,   // knee
        27, 28,   // ankle
        29, 30,   // heel
        31, 32    // foot_index
    )

    /* ───── Camera & pose ───── */
    private var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraFacing = CameraSelector.LENS_FACING_BACK
    private var poseLandmarkerHelper: PoseLandmarkerHelper? = null
    private lateinit var backgroundExecutor: ExecutorService

    private var cameraImageWidth  = 1
    private var cameraImageHeight = 1
    private val isFrontCamera get() = cameraFacing == CameraSelector.LENS_FACING_FRONT

    /* ───── TFLite ───── */
    private lateinit var interpreter : Interpreter
    private lateinit var meanScaler  : FloatArray
    private lateinit var scaleScaler : FloatArray
    private lateinit var actions     : List<String>
    private val    sequenceBuffer: Deque<FloatArray> = ArrayDeque(seqLen)

    /* ───── Sesi latihan ───── */
    private var plannedExercises : List<Map<String, Any>> = emptyList()
    private var maxDurationPerRep = 20

    private val sessionSummary = mutableListOf<ExercisePerformance>()
    private var currentExerciseIndex = 0
    private var currentExercisePerformance: ExercisePerformance? = null
    private var currentAttempt = 1

    private var preparationEndTime : Long? = null
    private var repetitionStartTime = 0L
    private var currentTime         = 0L
    private var perfectPoseStartTime: Long? = null
    private var isRepetitionComplete = false
    private var maskLegsForCurrentExercise = false // Untuk kontrol tampilan UI

    private var currentQualityLabel = "Tidak Terdeteksi"
    private var currentInstruction  = "Bersiap…"
    private var predictedLabel      = "…"

    /* ───── UI helper ───── */
    private val uiUpdateHandler = Handler(Looper.getMainLooper())
    private lateinit var uiUpdateRunnable: Runnable

    companion object { private const val TAG = "DetectActivity" }

    /* ════════════════════════════════════════════════════════════════ */
    /* Lifecycle                          */
    /* ════════════════════════════════════════════════════════════════ */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /* tombol ganti kamera */
        binding.cameraSwitchButton.setOnClickListener { switchCamera() }

        /* data dari intent DebugActivity */
        plannedExercises = intent.getParcelableArrayListExtra<Bundle>("plannedExercises")
            ?.map { b ->
                mapOf(
                    "actionName" to (b.getString("actionName") ?: ""),
                    "targetReps" to b.getInt("targetReps", 1)
                )
            } ?: emptyList()
        maxDurationPerRep = intent.getIntExtra("maxDurationPerRep", 20)

        /* eksekutor background */
        backgroundExecutor = Executors.newSingleThreadExecutor()

        /* start kamera */
        binding.viewFinder.post { setUpCamera() }

        /* timer perbarui UI */
        uiUpdateRunnable = object : Runnable {
            override fun run() {
                currentTime = System.currentTimeMillis()
                updateUI()
                uiUpdateHandler.postDelayed(this, 80)
            }
        }
        uiUpdateHandler.post(uiUpdateRunnable)

        bootstrap()
    }

    /* ───────────────────────── bootstrap ───────────────────────── */
    private fun bootstrap() {
        /* inisialisasi MediaPipe Pose di thread background */
        backgroundExecutor.execute {
            poseLandmarkerHelper = PoseLandmarkerHelper(
                context                    = this@DetectActivity,
                runningMode                = RunningMode.LIVE_STREAM,
                minPoseDetectionConfidence = 0.5f,
                minPoseTrackingConfidence  = 0.5f,
                minPosePresenceConfidence  = 0.5f,
                currentDelegate            = PoseLandmarkerHelper.DELEGATE_GPU,
                poseLandmarkerHelperListener = this@DetectActivity
            )
        }
        loadAssets()
    }

    /* ───────────────────────── load TFLite & scaler ───────────────────────── */
    private fun loadAssets() {
        try {
            /* model */
            val fd = assets.openFd("cnn_best.tflite")
            val buffer = fd.createInputStream().channel.map(
                FileChannel.MapMode.READ_ONLY, fd.startOffset, fd.declaredLength
            )
            interpreter = Interpreter(buffer, Interpreter.Options().apply { numThreads = 4 })
            Log.d(TAG, "TFLite model loaded")

            /* label aksi */
            actions = assets.open("actions.txt").bufferedReader()
                .readLines().map { it.trim() }.filter { it.isNotEmpty() }
            Log.d(TAG, "Actions loaded: $actions")

            /* scaler mean & scale */
            assets.open("scaler.json").use { s ->
                val j = org.json.JSONObject(String(s.readBytes()))
                meanScaler  = FloatArray(j.getJSONArray("mean").length()) { i ->
                    j.getJSONArray("mean").getDouble(i).toFloat()
                }
                scaleScaler = FloatArray(j.getJSONArray("scale").length()) { i ->
                    j.getJSONArray("scale").getDouble(i).toFloat()
                }
            }

            runOnUiThread {
                startExercise()
                updateUI()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading assets: ${e.message}", e)
            showErrorAndPop("Gagal memuat aset: ${e.message}")
        }
    }

    /* ════════════════════════════════════════════════════════════════ */
    /* CameraX                            */
    /* ════════════════════════════════════════════════════════════════ */

    private fun setUpCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture?.addListener({
            cameraProvider = cameraProviderFuture?.get()
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(this))
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun bindCameraUseCases() {
        cameraProvider?.unbindAll()

        val selector = CameraSelector.Builder().requireLensFacing(cameraFacing).build()

        preview = Preview.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(binding.viewFinder.display.rotation)
            .build().also { it.setSurfaceProvider(binding.viewFinder.surfaceProvider) }

        imageAnalyzer = ImageAnalysis.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(binding.viewFinder.display.rotation)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build().also { ia ->
                ia.setAnalyzer(backgroundExecutor) { img -> detectPose(img) }
            }

        try {
            camera = cameraProvider?.bindToLifecycle(
                this, selector, preview, imageAnalyzer
            )
        } catch (e: Exception) {
            Log.e(TAG, "Use-case binding failed", e)
            showErrorAndPop("Gagal membuka kamera: ${e.message}")
        }
    }

    private fun detectPose(imageProxy: ImageProxy) {
        cameraImageWidth  = imageProxy.width
        cameraImageHeight = imageProxy.height
        poseLandmarkerHelper?.detectLiveStream(imageProxy, isFrontCamera)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        imageAnalyzer?.targetRotation = binding.viewFinder.display.rotation
    }

    /* ════════════════════════════════════════════════════════════════ */
    /* Callback dari PoseLandmarkerHelper (Listener)           */
    /* ════════════════════════════════════════════════════════════════ */

    override fun onResults(resultBundle: PoseLandmarkerHelper.ResultBundle) {
        runOnUiThread {
            if (isFinishing || isDestroyed) return@runOnUiThread

            /* gambar landmark */
            binding.overlay.setResults(
                resultBundle.results.first(),
                resultBundle.inputImageHeight,
                resultBundle.inputImageWidth
            )
            binding.overlay.isFrontCamera = isFrontCamera
            binding.overlay.hideLegs = maskLegsForCurrentExercise
            binding.overlay.invalidate()

            /* ekstrak fitur utk model */
            // Dapatkan hasil PoseLandmarker secara keseluruhan
            val poseLandmarkerResult = resultBundle.results.first()

            // Dapatkan daftar PoseLandmark (ini yang punya 'visibility')
            val rawPoseLandmarks = poseLandmarkerResult.landmarks().firstOrNull() // Ini adalah List<NormalizedLandmark>

            // Dapatkan juga daftar Landmark yang lebih kaya (memiliki visibility)
            val fullPoseLandmarks = poseLandmarkerResult.worldLandmarks().firstOrNull() // Menggunakan world landmarks, atau landmarks() jika Anda hanya butuh normalized dan cek visibility.

            // Ambil normalized landmarks untuk input model
            val normalizedLandmarksForModel = poseLandmarkerResult.landmarks().firstOrNull()

            // Ambil world landmarks untuk mendapatkan informasi visibility
            // (karena NormalizedLandmark di Android MediaPipe Tasks tidak punya visibility)
            val worldLandmarksForVisibility = poseLandmarkerResult.worldLandmarks().firstOrNull()


            if (normalizedLandmarksForModel != null) {
                val currentActionName = currentExercisePerformance?.actionName ?: ""
                val isUpperBodyActionForModel = upperBodyOnlyActions.contains(currentActionName)

                sequenceBuffer.add(
                    extractAndNormalizeKeypoints(
                        normalizedLandmarksForModel,
                        worldLandmarksForVisibility, // Kirim world landmarks untuk cek visibility
                        isUpperBodyActionForModel
                    )
                )
            } else {
                sequenceBuffer.add(FloatArray(numFeat) { 0f })
            }

            // Kelola buffer sekuens
            if (sequenceBuffer.size > seqLen) sequenceBuffer.removeFirst()

            // Logika untuk memulai model setelah countdown selesai dan buffer terisi
            val countdownDone = preparationEndTime == null || currentTime >= (preparationEndTime ?: 0)
            if (sequenceBuffer.size == seqLen && countdownDone) runModel()

            // Perbarui instruksi UI
            if (preparationEndTime != null && countdownDone &&
                currentInstruction.startsWith("Bersiap")
            ) currentInstruction = "Ikuti gerakan"

            updateUI()
        }
    }

    override fun onError(error: String, errorCode: Int) {
        runOnUiThread {
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            Log.e(TAG, "PoseLandmarker error: $error ($errorCode)")
        }
    }

    /* ════════════════════════════════════════════════════════════════ */
    /* Alur Latihan & State Machine                 */
    /* ════════════════════════════════════════════════════════════════ */

    private fun startExercise() {
        if (currentExerciseIndex >= plannedExercises.size) {
            finishSession(); return
        }
        val plan = plannedExercises[currentExerciseIndex]
        currentExercisePerformance = ExercisePerformance(
            plan["actionName"] as String,
            plan["targetReps"] as Int
        )
        // maskLegsForCurrentExercise hanya mengontrol apa yang digambar di UI overlay
        maskLegsForCurrentExercise =
            upperBodyOnlyActions.contains(currentExercisePerformance!!.actionName)

        currentAttempt = 1
        resetRepetitionState(clearSequence = true)
        Log.d(TAG, "Mulai gerakan: ${currentExercisePerformance!!.actionName}")
    }

    private fun resetRepetitionState(clearSequence: Boolean = false) {
        if (clearSequence) sequenceBuffer.clear()

        currentTime = System.currentTimeMillis()
        preparationEndTime =
            if (currentAttempt == 1) currentTime + prepSec * 1000L else null
        repetitionStartTime  = preparationEndTime ?: currentTime
        perfectPoseStartTime = null
        isRepetitionComplete = false

        currentQualityLabel = "Tidak Terdeteksi"
        currentInstruction  =
            if (currentAttempt == 1) "Bersiap…" else "Ikuti gerakan"
        predictedLabel = "…"
        updateUI()
    }

    private fun completeRepetition(result: String) {
        if (isRepetitionComplete) return
        isRepetitionComplete = true

        currentExercisePerformance!!.recordAttempt(result)
        currentInstruction = if (result == "Sempurna") "BERHASIL!" else "WAKTU HABIS!"
        updateUI()

        Handler(Looper.getMainLooper()).postDelayed({
            if (isFinishing || isDestroyed) return@postDelayed

            if (currentExercisePerformance!!.completedReps +
                currentExercisePerformance!!.failedAttempts <
                currentExercisePerformance!!.targetReps
            ) {
                currentAttempt++
                resetRepetitionState()
            } else {
                sessionSummary.add(currentExercisePerformance!!)
                currentExerciseIndex++
                startExercise()
            }
        }, 2000)
    }

    /* ═════════════ Landmark → Fitur ═════════════ */
    private fun extractAndNormalizeKeypoints(
        normalizedLandmarks: List<NormalizedLandmark>, // Ini yang akan digunakan untuk x, y
        worldLandmarks: List<Landmark>?,
        maskLegsForModel: Boolean
    ): FloatArray {
        val feat = FloatArray(numFeat)
        val mirror = isFrontCamera

        for (i in 0 until 33) {
            val ixX = i * 2
            val ixY = i * 2 + 1

            val legPart = LEG_INDICES.contains(i)
            val maskedForAction = maskLegsForModel && legPart

            // ===== PERBAIKAN DIMULAI DI SINI =====
            // Cek konfidensi/visibilitas dari worldLandmarks
            val invisibleByConfidence = if (worldLandmarks != null && i < worldLandmarks.size) {
                // 1. Ambil 'kotak' Optional<Float> dari .visibility()
                val visibilityOptional = worldLandmarks[i].visibility()

                // 2. Cek apakah 'kotak' punya isi. Jika ya, ambil isinya (.get()) lalu bandingkan.
                //    Jika tidak, anggap saja tidak terlihat (true).
                if (visibilityOptional.isPresent) {
                    visibilityOptional.get() < TH_VIS
                } else {
                    true
                }
            } else {
                // Jika worldLandmarks null atau indeks tidak valid, anggap tidak terlihat
                true
            }
            // ===== PERBAIKAN SELESAI =====


            if (maskedForAction || invisibleByConfidence) {
                feat[ixX] = meanScaler[ixX]
                feat[ixY] = meanScaler[ixY]
            } else {
                // Pastikan menggunakan normalizedLandmarks untuk koordinat x, y
                if (i < normalizedLandmarks.size) {
                    val lm = normalizedLandmarks[i]
                    var x = lm.x()
                    val y = lm.y()
                    if (mirror) x = 1f - x
                    feat[ixX] = x
                    feat[ixY] = y
                } else {
                    // Fallback jika entah bagaimana normalizedLandmarks tidak lengkap
                    feat[ixX] = meanScaler[ixX]
                    feat[ixY] = meanScaler[ixY]
                }
            }
        }
        return feat
    }

    /* ═════════════ Inference ═════════════ */
    private fun runModel() {
        val flat = FloatArray(seqLen * numFeat)
        var k = 0
        sequenceBuffer.forEach { frame ->
            System.arraycopy(frame, 0, flat, k, numFeat); k += numFeat
        }

        val scaled = FloatArray(flat.size)
        for (i in flat.indices) {
            val j = i % numFeat
            scaled[i] = (flat[i] - meanScaler[j]) / (scaleScaler[j] + 1e-8f)
        }

        /* bentuk input: [1, seqLen, numFeat] */
        val input = Array(1) { Array(seqLen) { FloatArray(numFeat) } }
        for (t in 0 until seqLen)
            for (f in 0 until numFeat)
                input[0][t][f] = scaled[t * numFeat + f]

        val output = Array(1) { FloatArray(actions.size) }
        interpreter.run(input, output)

        val probs = output[0]
        val best  = probs.indices.maxByOrNull { probs[it] } ?: -1
        predictedLabel = if (best != -1) actions[best] else "…"

        updateExerciseState(probs[best])
    }

    private fun updateExerciseState(confidence: Float) {
        val ex = currentExercisePerformance ?: return
        if (isRepetitionComplete ||
            (preparationEndTime != null && currentTime < preparationEndTime!!)
        ) return

        val now = System.currentTimeMillis()
        val target = ex.actionName

        var newInstr  = currentInstruction
        var newQlty   = currentQualityLabel

        if (predictedLabel == target && confidence >= needConf) {
            newQlty = "Sempurna"; newInstr = "TAHAN!"
            perfectPoseStartTime = perfectPoseStartTime ?: now
            if (now - (perfectPoseStartTime ?: now) >= holdSec * 1000L) {
                completeRepetition("Sempurna"); return
            }
        } else {
            newQlty = "Belum Tepat"; newInstr = "Ikuti gerakan"
            perfectPoseStartTime = null
        }

        if (currentInstruction != newInstr) currentInstruction = newInstr
        if (currentQualityLabel != newQlty) currentQualityLabel = newQlty

        if (now - repetitionStartTime > maxDurationPerRep * 1000L &&
            !isRepetitionComplete
        ) completeRepetition("Waktu Habis")

        updateUI()
    }

    /* ═════════════ UI ═════════════ */
    private fun updateUI() {
        if (isFinishing || isDestroyed) return
        val ex = currentExercisePerformance ?: return

        binding.tvExerciseName.text =
            "Gerakan: ${ex.actionName}"
        binding.tvRepetitions.text =
            "Repetisi: ${ex.completedReps}/${ex.targetReps} (Percobaan: $currentAttempt)"

        val inPrep = preparationEndTime != null && currentTime < (preparationEndTime ?: 0)
        binding.countdownText.visibility  = if (inPrep) View.VISIBLE else View.GONE
        binding.qualityLabel.visibility   = if (inPrep) View.GONE   else View.VISIBLE
        binding.instructionText.visibility= if (inPrep) View.GONE   else View.VISIBLE
        binding.predictedLabel.visibility = if (inPrep) View.GONE   else View.VISIBLE
        binding.progressBar.visibility    =
            if (isRepetitionComplete || inPrep) View.GONE else View.VISIBLE

        if (inPrep) {
            val remain = ((preparationEndTime ?: currentTime) - currentTime) / 1000
            binding.countdownText.text = "Bersiap… ${max(0, remain.toInt())}"
        } else {
            binding.qualityLabel.text =
                "Kualitas: $currentQualityLabel"
            binding.qualityLabel.setTextColor(
                if (currentQualityLabel == "Sempurna")
                    ContextCompat.getColor(this, R.color.perfect_quality_color)
                else ContextCompat.getColor(this, R.color.imperfect_quality_color)
            )
            binding.instructionText.text = "Instruksi: $currentInstruction"
            binding.predictedLabel.text  = "Prediksi: $predictedLabel"

            val progress =
                min(1f, (currentTime - repetitionStartTime).toFloat() /
                        (maxDurationPerRep * 1000L))
            binding.progressBar.progress = (progress * 100).toInt()
        }
    }

    /* ═════════════ Sesi selesai ═════════════ */
    private fun finishSession() {
        uiUpdateHandler.removeCallbacks(uiUpdateRunnable)
        Log.d(TAG, "Session finished")

        val totalOk  = sessionSummary.sumOf { it.completedReps }
        val totalNg  = sessionSummary.sumOf { it.failedAttempts }

        AlertDialog.Builder(this)
            .setTitle("Sesi Latihan Selesai!")
            .setMessage(
                "Sempurna : $totalOk\n" +
                        "Tidak Tepat : $totalNg\n\n" +
                        sessionSummary.joinToString("\n") {
                            "${it.actionName}: ${it.completedReps} Sempurna, ${it.failedAttempts} Gagal"
                        }
            )
            .setPositiveButton("OK") { d, _ -> d.dismiss(); finish() }
            .setCancelable(false).show()
    }

    private fun showErrorAndPop(msg: String) {
        runOnUiThread {
            AlertDialog.Builder(this)
                .setTitle("Error").setMessage(msg)
                .setPositiveButton("OK") { d, _ -> d.dismiss(); finish() }
                .setCancelable(false).show()
        }
    }

    /* ────────── cleanup ────────── */
    override fun onDestroy() {
        super.onDestroy()
        uiUpdateHandler.removeCallbacks(uiUpdateRunnable)

        backgroundExecutor.shutdown()
        try { backgroundExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS) }
        catch (e: InterruptedException) { Log.e(TAG, "Executor shutdown interrupted", e) }

        poseLandmarkerHelper?.clearPoseLandmarker()
        interpreter.close()
    }

    /* ────────── switch camera ────────── */
    @SuppressLint("MissingPermission")
    private fun switchCamera() {
        cameraProvider?.unbindAll()
        cameraFacing = if (cameraFacing == CameraSelector.LENS_FACING_BACK)
            CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
        bindCameraUseCases()
    }
}