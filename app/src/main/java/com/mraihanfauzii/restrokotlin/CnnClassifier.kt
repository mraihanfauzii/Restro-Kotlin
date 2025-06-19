package com.mraihanfauzii.restrokotlin

import android.content.Context
import org.tensorflow.lite.Interpreter
import org.json.JSONObject
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.max

class CnnClassifier(ctx: Context) {

    /** skor probabilitas kelas terbaik pada inferensi terakhir */
    var confidence = 0f
        private set

    /* ─────────── konstanta input ─────────── */
    private val BATCH    = 1
    private val SEQ_LEN  = 60
    private val FEAT_DIM = 66          // 33 landmark × (x,y)
    private val CHANNELS = 1           // dummy dim biar [1,60,66,1]

    /* ─────────── aset model ─────────── */
    private val interpreter: Interpreter
    private val mean   : FloatArray
    private val scale  : FloatArray
    private val actions: List<String>
    private val numClasses: Int

    /** lock agar Interpreter.run() selalu single-thread */
    private val lock = Any()

    init {
        /* — model — */
        interpreter = Interpreter(
            loadModel(ctx, "cnn_best.tflite"),
            Interpreter.Options().apply { setNumThreads(4) }
        )

        /* bentuk tensor output -> [1, numClasses] */
        numClasses = interpreter.getOutputTensor(0).shape()[1]

        /* — scaler — */
        val scaler = JSONObject(loadAsset(ctx, "scaler.json"))
        mean  = scaler.getJSONArray("mean" ).toFloatArray()
        scale = scaler.getJSONArray("scale").toFloatArray()

        /* — label aksi — */
        actions = loadAsset(ctx, "actions.txt")
            .split('\n').map { it.trim() }.filter { it.isNotEmpty() }

        require(actions.size == numClasses) {
            "actions.txt (${actions.size}) ≠ model output ($numClasses)"
        }
    }

    /**
     * @param seq  float rata berukuran 60×66 = 3 960
     * @return     label aksi dengan probabilitas terbesar
     */
    fun classify(seq: FloatArray): String {
        require(seq.size == SEQ_LEN * FEAT_DIM) {
            "Input length harus ${SEQ_LEN * FEAT_DIM}, tetapi ${seq.size}"
        }

        /* ───── 1. normalisasi ───── */
        val scaled = FloatArray(seq.size) { i ->
            val j = i % FEAT_DIM
            (seq[i] - mean[j]) / (scale[j] + 1e-8f)
        }

        /* ───── 2. reshape -> [1,60,66,1] ───── */
        val input =
            Array(BATCH) {
                Array(SEQ_LEN) {
                    Array(FEAT_DIM) {
                        FloatArray(CHANNELS)
                    }
                }
            }

        var idx = 0
        for (t in 0 until SEQ_LEN)
            for (f in 0 until FEAT_DIM) {
                input[0][t][f][0] = scaled[idx++]
            }

        /* ───── 3. inferensi ───── */
        val output = Array(BATCH) { FloatArray(numClasses) }
        synchronized(lock) {
            interpreter.run(input, output)
        }

        /* ───── 4. ambil kelas terbaik ───── */
        val probs = output[0]
        var bestIdx = 0
        var bestVal = -1f
        for (i in probs.indices) if (probs[i] > bestVal) {
            bestVal = probs[i]; bestIdx = i
        }

        confidence = bestVal
        return actions[bestIdx]
    }

    fun close() = interpreter.close()

    /* ─────────── helper private ─────────── */

    private fun loadModel(ctx: Context, file: String): MappedByteBuffer =
        FileInputStream(ctx.assets.openFd(file).fileDescriptor).channel.map(
            FileChannel.MapMode.READ_ONLY,
            ctx.assets.openFd(file).startOffset,
            ctx.assets.openFd(file).declaredLength
        )

    private fun loadAsset(ctx: Context, file: String): String =
        ctx.assets.open(file).bufferedReader().use { it.readText() }

    private fun org.json.JSONArray.toFloatArray() =
        FloatArray(length()) { getDouble(it).toFloat() }
}
