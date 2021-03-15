package com.wanderlust.community_antiepidemic_system.entity

import com.google.gson.annotations.SerializedName

data class Admin (

    @SerializedName("admin_id")
    var adminId: String = "",

    var password: String = "",

    @SerializedName("community_name")
    var communityName: String = ""

)