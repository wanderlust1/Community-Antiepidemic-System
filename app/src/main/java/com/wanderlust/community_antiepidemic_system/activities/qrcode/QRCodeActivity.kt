package com.wanderlust.community_antiepidemic_system.activities.qrcode

import android.graphics.Bitmap
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.tencent.mmkv.MMKV
import com.wanderlust.community_antiepidemic_system.network.ApiService
import com.wanderlust.community_antiepidemic_system.R
import com.wanderlust.community_antiepidemic_system.entity.User
import com.wanderlust.community_antiepidemic_system.utils.DensityUtils
import com.wanderlust.community_antiepidemic_system.utils.MapUtils
import com.wanderlust.community_antiepidemic_system.utils.QRCodeUtils
import com.wanderlust.community_antiepidemic_system.utils.UrlUtils
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.CoroutineContext

class QRCodeActivity : AppCompatActivity(), CoroutineScope {

    private lateinit var mQRCode: ImageView
    private lateinit var mTvArea : TextView
    private lateinit var mTvCommunity : TextView
    private lateinit var mTvName : TextView
    private lateinit var mTvDate : TextView
    private lateinit var mTvHealthHint : TextView
    private lateinit var mRlDetail : RelativeLayout

    //协程
    private val mJob: Job by lazy { Job() }
    override val coroutineContext: CoroutineContext get() = mJob + Dispatchers.Main

    private val mLocationClient: LocationClient by lazy { LocationClient(this) }

    private val kv: MMKV by lazy { MMKV.defaultMMKV() }

    var mCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrcode)
        initView()
        mCountDownTimer.start()
        requestUserData()
        MapUtils.startOneLocation(mLocationClient, mLocationListener)
    }

    private fun initView() {
        mQRCode = findViewById(R.id.iv_qr_code)
        mTvArea = findViewById(R.id.tv_qr_area_name)
        mTvCommunity = findViewById(R.id.tv_qr_community_name)
        mTvName = findViewById(R.id.tv_qr_user_name)
        mTvDate = findViewById(R.id.tv_qr_date)
        mTvHealthHint = findViewById(R.id.tv_qr_health_tips)
        mRlDetail = findViewById(R.id.rl_qr_detail)
        val location = kv.decodeString(resources.getString(R.string.mmkv_def_loc_city))
        if (location != null) {
            mTvArea.text = location
        }
    }

    private fun showQRCode(content: String) {
        launch {
            var green: ByteArray? = null
            var red: ByteArray? = null
            var yellow: ByteArray? = null
            withContext(Dispatchers.IO) {
                val length = DensityUtils.dp2px(this@QRCodeActivity, 220f)
                val bitmap1 = QRCodeUtils.createQRCodeBitmap(content, length, length, ContextCompat.getColor(this@QRCodeActivity, R.color.qr_code_green))
                val bitmap2 = QRCodeUtils.createQRCodeBitmap(content, length, length, ContextCompat.getColor(this@QRCodeActivity, R.color.qr_code_red))
                val bitmap3 = QRCodeUtils.createQRCodeBitmap(content, length, length, ContextCompat.getColor(this@QRCodeActivity, R.color.qr_code_yellow))
                val stream1 = ByteArrayOutputStream()
                bitmap1?.compress(Bitmap.CompressFormat.PNG, 100, stream1)
                green = stream1.toByteArray()
                val stream2 = ByteArrayOutputStream()
                bitmap2?.compress(Bitmap.CompressFormat.PNG, 100, stream2)
                red = stream2.toByteArray()
                val stream3 = ByteArrayOutputStream()
                bitmap3?.compress(Bitmap.CompressFormat.PNG, 100, stream3)
                yellow = stream3.toByteArray()
            }
            Glide.with(this@QRCodeActivity).load(green).centerCrop().into(mQRCode)
            mQRCode.setOnClickListener {
                when (++mCount % 3) {
                    0 -> Glide.with(this@QRCodeActivity).load(green).centerCrop().into(mQRCode)
                    1 -> Glide.with(this@QRCodeActivity).load(red).centerCrop().into(mQRCode)
                    2 -> Glide.with(this@QRCodeActivity).load(yellow).centerCrop().into(mQRCode)
                }
            }
        }
    }

    private fun requestUserData() {
        //发送请求
        Retrofit.Builder()
            .baseUrl(UrlUtils.SERVICE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
            .getUserData("00003", "123")
            .enqueue(object : retrofit2.Callback<User> {
                override fun onFailure(call: Call<User>, t: Throwable) {
                    Log.d("aaa", "onFailure: " + t.message)
                }
                override fun onResponse(call: Call<User>, response: retrofit2.Response<User>) {
                    Log.d("aaa", "onResponse: " + response.body())
                    showQRCode(Gson().toJson(response.body()))
                }
            })
    }

    //计时
    private val mCountDownTimer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
        override fun onFinish() {
        }
        override fun onTick(millisUntilFinished: Long) {
            val date = SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss", Locale.CHINA).format(System.currentTimeMillis())
            mTvDate.text = date
        }
    }

    //定位回调
    private val mLocationListener: BDAbstractLocationListener = object : BDAbstractLocationListener() {
        override fun onReceiveLocation(location: BDLocation?) {
            if (location == null || location.city == null) return
            mTvArea.text = location.city
            kv.encode(resources.getString(R.string.mmkv_def_loc_city), location.province)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mJob.cancel()
        mCountDownTimer.cancel()
    }

}