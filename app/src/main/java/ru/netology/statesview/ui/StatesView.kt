package ru.netology.statesview.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import ru.netology.statesview.R
import ru.netology.statesview.util.AndroidUtils
import ru.netology.statesview.util.StatesViewKey
import java.lang.Integer.min
import kotlin.collections.ArrayList
import kotlin.random.Random

class StatesView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(
    context,
    attributeSet,
    defStyleAttr,
    defStyleRes
) {
    private var lineWidth = AndroidUtils.dp(context, 5F).toFloat()
    private var fontSize = AndroidUtils.dp(context, 20F).toFloat()
    private var colors = emptyList<Int>()
    private var circleColor = randomColor()

    init {

        context.withStyledAttributes(attributeSet, R.styleable.StatesView) {
            lineWidth = getDimension(R.styleable.StatesView_lineWidth, lineWidth)
            fontSize = getDimension(R.styleable.StatesView_fontSize, fontSize)
            colors = listOf(
                getColor(
                    R.styleable.StatesView_color1,
                    randomColor()
                ),
                getColor(
                    R.styleable.StatesView_color2,
                    randomColor()
                ),
                getColor(
                    R.styleable.StatesView_color3,
                    randomColor()
                ),
                getColor(
                    R.styleable.StatesView_color4,
                    randomColor()
                )
            )
            circleColor = getColor(R.styleable.StatesView_circleColor, randomColor())
        }
    }
    var data : Map<StatesViewKey, List<Float>> = emptyMap()
        set(value) {
            field = value
            invalidate()
        }

    private var radius = 0F
    private var center = PointF(0F, 0F)
    private var oval = RectF()
    private var strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = lineWidth
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        color = circleColor
    }
    private var textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = fontSize
    }
    private var pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        strokeWidth = lineWidth
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        color = colors[0]
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        radius = min(w, h) / 2F - lineWidth
        center = PointF(w / 2F, h / 2F)
        oval = RectF(
            center.x - radius, center.y - radius,
            center.x + radius, center.y + radius
        )

    }

    override fun onDraw(canvas: Canvas) {
        if (data.isEmpty()) {
            return
        }

        val percent = findPercent()
        var startFrom = -90F
        canvas.drawCircle(center.x, center.y, radius, strokePaint)
        data[StatesViewKey.DATA]?.let { dataList ->
            for ((index, _) in dataList.withIndex()) {
                val angle = 360F * percent[index]
                strokePaint.color = colors[index]
                canvas.drawArc(oval, startFrom, angle, false, strokePaint)
                startFrom += angle
            }
        }
        canvas.drawPoint(center.x, center.y - radius, pointPaint)
        canvas.drawText(
            "%.2f%%".format(percent.sum() * 100),
            center.x,
            center.y + textPaint.textSize / 4,
            textPaint
        )
    }

    private fun randomColor(): Int =
        Random.nextInt(0xFF000000.toInt(), 0xFFFFFFFF.toInt())

    private fun findPercent() : List<Float> {
        if (data[StatesViewKey.FULL_AMOUNT].isNullOrEmpty() && data[StatesViewKey.DATA].isNullOrEmpty()) {
            return emptyList()
        } else {
            val sum = data[StatesViewKey.FULL_AMOUNT]?.get(0)
            val list = ArrayList<Float>()
            if (sum != 0F) {
                data[StatesViewKey.DATA]?.let { dataList ->
                    for (datum in dataList) {
                        list.add(
                            datum / (sum
                                ?: throw java.lang.Exception("FULL_AMOUNT can't be null or 0"))
                        )
                    }
                }
            }
            return list
        }
    }
}