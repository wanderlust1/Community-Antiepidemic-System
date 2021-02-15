package com.wanderlust.community_antiepidemic_system

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.wanderlust.community_antiepidemic_system.entity.RiskAreaReq
import com.wanderlust.community_antiepidemic_system.entity.RiskAreaRsp
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.and
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
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
            postRetrofit()
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
        val baseUrl = "http://103.66.32.242:8005/zwfwMovePortal/interface/"
        val timestamp = System.currentTimeMillis() / 1000
        val request = RiskAreaReq(timestamp = timestamp)
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
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
            .getRiskAreaData(request)
            .enqueue(object : retrofit2.Callback<ResponseBody> {
                override fun onFailure(call: retrofit2.Call<ResponseBody>, t: Throwable) {
                    Log.d("aaa", "onFailure: " + t.message)
                }
                override fun onResponse(call: retrofit2.Call<ResponseBody>, response: retrofit2.Response<ResponseBody>) {
                    Log.d("aaa", "onResponse: " + response.raw())
                    Log.d("aaa", "onResponse: " + response.body().toString())
                }
        })
    }

    private fun postRequestPayLoad() {
        val url = "http://103.66.32.242:8005/zwfwMovePortal/interface/interfaceJson"
        //获取时间戳
        val timestamp: Long = System.currentTimeMillis() / 1000
        //以下常量值都是从网页源代码上获取到的------start
        val STATE_COUNCIL_SIGNATURE_KEY = "fTN2pfuisxTavbTuYVSsNJHetwq5bJvCQkjjtiLM2dCratiA"
        val STATE_COUNCIL_X_WIF_NONCE = "QkjjtiLM2dCratiA"
        val STATE_COUNCIL_X_WIF_PAASID = "smt-application"
        //国务院疫情防控查询 appId
        val STATE_COUNCIL_APP_ID = "NcApplication"
        //国务院疫情防控查询 PASSID
        val STATE_COUNCIL_PASSID = "zdww"
        //国务院疫情防控查询 秘钥
        val STATE_COUNCIL_NONCE = "123456789abcdefg"
        //国务院疫情防控查询 token
        val STATE_COUNCIL_TOEKN = "23y0ufFl5YxIyGrI8hWRUZmKkvtSjLQA"
        //国务院疫情防控查询 key
        val STATE_COUNCIL_KEY = "3C502C97ABDA40D0A60FBEE50FAAD1DA"
        //计算签名要用的字符串
        val signatureStr = String.format("%d%s%d", timestamp, STATE_COUNCIL_SIGNATURE_KEY, timestamp)
        val signatureStr2 = String.format("%d%s%s%d", timestamp, STATE_COUNCIL_TOEKN, STATE_COUNCIL_NONCE, timestamp)
        val signatureHeader = getSHA256StrJava(signatureStr2)?.toUpperCase(Locale.ROOT) ?: ""
        //计算签名
        val signature: String = getSHA256StrJava(signatureStr)?.toUpperCase(Locale.ROOT) ?: ""
        val okHttpClient = OkHttpClient()
        
        val json = JSONObject()
        json.put("appId",STATE_COUNCIL_APP_ID)
        json.put("paasHeader",STATE_COUNCIL_PASSID)
        json.put("timestampHeader", timestamp.toString())
        json.put("nonceHeader",STATE_COUNCIL_NONCE)
        json.put("signatureHeader", signatureHeader)
        json.put("key",STATE_COUNCIL_KEY)
        val requestBody = json.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder().url(url)
                .addHeader("x-wif-nonce", STATE_COUNCIL_X_WIF_NONCE)
                .addHeader("x-wif-paasid", STATE_COUNCIL_X_WIF_PAASID)
                .addHeader("x-wif-signature", signature)
                .addHeader("x-wif-timestamp", java.lang.String.valueOf(timestamp))
                .post(requestBody).build()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("aaa", "onFailure: " + e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("aaa", response.protocol.toString() + " " + response.code + " " + response.message)
                val headers = response.headers
                for (i in 0 until headers.size) {
                    Log.d("aaa", headers.name(i) + ":" + headers.value(i))
                }
                Log.d("aaa", "onResponse: " + response.body?.string())
            }
        })
    }

    /**
     * SHA-256 加密
     * 国务院疫情风险接口
     * http://103.66.32.242:8005/zwfwMovePortal/interface/interfaceJson
     * http://bmfw.www.gov.cn/yqfxdjcx/risk.html
     * @param str 被加密的字符串
     * @return
     * @author 上善若水 on 2021/1/21 20:25
     */
    private fun getSHA256StrJava(str: String): String? {
        val messageDigest: MessageDigest
        var encodeStr = ""
        try {
            messageDigest = MessageDigest.getInstance("SHA-256")
            messageDigest.update(str.toByteArray(StandardCharsets.UTF_8))
            val bytes: ByteArray = messageDigest.digest()
            val stringBuffer = StringBuffer()
            var temp: String? = null
            for (i in bytes.indices) {
                temp = Integer.toHexString(bytes[i] and 0xFF)
                if (temp.length == 1) {
                    //1得到一位的进行补0操作
                    stringBuffer.append("0")
                }
                stringBuffer.append(temp)
            }
            encodeStr = stringBuffer.toString()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return encodeStr
    }

}