package com.wanderlust.community_antiepidemic_system.event

import com.wanderlust.community_antiepidemic_system.entity.Community

interface CommunityEvent {

    companion object {
        const val SUCC = 0
        const val FAIL = 1
        const val NEW_JOIN = 11 //首次加入社区
        const val CHANGE = 12   //更改已选择的社区
    }

    data class SearchReq(val keywords: String, val userId: String)

    data class SearchRsp(val code: Int, val result: List<Community> = mutableListOf())

    data class JoinReq(val userId: String, val oldCommunityId: String = "", val newCommunityId: String, val type: Int)

    data class JoinRsp(val code: Int, val msg: String)

}