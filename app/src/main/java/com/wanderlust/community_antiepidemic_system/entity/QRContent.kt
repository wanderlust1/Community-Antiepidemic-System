package com.wanderlust.community_antiepidemic_system.entity

import com.wanderlust.community_antiepidemic_system.utils.HealthType

/**
 * @see com.wanderlust.community_antiepidemic_system.utils.HealthType
 */
data class QRContent(

    //二维码内容
    var content: Content = Content(),

    //最新体温
    var temperature: String = "",

    //最新确诊状况
    var diagnose: Int = HealthType.NONE,

    //最新接触史
    var approach: Int = HealthType.APPROACH_NONE,

    //最近一个月外出地区
    var outside: MutableList<String> = mutableListOf()

)

data class Content(

    var label: String = "",

    var name: String = "",

    var cid: String = "",

    var phone: String = "",

    var time: String = "",

    var encode: String = ""

)