package com.wanderlust.community_antiepidemic_system.entity

import java.util.*

data class TemperReg (

    var userId: String = "",

    var date: String = "",

    var temper: String = "",

    var status: String = "",

    var approach: Int = 0,

    var diagnose: Int = 0,

    var isDanger: Boolean = false,

    var dateFormat: Date? = null

)