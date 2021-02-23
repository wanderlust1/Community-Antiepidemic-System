package com.wanderlust.community_antiepidemic_system.utils

import android.content.Context

object DensityUtils {

    fun dp2px(context: Context, dpValue: Float): Int {
        return (dpValue * context.resources.displayMetrics.density + 0.5f).toInt()
    }

}