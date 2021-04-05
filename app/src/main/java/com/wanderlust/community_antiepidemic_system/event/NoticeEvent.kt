package com.wanderlust.community_antiepidemic_system.event

import com.wanderlust.community_antiepidemic_system.entity.Notice

interface NoticeEvent {

    companion object {
        const val SUCC = 0
        const val FAIL = 1
    }

    data class AddNoticeReq(val notice: Notice)

    data class AddNoticeRsp(val code: Int, val msg: String)

    data class GetNoticesListReq(val userId: String, val communityId: String, val type: Int)

    data class GetNoticesListRsp(val result: List<Notice> = mutableListOf())

    data class GetNoReadCountReq(val userId: String, val communityId: String)

    data class GetNoReadCountRsp(val noReadCount: Int)

    data class SetNoticeReadReq(val userId: String, val noticeId: String, val communityId: String)

    data class SetNoticeReadRsp(val noReadCount: Int, val code: Int, val msg: String)

}