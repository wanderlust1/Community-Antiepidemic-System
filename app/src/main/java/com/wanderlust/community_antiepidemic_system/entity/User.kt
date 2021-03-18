package com.wanderlust.community_antiepidemic_system.entity

import com.google.gson.annotations.SerializedName

data class User (

    @SerializedName("user_id")
    var userId: String = "", //用户id

    var password: String = "", //密码

    @SerializedName("user_name")
    var userName: String = "", //真实姓名

    var cid: String = "", //身份证号

    var communityId: String = "", //所在社区

    var communityName: String = ""

)