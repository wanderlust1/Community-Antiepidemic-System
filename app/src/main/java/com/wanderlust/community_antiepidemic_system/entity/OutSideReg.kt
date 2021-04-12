package com.wanderlust.community_antiepidemic_system.entity

import java.util.*

data class OutSideReg (

    var userId: String = "",

    var date: String = "",

    var city: String = "",

    var startTime: String = "",

    var endTime: String = "",

    var reason: String = "",

    var phone: String = "",

    var isRiskArea: Boolean = false,

    var dateFormat: Date? = null

)