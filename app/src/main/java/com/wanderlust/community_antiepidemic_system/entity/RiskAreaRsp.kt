package com.wanderlust.community_antiepidemic_system.entity

import com.baidu.mapapi.model.LatLng
import com.google.gson.annotations.SerializedName

data class RiskAreaRsp (

    val data: RiskAreaData,

    val code: String = "",

    val msg: String = ""

)

data class RiskAreaData (

    @SerializedName("end_update_time") val time: String = "",

    @SerializedName("hcount") val highCount: Int = 0,

    @SerializedName("mcount") val midCount: Int = 0,

    @SerializedName("highlist") val highList: MutableList<Area> = mutableListOf(),

    @SerializedName("middlelist") val midList: MutableList<Area> = mutableListOf()

)

data class Area (

    var province: String = "",

    var city: String = "",

    var county: String = "",

    var communitys: MutableList<String> = mutableListOf(),

    var isHigh: Boolean = false, //true为高风险地区，false为低风险地区

    var position: LatLng? = null //中心位置坐标

)