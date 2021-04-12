package com.wanderlust.community_antiepidemic_system.network

import android.util.Log
import com.tencent.mmkv.MMKV
import com.wanderlust.community_antiepidemic_system.R
import com.wanderlust.community_antiepidemic_system.activities.home_user.UserHomeActivity
import com.wanderlust.community_antiepidemic_system.event.DiseaseDataEvent
import com.wanderlust.community_antiepidemic_system.event.RiskAreaEvent
import com.wanderlust.community_antiepidemic_system.utils.CommonUtils
import com.wanderlust.community_antiepidemic_system.utils.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.ConnectException
import java.util.*

object ServiceManager {

    //后台服务器URL 192.168.43.13
    private const val SERVICE_URL = "http://192.168.43.13:8080/wanderlust-cas-service/"

    //疫情统计数据的URL
    private const val STATIC_DATA_URL = "https://ncovdata.market.alicloudapi.com/"

    //疫情风险地区API的URL
    private const val AREA_DATA_URL = "http://103.66.32.242:8005/zwfwMovePortal/interface/"

    val client: ApiService = Retrofit.Builder()
        .baseUrl(SERVICE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)

    suspend inline fun <reified Rsp> request(crossinline buildClient: (client: ApiService) -> Call<Rsp>): Rsp? {
        val response = try {
            withContext(Dispatchers.IO) {
                buildClient.invoke(client).execute()
            }
        } catch (e: ConnectException) {
            R.string.connection_error.toast()
            null
        } catch (e: Exception) {
            e.printStackTrace()
            R.string.timeout_error.toast()
            null
        }
        return response?.body()
    }

    /**
     * 请求疫情统计数据（若本地数据未过期则优先取本地），并将结果保存至本地。
     */
    suspend fun requestDiseaseStaticData(): DiseaseDataEvent.AntiepidemicRsp? {
        val kv = MMKV.defaultMMKV()
        val localResult = CommonUtils.readDiseaseDataMMKV(kv)
        return if (localResult != null) {
            localResult
        } else {
            val response = withContext(Dispatchers.IO) {
                try {
                    Retrofit.Builder()
                        .baseUrl(STATIC_DATA_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                        .create(ApiService::class.java).getAntiepidemicData().execute()
                } catch (e: ConnectException) {
                    R.string.connection_error.toast()
                    null
                } catch (e: Exception) {
                    e.printStackTrace()
                    R.string.other_error.toast()
                    null
                }
            }
            Log.d(UserHomeActivity.TAG, response?.body().toString())
            if (response?.body() == null) return null
            CommonUtils.saveDiseaseDataMMKV(kv, response.body())
            response.body()!!
        }
    }

    /**
     * 请求疫情地区数据（若本地数据未过期则优先取本地），并将结果保存至本地。
     */
    suspend fun requestRiskAreaData(): RiskAreaEvent.RiskAreaRsp? {
        val kv = MMKV.defaultMMKV()
        val localResult = CommonUtils.readRiskAreaMMKV(kv) //从本地读数据
        return if (localResult != null) {
            localResult
        } else {
            //获得当前时间戳
            val timestamp = System.currentTimeMillis() / 1000
            //封装Header数据
            val client = withContext(Dispatchers.IO) {
                OkHttpClient.Builder().addInterceptor(object : Interceptor {
                    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
                        val signatureStr = "$timestamp${RiskAreaEvent.RiskAreaReq.STATE_COUNCIL_SIGNATURE_KEY}$timestamp"
                        val signature = RiskAreaEvent.RiskAreaReq.getSHA256StrJava(signatureStr).toUpperCase(Locale.ROOT)
                        val build = chain.request().newBuilder()
                            .addHeader("x-wif-nonce", RiskAreaEvent.RiskAreaReq.STATE_COUNCIL_X_WIF_NONCE)
                            .addHeader("x-wif-paasid", RiskAreaEvent.RiskAreaReq.STATE_COUNCIL_X_WIF_PAASID)
                            .addHeader("x-wif-signature", signature)
                            .addHeader("x-wif-timestamp", timestamp.toString())
                            .build()
                        return chain.proceed(build)
                    }
                }).retryOnConnectionFailure(true).build()
            }
            val response = withContext(Dispatchers.IO) {
                try {
                    Retrofit.Builder()
                        .baseUrl(AREA_DATA_URL)
                        .client(client)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                        .create(ApiService::class.java)
                        .getRiskAreaData(RiskAreaEvent.RiskAreaReq(timestamp = timestamp)).execute()
                } catch (e: ConnectException) {
                    R.string.connection_error.toast()
                    null
                } catch (e: Exception) {
                    R.string.timeout_error.toast()
                    null
                }
            }
            Log.d("RiskAreaRsp", "onResponse: " + response?.body())
            CommonUtils.saveRiskAreaMMKV(kv, response?.body()) //由MMKV保存至本地
            response?.body()
        }
    }
}
