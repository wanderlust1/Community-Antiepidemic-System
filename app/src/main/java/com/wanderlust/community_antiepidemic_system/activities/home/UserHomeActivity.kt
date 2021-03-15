package com.wanderlust.community_antiepidemic_system.activities.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.tencent.mmkv.MMKV
import com.wanderlust.community_antiepidemic_system.R
import com.wanderlust.community_antiepidemic_system.activities.map.MapActivity
import com.wanderlust.community_antiepidemic_system.activities.qrcode.QRCodeActivity
import com.wanderlust.community_antiepidemic_system.event.AntiepidemicRsp
import com.wanderlust.community_antiepidemic_system.network.ApiService
import com.wanderlust.community_antiepidemic_system.utils.MapUtils
import com.wanderlust.community_antiepidemic_system.utils.UrlUtils
import com.wanderlust.community_antiepidemic_system.utils.toast
import com.wanderlust.community_antiepidemic_system.widget.DiseaseStatsView
import kotlinx.coroutines.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.ConnectException
import kotlin.coroutines.CoroutineContext

class UserHomeActivity : AppCompatActivity(), CoroutineScope {

    companion object {
        const val TAG = "UserHomeActivity"
    }

    private lateinit var mIvPerson: ImageView
    private lateinit var mIvNotify: ImageView
    private lateinit var mTvDate: TextView
    private lateinit var mRlMap: RelativeLayout
    private lateinit var mDsvCountry: DiseaseStatsView
    private lateinit var mDsvProvince: DiseaseStatsView
    private lateinit var mTvMyCommunity: TextView
    private lateinit var mTvQRCode: TextView
    private lateinit var mTvTemperature: TextView
    private lateinit var mTvOutSide: TextView

    private val mJob: Job by lazy { Job() }
    override val coroutineContext: CoroutineContext get() = mJob + Dispatchers.Main

    private val mLocationClient: LocationClient by lazy { LocationClient(this) }

    private val kv: MMKV by lazy { MMKV.defaultMMKV() }

    private var mResult: AntiepidemicRsp? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_home)
        initView()
        setView()
        requestDiseaseData()
    }

    private fun initView() {
        mIvPerson = findViewById(R.id.iv_home_self)
        mIvNotify = findViewById(R.id.iv_home_notification)
        mTvDate = findViewById(R.id.tv_home_stats_time)
        mRlMap = findViewById(R.id.rl_home_danger_map)
        mDsvCountry = findViewById(R.id.dsv_home_stats_country)
        mDsvProvince = findViewById(R.id.dsv_home_stats_province)
        mTvMyCommunity = findViewById(R.id.tv_home_my_community)
        mTvQRCode = findViewById(R.id.tv_home_health_qrcode)
        mTvTemperature = findViewById(R.id.tv_home_register_temperature)
        mTvOutSide = findViewById(R.id.tv_home_register_out_side)
    }

    private fun setView() {
        mIvPerson.setOnClickListener {

        }
        mIvNotify.setOnClickListener {

        }
        mRlMap.setOnClickListener {
            startActivity(Intent(this, MapActivity::class.java))
        }
        mTvQRCode.setOnClickListener {
            startActivity(Intent(this, QRCodeActivity::class.java))
        }
    }

    private fun requestDiseaseData() {
        launch {
            val retrofit = Retrofit.Builder()
                .baseUrl(UrlUtils.STATIC_DATA_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
            val response = try {
                withContext(Dispatchers.IO) {
                    retrofit.getAntiepidemicData().execute()
                }
            } catch (e: ConnectException) {
                R.string.connection_error.toast(this@UserHomeActivity)
                null
            } catch (e: Exception) {
                e.printStackTrace()
                R.string.other_error.toast(this@UserHomeActivity)
                null
            }
            Log.d(TAG, response?.body().toString())
            if (response?.body() == null) return@launch
            mResult = response.body()!!
            mTvDate.text = "截至 ${mResult?.country?.time}"
            mDsvCountry.setData(mResult?.country)
            mDsvProvince.setData(mResult?.provinceArray?.find {
                it.childStatistic.contains(
                    kv.decodeString(resources.getString(R.string.mmkv_def_loc), "error"))
            })
            MapUtils.startOneLocation(mLocationClient, mLocationListener)
        }
    }

    //定位回调
    private val mLocationListener: BDAbstractLocationListener = object : BDAbstractLocationListener() {
        override fun onReceiveLocation(location: BDLocation?) {
            if (location == null || location.province == null) return
            mDsvProvince.setData(mResult?.provinceArray?.find {
                it.childStatistic.contains(location.province)
            })
            kv.encode(resources.getString(R.string.mmkv_def_loc), location.province)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mJob.cancel()
    }

}