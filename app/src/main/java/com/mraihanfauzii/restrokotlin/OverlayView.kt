package com.mraihanfauzii.restrokotlin

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

class OverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var result: PoseLandmarkerResult? = null

    /* ukuran frame asli */
    private var imgW = 1
    private var imgH = 1
    private var sX   = 1f
    private var sY   = 1f

    var isFrontCamera = false
    var hideLegs      = false

    private val LEG_IDS      = (23..32).toSet()
    private val MIN_PRESENCE = 0.40f

    /* 🎨 kuas */
    private val ptPaint = Paint().apply {
        color = Color.CYAN
        style = Paint.Style.FILL
        strokeWidth = 8f
        isAntiAlias = true
    }
    private val lnPaint = Paint().apply {
        color = Color.YELLOW
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }

    // ────────── public API ──────────
    fun setResults(
        res: PoseLandmarkerResult,
        imageHeight: Int,
        imageWidth : Int,
        mode: RunningMode = RunningMode.IMAGE
    ) {
        result = res
        imgH   = imageHeight
        imgW   = imageWidth
        sX     = width  / imgW.toFloat()
        sY     = height / imgH.toFloat()
        invalidate()
    }

    fun clear() {
        result = null
        invalidate()
    }

    // ────────── rendering ──────────
    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        val res = result ?: return
        if (res.landmarks().isEmpty()) return

        val pose  = res.landmarks().first()              // 33 landmark
        val edges = PoseLandmarker.POSE_LANDMARKS

        /* ----- garis tulang ----- */
        for (e in edges) {
            val a = e.start();  val b = e.end()
            if (hideLegs && (a in LEG_IDS || b in LEG_IDS)) continue

            val lmA = pose[a];  val lmB = pose[b]
            val pA  = lmA.presence().orElse(0f)
            val pB  = lmB.presence().orElse(0f)
            if (pA < MIN_PRESENCE || pB < MIN_PRESENCE) continue

            val ax = lmA.x() * imgW * sX
            val ay = lmA.y() * imgH * sY
            val bx = lmB.x() * imgW * sX
            val by = lmB.y() * imgH * sY

            val fx = if (isFrontCamera) canvas.width - ax else ax
            val gx = if (isFrontCamera) canvas.width - bx else bx
            canvas.drawLine(fx, ay, gx, by, lnPaint)
        }

        /* ----- titik landmark ----- */
        pose.forEachIndexed { idx, lm ->
            if (hideLegs && idx in LEG_IDS) return@forEachIndexed
            val p = lm.presence().orElse(0f)
            if (p < MIN_PRESENCE) return@forEachIndexed

            val x  = lm.x() * imgW * sX
            val y  = lm.y() * imgH * sY
            val fx = if (isFrontCamera) canvas.width - x else x
            canvas.drawCircle(fx, y, ptPaint.strokeWidth / 2, ptPaint)
        }
    }
}
