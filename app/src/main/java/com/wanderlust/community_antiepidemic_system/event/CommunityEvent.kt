package com.wanderlust.community_antiepidemic_system.event

import com.wanderlust.community_antiepidemic_system.entity.Community
import com.wanderlust.community_antiepidemic_system.entity.CommunityStatistics

interface CommunityEvent {

    companion object {
        const val SUCC = 0
        const val FAIL = 1
        const val NEW_JOIN = 11 //首次加入社区
        const val CHANGE = 12   //更改已选择的社区
    }

    data class SearchReq(val keywords: String, val id: String, val loginType: Int)

    data class SearchRsp(val code: Int, val result: List<Community> = mutableListOf())

    data class JoinReq(val userId: String, val oldCommunityId: String = "", val newCommunityId: String, val type: Int)

    data class JoinRsp(val code: Int, val msg: String)

    data class CreateCommunityReq(val community: Community, val adminId: String)

    data class CreateCommunityRsp(val communityId: String, val code: Int, val msg: String)

    data class AdminBindCommunityReq(val adminId: String, val newCommunityId: String)

    data class AdminBindCommunityRsp(val code: Int, val msg: String)

    data class CommunityMessageReq(val adminId: String)

    data class CommunityMessageRsp(val community: Community, val statistics: CommunityStatistics, val code: Int)

}