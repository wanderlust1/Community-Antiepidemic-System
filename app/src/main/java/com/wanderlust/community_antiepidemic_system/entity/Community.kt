package com.wanderlust.community_antiepidemic_system.entity

import com.google.gson.annotations.SerializedName

data class Community (

    @SerializedName("community_id") var id: String = "",

    @SerializedName("community_name") var name: String = "",

    @SerializedName("menber_count") var count: Int = 0,

    var location: String = "",

    var phone: String = "",

    var hasJoined: Int = 0 //0为当前用户未加入此社区，1为已加入

)