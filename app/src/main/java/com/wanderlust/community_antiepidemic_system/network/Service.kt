package com.wanderlust.community_antiepidemic_system.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Service {

    //后台服务器URL
    const val SERVICE_URL = "http://10.173.165.67:8080/wanderlust-cas-service/"

    //疫情统计数据的URL
    const val STATIC_DATA_URL = "https://ncovdata.market.alicloudapi.com/"

    //疫情风险地区API的URL
    const val AREA_DATA_URL = "http://103.66.32.242:8005/zwfwMovePortal/interface/"

    val request: ApiService = Retrofit.Builder()
        .baseUrl(SERVICE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)

    val staticData: ApiService = Retrofit.Builder()
        .baseUrl(STATIC_DATA_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)

    val areaData: ApiService = Retrofit.Builder()
        .baseUrl(AREA_DATA_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)

}