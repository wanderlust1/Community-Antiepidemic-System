package com.wanderlust.community_antiepidemic_system.utils

object HealthType {

    const val HEALTH    = 1 // 健康
    const val FEVER     = 2 // 发烧（其他原因如感冒//，最新体温记录为37.3°C以上

    const val NONE      = 10 // 非确诊或疑似病例
    const val DIAGNOSE  = 11 // 确诊或诊断阳性人员
    const val SUSPECT   = 12 // 疑似病例

    const val APPROACH_NONE     = 20 // 非密切接触者
    const val APPROACH_SUSPECT  = 21 // 密切接触者（接触疑似）
    const val APPROACH_DIAGNOSE = 22 // 密切接触者（接触确诊）

    fun toString(type: Int): String {
        return when (type) {
            NONE              -> "未被确诊"
            DIAGNOSE          -> "新冠肺炎确诊病例"
            SUSPECT           -> "新冠肺炎疑似病例"
            APPROACH_NONE     -> "近期未接触过疑似或确诊病例"
            APPROACH_DIAGNOSE -> "近期接触过确诊病例"
            APPROACH_SUSPECT  -> "近期接触过疑似病例"
            else -> ""
        }
    }

}