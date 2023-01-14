package ru.netology.statesview.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
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
    private var animation = 1

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
            animation = getInteger(R.styleable.StatesView_animation, 1)
        }
    }

    private var progressParallel = 0F
    private var valueAnimator: ValueAnimator? = null
    private var progressSequential = mutableListOf(0F, 0F, 0F, 0F)
    private var valueAnimator0: ValueAnimator? = null
    private var valueAnimator1: ValueAnimator? = null
    private var valueAnimator2: ValueAnimator? = null
    private var valueAnimator3: ValueAnimator? = null


    var data: Map<StatesViewKey, List<Float>> = emptyMap()
        set(value) {
            field = value
            update()
        }

    private var radius = 0F
    private var center = PointF(0F, 0F)
    private var oval = RectF()
    private var strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = lineWidth
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private var circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = lineWidth
        color = circleColor
    }
    private var textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = fontSize
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
        canvas.drawCircle(center.x, center.y, radius, circlePaint)
        data[StatesViewKey.DATA]?.let { dataList ->
            for ((index, _) in dataList.withIndex()) {
                val angle = 360F * percent[index]
                strokePaint.color = colors[index]
                canvas.drawArc(
                    oval,
                    when (animation) {
                        0 -> startFrom + (360F * progressParallel)
                        1 -> startFrom
                        else -> throw Exception("Attr animation can only be 0 or 1")
                    },
                    when (animation) {
                        0 -> angle * progressParallel
                        1 -> angle * progressSequential[index]
                        else -> throw Exception("Attr animation can only be 0 or 1")
                    },
                    false,
                    strokePaint
                )
                startFrom += angle
            }
        }
        canvas.drawText(
            "%.2f%%".format(percent.sum() * 100),
            center.x,
            center.y + textPaint.textSize / 4,
            textPaint
        )
        strokePaint.color = colors[0]
        when (animation) {
            0 -> canvas.drawArc(oval, startFrom + (360F * progressParallel), 10F, false, strokePaint)
            1 -> canvas.drawArc(oval, -90F, 10F, false, strokePaint)
            else -> throw Exception("Attr animation can only be 0 or 1")
        }
    }

    private fun randomColor(): Int =
        Random.nextInt(0xFF000000.toInt(), 0xFFFFFFFF.toInt())

    private fun findPercent(): List<Float> {
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

    private fun update() {
        when (animation) {
            0 -> {
                valueAnimator?.let {
                    it.removeAllUpdateListeners()
                    it.cancel()
                }
                progressParallel = 0F

                valueAnimator = ValueAnimator.ofFloat(0F, 1F).apply {
                    addUpdateListener { anim ->
                        progressParallel = anim.animatedValue as Float
                        invalidate()
                    }
                    duration = 1500
                    interpolator = AccelerateDecelerateInterpolator()
                }.also {
                    it.start()
                }
            }
            1 -> {
                valueAnimator0?.let {
                    it.removeAllUpdateListeners()
                    it.cancel()
                }
                valueAnimator1?.let {
                    it.removeAllUpdateListeners()
                    it.cancel()
                }
                valueAnimator2?.let {
                    it.removeAllUpdateListeners()
                    it.cancel()
                }
                valueAnimator3?.let {
                    it.removeAllUpdateListeners()
                    it.cancel()
                }
                progressSequential = mutableListOf(0F, 0F, 0F, 0F)

                valueAnimator0 = ValueAnimator.ofFloat(0F, 1F).apply {
                    addUpdateListener { anim ->
                        progressSequential[0] = anim.animatedValue as Float
                        invalidate()
                    }
                    duration = 300
                    interpolator = AccelerateDecelerateInterpolator()
                }.also {
                    it.start()
                }
                valueAnimator1 = ValueAnimator.ofFloat(0F, 1F).apply {
                    addUpdateListener { anim ->
                        progressSequential[1] = anim.animatedValue as Float
                        invalidate()
                    }
                    duration = 300
                    interpolator = AccelerateDecelerateInterpolator()
                    startDelay = 300
                }.also {
                    it.start()
                }
                valueAnimator2 = ValueAnimator.ofFloat(0F, 1F).apply {
                    addUpdateListener { anim ->
                        progressSequential[2] = anim.animatedValue as Float
                        invalidate()
                    }
                    duration = 300
                    interpolator = AccelerateDecelerateInterpolator()
                    startDelay = 600
                }.also {
                    it.start()
                }
                valueAnimator3 = ValueAnimator.ofFloat(0F, 1F).apply {
                    addUpdateListener { anim ->
                        progressSequential[3] = anim.animatedValue as Float
                        invalidate()
                    }
                    duration = 300
                    interpolator = AccelerateDecelerateInterpolator()
                    startDelay = 900
                }.also {
                    it.start()
                }
            }
            else -> throw Exception("Attr animation can only be 0 or 1")
        }
    }
}