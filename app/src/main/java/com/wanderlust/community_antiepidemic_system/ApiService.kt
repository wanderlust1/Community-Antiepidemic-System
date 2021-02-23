package com.wanderlust.community_antiepidemic_system

import com.wanderlust.community_antiepidemic_system.entity.AntiepidemicRsp
import com.wanderlust.community_antiepidemic_system.entity.RiskAreaReq
import com.wanderlust.community_antiepidemic_system.entity.RiskAreaRsp
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiService {

    @Headers(
        "Content-Type:application/json; charset=utf-8",
        "User-Agent:Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                + "AppleWebKit/537.36 (KHTML, like Gecko) "
                + "Chrome/88.0.4324.150 Safari/537.36"
    )
    @POST("interfaceJson")
    fun getRiskAreaData(@Body riskAreaReq: RiskAreaReq) : Call<RiskAreaRsp>

    @Headers(
        "Authorization:APPCODE 71ca0d51918e44838eb127c40213e577",
        "Content-Type:application/json; charset=utf-8"
    )
    @GET("/ncov/cityDiseaseInfoWithTrend")
    fun getAntiepidemicData() : Call<AntiepidemicRsp>

}