package com.wanderlust.community_antiepidemic_system.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.text.TextUtils
import androidx.annotation.DrawableRes
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.Poi
import java.text.SimpleDateFormat
import java.util.*

object MapUtils {
    fun getLocationStr(location: BDLocation?, locationClient: LocationClient?): String? {
        if (null == location) {
            return null
        }
        val sb = StringBuffer(256)
        sb.append("\n定位时间 : ")
        sb.append(location.time)
        sb.append("回调时间: ${formatDateTime(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss")}".trimIndent())
        sb.append("\n定位类型 : ") // 定位类型
        sb.append(location.locType)
        sb.append("\n经度 : ") // 纬度
        sb.append(location.longitude)
        sb.append("\n纬度 : ") // 经度
        sb.append(location.latitude)
        sb.append("\n精度 : ") // 半径
        sb.append(location.radius)
        if (location.locType == BDLocation.TypeNetWorkLocation) { // 网络定位结果
            // 运营商信息
            if (location.hasAltitude()) { // *****如果有海拔高度*****
                sb.append("海拔高度 : ")
                sb.append(location.altitude) // 单位：米
            }
        }
        sb.append("\n方向: ")
        sb.append(location.direction) // 方向
        sb.append("\n国家编码 : ") // 国家码
        sb.append(location.countryCode)
        sb.append("\n国家 : ") // 城市
        sb.append(location.country)
        sb.append("\n省份 : ") // 获取省份
        sb.append(location.province)
        sb.append("\n城市编码 : ") // 城市编码
        sb.append(location.cityCode)
        sb.append("\n城市 : ") // 国家名称
        sb.append(location.city)
        sb.append("\n区县 : ") // 区
        sb.append(location.district)
        sb.append("\n乡镇街道 : ") // 获取镇信息
        sb.append(location.town)
        sb.append("\n地址 : ") // 地址信息
        sb.append(location.addrStr)
        sb.append("\n附近街道 : ") // 街道
        sb.append(location.street)
        sb.append("\n室内外结果 : ") // *****返回用户室内外判断结果*****
        sb.append(location.userIndoorState)
        sb.append("\n位置语义化 : ")
        sb.append(location.locationDescribe) // 位置语义化信息
        sb.append("\nPOI兴趣点 : ") // POI信息
        if (location.poiList != null && !location.poiList.isEmpty()) {
            for (i in location.poiList.indices) {
                val poi = location.poiList[i] as Poi
                sb.append("""POI名称 : """)
                sb.append(poi.name + ", ")
                sb.append("POI类型 : ")
                sb.append(poi.tags)
            }
        }
        if (location.locType == BDLocation.TypeGpsLocation) { // GPS定位结果
            sb.append("\n速度 : ")
            sb.append(location.speed) // 速度 单位：km/h
            sb.append("\n卫星数 : ")
            sb.append(location.satelliteNumber) // 卫星数目
            sb.append("\n海拔高度 : ")
            sb.append(location.altitude) // 海拔高度 单位：米
        }
        sb.append("\nSDK版本 : ")
        if (null != locationClient) {
            val version = locationClient.version // 获取sdk版本
            sb.append(version)
        }
        return sb.toString()
    }

    fun getLocDiagnosticStr(locType: Int, diagnosticType: Int, diagnosticMessage: String): String {
        val sb = StringBuffer(256)
        sb.append("诊断结果: ")
        if (locType == BDLocation.TypeNetWorkLocation) {
            if (diagnosticType == 1) {
                sb.append("网络定位成功，没有开启GPS，建议打开GPS会更好")
                sb.append("""
    
    $diagnosticMessage
    """.trimIndent())
            } else if (diagnosticType == 2) {
                sb.append("网络定位成功，没有开启Wi-Fi，建议打开Wi-Fi会更好")
                sb.append("""
    
    $diagnosticMessage
    """.trimIndent())
            }
        } else if (locType == BDLocation.TypeOffLineLocationFail) {
            if (diagnosticType == 3) {
                sb.append("定位失败，请您检查您的网络状态")
                sb.append("""
    
    $diagnosticMessage
    """.trimIndent())
            }
        } else if (locType == BDLocation.TypeCriteriaException) {
            if (diagnosticType == 4) {
                sb.append("定位失败，无法获取任何有效定位依据")
                sb.append("""
    
    $diagnosticMessage
    """.trimIndent())
            } else if (diagnosticType == 5) {
                sb.append("定位失败，无法获取有效定位依据，请检查运营商网络或者Wi-Fi网络是否正常开启，尝试重新请求定位")
                sb.append(diagnosticMessage)
            } else if (diagnosticType == 6) {
                sb.append("定位失败，无法获取有效定位依据，请尝试插入一张sim卡或打开Wi-Fi重试")
                sb.append("""
    
    $diagnosticMessage
    """.trimIndent())
            } else if (diagnosticType == 7) {
                sb.append("定位失败，飞行模式下无法获取有效定位依据，请关闭飞行模式重试")
                sb.append("""
    
    $diagnosticMessage
    """.trimIndent())
            } else if (diagnosticType == 9) {
                sb.append("定位失败，无法获取任何有效定位依据")
                sb.append("""
    
    $diagnosticMessage
    """.trimIndent())
            }
        } else if (locType == BDLocation.TypeServerError) {
            if (diagnosticType == 8) {
                sb.append("定位失败，请确认您定位的开关打开状态，是否赋予APP定位权限")
                sb.append("""
    
    $diagnosticMessage
    """.trimIndent())
            }
        }
        return sb.toString()
    }

    private var simpleDateFormat: SimpleDateFormat? = null

    fun formatDateTime(time: Long, strPattern: String?): String {
        var strPattern = strPattern
        if (TextUtils.isEmpty(strPattern)) {
            strPattern = "yyyy-MM-dd HH:mm:ss"
        }
        if (simpleDateFormat == null) {
            try {
                simpleDateFormat = SimpleDateFormat(strPattern, Locale.CHINA)
            } catch (e: Throwable) {
            }
        } else {
            simpleDateFormat!!.applyPattern(strPattern)
        }
        return if (simpleDateFormat == null) "NULL" else simpleDateFormat!!.format(time)
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

}