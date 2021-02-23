package com.wanderlust.community_antiepidemic_system.entity

data class AntiepidemicRsp(

    val country: Statistics?,

    val dataSourceUpdateTime: Time?,

    val provinceArray: MutableList<Statistics> = mutableListOf()

)

data class Statistics(

    val totalCured     : String = "",

    val totalDeath     : String = "",

    val totalIncrease  : String = "",

    val incDoubtful    : String = "",

    val childStatistic : String = "",

    val totalDoubtful  : String = "",

    val time           : String = "",

    val totalConfirmed : String = ""

)

data class Time (

    val updateTime: String = "",

    val dataSource: String = ""

)