package com.wanderlust.community_antiepidemic_system.event

import com.wanderlust.community_antiepidemic_system.entity.Admin
import com.wanderlust.community_antiepidemic_system.entity.User

interface UserEvent {

    companion object {
        const val SUCC = 0
        const val FAIL = 1
        const val EXISTED = 2
    }

    data class LoginReq(val user: User? = null, val admin: Admin? = null, val loginType: Int)

    data class LoginRsp(val code: Int, val msg: String, val user: User? = null, val admin: Admin? = null)

    data class LoginQueryRsp(val user: User? = null, val admin: Admin? = null, val code: Int)

    data class RegisterReq(val user: User? = null, val admin: Admin? = null, val loginType: Int)

    data class RegisterRsp(val code: Int, val msg: String)

    data class GetCommunityUsersReq(val communityId: String)

    data class GetCommunityUsersRsp(val result: List<User>)

    data class KickUserReq(val userId: String, var communityId: String)

    data class KickUserRsp(val code: Int, val msg: String)

}