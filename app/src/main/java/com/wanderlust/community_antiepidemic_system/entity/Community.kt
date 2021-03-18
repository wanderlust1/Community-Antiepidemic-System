package com.wanderlust.community_antiepidemic_system.entity

import com.google.gson.annotations.SerializedName

data class Community (

    @SerializedName("community_id") val id: String = "",

    @SerializedName("community_name") val name: String = "",

    @SerializedName("menber_count") val count: Int = 0,

    val location: String = "",

    val hasJoined: Int = 0 //0为当前用户未加入此社区，1为已加入

)