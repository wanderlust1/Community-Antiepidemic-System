package com.wanderlust.community_antiepidemic_system.utils

import android.content.Context
import android.content.SyncStatusObserver
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.baidu.mapapi.map.BitmapDescriptorFactory
import com.baidu.mapapi.map.InfoWindow
import com.baidu.mapapi.map.MarkerOptions
import com.baidu.mapapi.search.core.PoiInfo
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.wanderlust.community_antiepidemic_system.R
import com.wanderlust.community_antiepidemic_system.WanderlustApp
import com.wanderlust.community_antiepidemic_system.activities.home_admin.AdminHomeActivity
import com.wanderlust.community_antiepidemic_system.activities.notice.EditNoticeActivity
import com.wanderlust.community_antiepidemic_system.network.ServiceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import java.net.ConnectException

/**
 * KT扩展函数工具
 * @author Wanderlust
 */
fun String.toast(context: Context? = WanderlustApp.context) {
    if (context != null) {
        Toast.makeText(context, this, Toast.LENGTH_SHORT).show()
    }
}

fun Int.toast(context: Context? = WanderlustApp.context) {
    if (context != null) {
        Toast.makeText(context, this, Toast.LENGTH_SHORT).show()
    }
}

fun Any.toJsonRequest(): RequestBody {
    return Gson().toJson(this).toRequestBody()
}

fun PoiInfo.drawMarker(context: Context, isSelf: Boolean, text: String? = null): MarkerOptions {
    //绘制图标
    val icon = CommonUtils.drawBitmapFromVector(context,
        if (isSelf) R.drawable.ic_location_blue else R.drawable.ic_location_red)
    val markerOptions = MarkerOptions()
        .position(getLocation())
        .icon(BitmapDescriptorFactory.fromBitmap(icon))
    //标记点上的label，显示名称
    val textView = TextView(context).apply {
        this.textSize = if (isSelf) 13f else 12f
        this.text = text ?: getName()
        setTextColor(if (isSelf) Color.WHITE else Color.BLACK)
        setPadding(10, 5, 10, 5)
        background = ContextCompat.getDrawable(context,
            if (isSelf) R.drawable.bg_label_map_mark_blue else R.drawable.bg_label_map_mark)
    }
    return markerOptions.scaleX(0.8f).scaleY(0.8f).infoWindow(InfoWindow(textView, getLocation(), -60))
}

fun TextInputEditText.addErrorTextWatcher(parent: TextInputLayout, errorText: String) {
    addTextChangedListener(object: TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (text.isNullOrBlank()) {
                parent.error = errorText
            } else {
                parent.isErrorEnabled = false
            }
        }
        override fun afterTextChanged(s: Editable?) {

        }
    })
}

fun EditText.addLimitTextWatcher(limit: Int, textChangeObserver: (currLen: Int) -> Unit) {
    addTextChangedListener(object: TextWatcher {
        private var limit = EditNoticeActivity.MAX_CONTENT_LEN
        private var cursor = 0
        private var beforeLength = 0
        override fun beforeTextChanged (s: CharSequence, start: Int, count: Int, after: Int) {
            beforeLength = s.length
        }
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            cursor = start
        }
        override fun afterTextChanged(s: Editable) {
            val afterLength = s.length
            if (afterLength > limit) {
                val inputNum = afterLength - beforeLength
                val start = cursor + (inputNum - afterLength + limit)
                val end   = cursor + inputNum
                setText(s.delete(start, end).toString())
                setSelection(start)
            }
            textChangeObserver.invoke(afterLength.coerceAtMost(limit))
        }
    })
}