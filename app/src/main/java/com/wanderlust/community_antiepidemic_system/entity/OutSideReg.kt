package com.wanderlust.community_antiepidemic_system.entity

data class OutSideReg (

    var userId: String = "",

    var date: String = "",

    var city: String = "",

    var startTime: String = "",

    var endTime: String = "",

    var reason: String = "",

    var phone: String = "",

    var isRiskArea: Boolean = false

)