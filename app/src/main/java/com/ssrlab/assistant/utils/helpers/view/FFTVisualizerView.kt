package com.ssrlab.assistant.utils.helpers.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.ssrlab.assistant.R

class FFTVisualizerView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val paint = Paint()
    private var fftData = FloatArray(0)

    fun updateFFTData(fftData: FloatArray) {
        this.fftData = fftData
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (fftData.isNotEmpty()) {
            val width = width.toFloat()
            val height = height.toFloat()
            val halfWidth = width / 2
            val barWidth = halfWidth / (fftData.size / 2)

            val heightFraction = 0.5f

            for (i in 0 until fftData.size / 2) {
                val xLeft = halfWidth - (i * barWidth)
                val xRight = halfWidth + (i * barWidth)

                var reducedBarHeight = 0f
                if (fftData[i] > 0.25) reducedBarHeight = fftData[i] * height * heightFraction

                val top = (height - reducedBarHeight) / 2
                val bottom = height - top

                canvas.drawRect(xLeft, top, xLeft + barWidth, bottom, paint)
                canvas.drawRect(xRight, top, xRight + barWidth, bottom, paint)
            }
        }
    }


    init {
        paint.color = ContextCompat.getColor(context, R.color.record_wave)
    }
}