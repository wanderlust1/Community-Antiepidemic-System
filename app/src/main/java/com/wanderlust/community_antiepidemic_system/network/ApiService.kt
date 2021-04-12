package com.wanderlust.community_antiepidemic_system.network

import com.wanderlust.community_antiepidemic_system.event.*
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @Headers(
        "Content-Type:application/json; charset=utf-8",
        "User-Agent:Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                + "AppleWebKit/537.36 (KHTML, like Gecko) "
                + "Chrome/88.0.4324.150 Safari/537.36"
    )
    @POST("interfaceJson")
    fun getRiskAreaData(@Body riskAreaReq: RiskAreaEvent.RiskAreaReq) : Call<RiskAreaEvent.RiskAreaRsp>

    @Headers(
        "Authorization:APPCODE 71ca0d51918e44838eb127c40213e577",
        "Content-Type:application/json; charset=utf-8"
    )
    @GET("ncov/cityDiseaseInfoWithTrend")
    fun getAntiepidemicData() : Call<DiseaseDataEvent.AntiepidemicRsp>

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("login")
    fun login(@Body body: RequestBody): Call<UserEvent.LoginRsp>

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("register")
    fun register(@Body body: RequestBody): Call<UserEvent.RegisterRsp>

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("searchCommunity")
    fun searchCommunity(@Body body: RequestBody): Call<CommunityEvent.SearchRsp>

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("joinCommunity")
    fun joinCommunity(@Body body: RequestBody): Call<CommunityEvent.JoinRsp>

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("getTemperRecord")
    fun getTemperRecord(@Body body: RequestBody): Call<RegEvent.GetTemperRecordRsp>

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("getOutsideRecord")
    fun getOutsideRecord(@Body body: RequestBody): Call<RegEvent.GetOutSideRecordRsp>

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("addTemperReg")
    fun addTemperReg(@Body body: RequestBody): Call<RegEvent.AddTemperRecordRsp>

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("addOutsideReg")
    fun addOutsideReg(@Body body: RequestBody): Call<RegEvent.AddOutsideRecordRsp>

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("requestQRContent")
    fun getQRContent(@Body body: RequestBody): Call<QRCodeEvent.QRContentRsp>

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("createCommunity")
    fun createCommunity(@Body body: RequestBody): Call<CommunityEvent.CreateCommunityRsp>

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("adminBindCommunity")
    fun adminBindCommunity(@Body body: RequestBody): Call<CommunityEvent.AdminBindCommunityRsp>

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("addNotice")
    fun addNotice(@Body body: RequestBody): Call<NoticeEvent.AddNoticeRsp>

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("setNoticeRead")
    fun setNoticeRead(@Body body: RequestBody): Call<NoticeEvent.SetNoticeReadRsp>

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("getNoticesList")
    fun getNoticesList(@Body body: RequestBody): Call<NoticeEvent.GetNoticesListRsp>

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("getNoReadCount")
    fun getNoReadCount(@Body body: RequestBody): Call<NoticeEvent.GetNoReadCountRsp>

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("deleteNotice")
    fun deleteNotice(@Body body: RequestBody): Call<NoticeEvent.DeleteNoticeRsp>

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("getCommunityUsers")
    fun getCommunityUsers(@Body body: RequestBody): Call<UserEvent.GetCommunityUsersRsp>

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("kickUser")
    fun kickUser(@Body body: RequestBody): Call<UserEvent.KickUserRsp>

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("getCommunityMessage")
    fun getCommunityMessage(@Body body: RequestBody): Call<CommunityEvent.CommunityMessageRsp>

}