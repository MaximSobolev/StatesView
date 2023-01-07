package ru.netology.statesview.util

import android.content.Context
import kotlin.math.ceil

object AndroidUtils {
    fun dp(context: Context, dp: Float): Int =
        if (dp == 0F) 0 else ceil(context.resources.displayMetrics.density * dp).toInt()
}