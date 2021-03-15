package com.wanderlust.community_antiepidemic_system.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import androidx.annotation.DrawableRes
import com.baidu.location.*

object MapUtils {

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

}