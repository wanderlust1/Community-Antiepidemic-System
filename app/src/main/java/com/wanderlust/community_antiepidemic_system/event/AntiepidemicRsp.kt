package com.wanderlust.community_antiepidemic_system.event

data class AntiepidemicRsp(

    val country: Statistics?,

    val dataSourceUpdateTime: Time?,

    val provinceArray: MutableList<Statistics> = mutableListOf()

)

data class Statistics(

    val totalCured     : String = "", //累计治愈

    val totalDeath     : String = "", //累计死亡

    val totalIncrease  : String = "", //累计新增

    val incDoubtful    : String = "", //新增疑似病例

    val childStatistic : String = "",

    val totalDoubtful  : String = "", //累计疑似病例

    val time           : String = "",

    val totalConfirmed : String = "" //累计确诊

    //现存确诊 = totalConfirmed - totalCured - totalDeath

)

data class Time (

    val updateTime: String = "",

    val dataSource: String = ""

)