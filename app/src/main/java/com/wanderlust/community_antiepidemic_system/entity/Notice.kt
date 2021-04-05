package com.wanderlust.community_antiepidemic_system.entity

data class Notice (

    var id: String = "",

    var content: String = "",

    var title: String = "",

    var date: String = "",

    var communityId: String = "", //公告所属的社区

    var type: Int = 0,

    var hasRead: Int = 0,  //当前公告是否被当前用户标记为已读，只在user请求时可见

    var readCount: Int = 0 //当前公告标记为已读的用户，只在admin请求时可见

) {
    companion object {
        const val NOTICE_NORMAL = 1 //普通公告
        const val NOTICE_ALERT  = 2 //健康登记提醒公告
    }
}