package com.wanderlust.community_antiepidemic_system.entity

data class CommunityStatistics (

    var total: Int = 0, //社区总人数

    var submit: Int = 0, //近一个月提交了健康登记的人数

    var noneSubmit: Int = 0, //未提交健康登记的人数

    var health: Int = 0, //健康状态正常的人数

    var highTemper: Int = 0, //发烧，但没有确诊和接触的人数

    var diagnose: Int = 0, //确诊病例数

    var suspect: Int = 0, //疑似病例数

    var approach: Int = 0, //密切接触者数

    var outside: Int = 0, //近一个月登记过外出的人数

    var outsideRegs: List<OutSideReg> = listOf(), //近一个月的外出记录

    var midRisk: Int = 0, //近一个月到达过中风险地区的人数

    var highRisk: Int = 0 //近一个月到达过高风险地区的人数

)
