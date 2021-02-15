package com.wanderlust.community_antiepidemic_system

import com.wanderlust.community_antiepidemic_system.entity.RiskAreaReq
import com.wanderlust.community_antiepidemic_system.entity.RiskAreaRsp
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiService {

    @Headers("Content-Type:application/json; charset=utf-8")
    @POST("interfaceJson")
    fun getRiskAreaData(@Body riskAreaReq: RiskAreaReq) : Call<ResponseBody>

}