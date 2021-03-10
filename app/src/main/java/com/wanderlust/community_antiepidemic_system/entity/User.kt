package com.wanderlust.community_antiepidemic_system.entity

import com.google.gson.annotations.SerializedName

data class User (

    @SerializedName("user_id")
    var userId: String = "",

    var password: String = "",

    @SerializedName("user_name")
    var userName: String = "",

    var cid: String = ""

)