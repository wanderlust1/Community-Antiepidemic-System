package com.wanderlust.community_antiepidemic_system.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.util.Log
import androidx.annotation.DrawableRes
import com.baidu.location.*
import com.google.gson.Gson
import com.tencent.mmkv.MMKV
import com.wanderlust.community_antiepidemic_system.event.DiseaseDataEvent
import com.wanderlust.community_antiepidemic_system.event.RiskAreaEvent
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

object CommonUtils {

    fun dp2px(context: Context, dpValue: Float): Int {
        return (dpValue * context.resources.displayMetrics.density + 0.5f).toInt()
    }

    fun formatLocType(type: Int): String {
        when (type) {
            BDLocation.TypeGpsLocation -> { // GPS定位结果
                return "GPS定位成功"
            }
            BDLocation.TypeNetWorkLocation -> { // 网络定位结果
                return "网络定位成功"
            }
            BDLocation.TypeOffLineLocation -> { // 离线定位结果
                return "离线定位成功"
            }
            BDLocation.TypeServerError -> {
                return "服务端网络定位失败"
            }
            BDLocation.TypeNetWorkException -> {
                return "定位失败，请检查网络是否通畅"
            }
            BDLocation.TypeCriteriaException -> {
                return "定位失败，请检查手机定位服务或重启手机"
            }
        }
        return ""
    }

    fun drawBitmapFromVector(context: Context, @DrawableRes vectorResId: Int): Bitmap? {
        val vectorDrawable = context.getDrawable(vectorResId) ?: return null
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        vectorDrawable.draw(canvas)
        return bitmap
    }

    //启动单次定位
    fun startOneLocation(locClient: LocationClient, listener: BDAbstractLocationListener) {
        locClient.registerLocationListener(listener)
        val option = LocationClientOption()
        option.locationMode = LocationClientOption.LocationMode.Hight_Accuracy
        option.setCoorType("bd09ll")
        option.setScanSpan(0)
        option.setOnceLocation(true)
        option.isOpenGps = true
        option.setIsNeedAddress(true)
        locClient.locOption = option
        locClient.start()
    }

    //动态申请定位权限
    fun requestPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= 23) {
            val permissionsList: ArrayList<String> = ArrayList()
            val permissions = arrayOf(
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_WIFI_STATE)
            for (perm in permissions) {
                if (PackageManager.PERMISSION_GRANTED != activity.checkSelfPermission(perm)) {
                    permissionsList.add(perm)
                }
            }
            if (permissionsList.isNotEmpty()) {
                val strings = arrayOfNulls<String>(permissionsList.size)
                activity.requestPermissions(permissionsList.toArray(strings), 0)
            }
        }
    }

    /**
     * 将风险地区信息保存至本地，记录时间戳。
     * 避免频繁请求接口，减小开销
     */
    fun saveRiskAreaMMKV(saver: MMKV, result: RiskAreaEvent.RiskAreaRsp?) {
        if (result == null) return
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(Calendar.getInstance().time)
        saver.encode("RiskAreaRsp_time", date)
        saver.encode("RiskAreaRsp_data", Gson().toJson(result))
        Log.d("RiskAreaRsp", "已缓存至本地，date=${date}")
    }

    /**
     * 读取已保存至本地的风险地区信息。避免频繁请求接口，减小开销
     * （如果上次保存时间距今大于一天，则认为本地信息已过期，需要重新网络请求）
     */
    fun readRiskAreaMMKV(reader: MMKV): RiskAreaEvent.RiskAreaRsp? {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(Calendar.getInstance().time)
        val oldDate = reader.decodeString("RiskAreaRsp_time")
        if (oldDate.isNullOrEmpty() || oldDate != date) {
            return null
        }
        val data = reader.decodeString("RiskAreaRsp_data")
        if (data.isNullOrEmpty()) {
            return null
        }
        Log.d("RiskAreaRsp", "已从本地载入缓存")
        return Gson().fromJson(data, RiskAreaEvent.RiskAreaRsp::class.java)
    }

    /**
     * 将疫情统计数据保存至本地，记录时间戳。
     * 避免频繁请求接口，减小开销
     */
    fun saveDiseaseDataMMKV(saver: MMKV, result: DiseaseDataEvent.AntiepidemicRsp?) {
        if (result == null) return
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(Calendar.getInstance().time)
        saver.encode("AntiepidemicRsp_time", date)
        saver.encode("AntiepidemicRsp_data", Gson().toJson(result))
        Log.d("AntiepidemicRsp", "已缓存至本地，date=${date}")
    }

    /**
     * 读取已保存至本地的疫情统计数据。避免频繁请求接口，减小开销
     * （如果上次保存时间距今大于一天，则认为本地信息已过期，需要重新网络请求）
     */
    fun readDiseaseDataMMKV(reader: MMKV): DiseaseDataEvent.AntiepidemicRsp? {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(Calendar.getInstance().time)
        val oldDate = reader.decodeString("AntiepidemicRsp_time")
        if (oldDate.isNullOrEmpty() || oldDate != date) {
            return null
        }
        val data = reader.decodeString("AntiepidemicRsp_data")
        if (data.isNullOrEmpty()) {
            return null
        }
        Log.d("AntiepidemicRsp", "已从本地载入缓存")
        return Gson().fromJson(data, DiseaseDataEvent.AntiepidemicRsp::class.java)
    }


}