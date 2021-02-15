package com.wanderlust.community_antiepidemic_system.entity

import com.google.gson.annotations.SerializedName

data class RiskAreaRsp (

    val data: RiskAreaData,

    val code: Int = 1,

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

    val province: String = "",

    val city: String = "",

    val county: String = "",

    val communitys: MutableList<String> = mutableListOf()

)