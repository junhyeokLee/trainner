package com.sports2i.trainer.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class LineChart @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val dataPoints = listOf(
        DataPoint(1f, 50f),
        DataPoint(2f, 80f),
        DataPoint(3f, 60f),
        DataPoint(4f, 40f),
        DataPoint(5f, 70f)
    )

    private val linePaint = Paint().apply {
        color = android.graphics.Color.BLUE
        style = Paint.Style.STROKE
        strokeWidth = 5f
        isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawLineChart(canvas)
    }

    private fun drawLineChart(canvas: Canvas?) {
        val maxX = dataPoints.maxByOrNull { it.x }?.x ?: 1f
        val maxY = dataPoints.maxByOrNull { it.y }?.y ?: 1f

        val scale = 0.8f // 그래픽이 차트 영역의 80%를 차지하도록 스케일 조절

        val chartWidth = width.toFloat() * scale
        val chartHeight = height.toFloat() * scale

        val xScale = chartWidth / maxX
        val yScale = chartHeight / maxY

        val xOffset = (width - chartWidth) / 2
        val yOffset = (height - chartHeight) / 2

        val path = android.graphics.Path()
        dataPoints.forEachIndexed { index, point ->
            val x = xOffset + point.x * xScale
            val y = yOffset + chartHeight - point.y * yScale

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        canvas?.drawPath(path, linePaint)
    }

    data class DataPoint(val x: Float, val y: Float)
}
