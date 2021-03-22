package com.wanderlust.community_antiepidemic_system.entity

data class TemperReg (

    var userId: String = "",

    var date: String = "",

    var temper: String = "",

    var status: String = "",

    var approach: String = "",

    var isDanger: Boolean = false

)