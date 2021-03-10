package com.wanderlust.community_antiepidemic_system

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.wanderlust.community_antiepidemic_system.entity.AntiepidemicRsp
import com.wanderlust.community_antiepidemic_system.map.MapActivity
import com.wanderlust.community_antiepidemic_system.qrcode.QRCodeActivity
import com.wanderlust.community_antiepidemic_system.utils.DensityUtils
import com.wanderlust.community_antiepidemic_system.utils.QRCodeUtils
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.lang.Runnable
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity() {

    private lateinit var mBtnAdd: Button
    private lateinit var mTvNum: TextView

    private lateinit var mViewModel: NumberViewModel

    var mCount = 0

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
            startActivity(Intent(this, QRCodeActivity::class.java))
        }
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