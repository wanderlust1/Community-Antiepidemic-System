package com.wanderlust.community_antiepidemic_system.entity

data class QRCodeMessage (

    var color: Int = 0,

    var cause: Int = 0,

    var message: String = ""

) {

    companion object {
        const val GREEN  = 0 //绿码
        const val RED    = 1 //红码
        const val YELLOW = 2 //黄码

        const val RED_APPROACH = 10
        const val RED_DIAGNOSE = 11
        const val RED_ENTER_HIGH_RISK = 12
        const val RED_ENTER_MID_RISK = 14

        const val YELLOW_ENTER_MID_RISK = 15
        const val YELLOW_FEVER = 16
    }

}