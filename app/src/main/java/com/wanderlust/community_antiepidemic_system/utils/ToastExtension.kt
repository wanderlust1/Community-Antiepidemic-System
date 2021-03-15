package com.wanderlust.community_antiepidemic_system.utils

import android.content.Context
import android.widget.Toast

fun String.toast(context: Context) {
    Toast.makeText(context, this, Toast.LENGTH_SHORT).show()
}

fun Int.toast(context: Context) {
    Toast.makeText(context, this, Toast.LENGTH_SHORT).show()
}