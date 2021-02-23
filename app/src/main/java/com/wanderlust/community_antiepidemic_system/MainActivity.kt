package com.wanderlust.community_antiepidemic_system

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.wanderlust.community_antiepidemic_system.entity.AntiepidemicRsp
import com.wanderlust.community_antiepidemic_system.entity.RiskAreaReq
import com.wanderlust.community_antiepidemic_system.entity.RiskAreaRsp
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var mBtnAdd: Button
    private lateinit var mTvNum: TextView

    private lateinit var mViewModel: NumberViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
        Thread(Runnable {
            //postRetrofit()
        }).start()
    }

    private fun init() {
        mBtnAdd = findViewById(R.id.btn_add)
        mTvNum = findViewById(R.id.tv_num)
        //获取ViewModel
        mViewModel = ViewModelProvider(this, NumberViewModel.NumberViewModelFactory()).get(NumberViewModel::class.java)
        //使用ViewModel中的数据
        mTvNum.text = mViewModel.mNum.toString()

        mBtnAdd.setOnClickListener {
            mViewModel.mNum = mViewModel.mNum + 1
            mTvNum.text = mViewModel.mNum.toString()
        }
        mTvNum.setOnClickListener {
            startActivity(Intent(this, MapActivity::class.java))
        }
    }

    private fun postRetrofit() {
        //疫情风险地区API的URL
        val baseUrl = "http://103.66.32.242:8005/zwfwMovePortal/interface/"
        //获得当前时间戳
        val timestamp = System.currentTimeMillis() / 1000
        //封装Header数据
        val client = OkHttpClient.Builder().addInterceptor(object : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                val signatureStr = "$timestamp${RiskAreaReq.STATE_COUNCIL_SIGNATURE_KEY}$timestamp"
                val signature = RiskAreaReq.getSHA256StrJava(signatureStr).toUpperCase(Locale.ROOT)
                val build = chain.request().newBuilder()
                    .addHeader("x-wif-nonce", RiskAreaReq.STATE_COUNCIL_X_WIF_NONCE)
                    .addHeader("x-wif-paasid", RiskAreaReq.STATE_COUNCIL_X_WIF_PAASID)
                    .addHeader("x-wif-signature", signature)
                    .addHeader("x-wif-timestamp", timestamp.toString())
                    .build()
                return chain.proceed(build)
            }
        }).retryOnConnectionFailure(true).build()
        //发送请求
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(ApiService::class.java)
            .getRiskAreaData(RiskAreaReq(timestamp = timestamp))
            .enqueue(object : retrofit2.Callback<RiskAreaRsp> {
                override fun onFailure(call: Call<RiskAreaRsp>, t: Throwable) {
                    Log.d("aaa", "onFailure: " + t.message)
                }
                override fun onResponse(call: Call<RiskAreaRsp>, response: retrofit2.Response<RiskAreaRsp>) {
                    Log.d("aaa", "onResponse: " + response.body())
                    mTvNum.text = response.body().toString()
                }
            })
    }


    private fun postRetrofit2() {
        //疫情统计数据API的URL
        val baseUrl = "https://ncovdata.market.alicloudapi.com/"
        //发送请求
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
            .getAntiepidemicData()
            .enqueue(object : retrofit2.Callback<AntiepidemicRsp> {
                override fun onFailure(call: Call<AntiepidemicRsp>, t: Throwable) {
                    Log.d("aaa", "onFailure: " + t.message)
                }
                override fun onResponse(call: Call<AntiepidemicRsp>, response: retrofit2.Response<AntiepidemicRsp>) {
                    Log.d("aaa", "onResponse: " + response.body())
                    mTvNum.text = response.body().toString()
                }
            })
    }

}