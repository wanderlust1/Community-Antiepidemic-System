package com.wanderlust.community_antiepidemic_system.activities.qrcode

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.tencent.mmkv.MMKV
import com.wanderlust.community_antiepidemic_system.R
import com.wanderlust.community_antiepidemic_system.WanderlustApp
import com.wanderlust.community_antiepidemic_system.entity.QRCodeMessage
import com.wanderlust.community_antiepidemic_system.entity.QRContent
import com.wanderlust.community_antiepidemic_system.event.QRCodeEvent
import com.wanderlust.community_antiepidemic_system.event.RiskAreaEvent
import com.wanderlust.community_antiepidemic_system.network.ApiService
import com.wanderlust.community_antiepidemic_system.utils.*
import kotlinx.coroutines.*
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.ConnectException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.CoroutineContext

class QRCodeActivity : AppCompatActivity(), CoroutineScope {

    companion object {
        const val TAG = "QRCodeActivity"
    }

    private lateinit var mQRCode: ImageView
    private lateinit var mTvArea: TextView
    private lateinit var mTvCommunity: TextView
    private lateinit var mTvName: TextView
    private lateinit var mTvDate: TextView
    private lateinit var mTvHealthHint: TextView
    private lateinit var mRlDetail: RelativeLayout
    private lateinit var mIvQRMsg: ImageView
    private lateinit var mLlBackground: LinearLayout

    //协程
    private val mJob: Job by lazy { Job() }
    override val coroutineContext: CoroutineContext get() = mJob + Dispatchers.Main

    private val mLocationClient: LocationClient by lazy { LocationClient(this) }
    private var mLocationText = ""

    private val kv: MMKV by lazy { MMKV.defaultMMKV() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrcode)
        initView()
        mCountDownTimer.start()
        requestQRContent()
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
        mIvQRMsg = findViewById(R.id.iv_qr_my_qr_msg)
        mLlBackground = findViewById(R.id.ll_qr_bg)
        val location = kv.decodeString(resources.getString(R.string.mmkv_def_loc_city))
        if (location != null) {
            mTvArea.text = location
        }
        val user = (application as WanderlustApp).gUser ?: return
        mTvCommunity.text = user.communityName
        mTvName.text = "${user.userName[0]}*${user.userName[user.userName.length - 1]}"
    }

    private fun requestQRContent() {
        val id = (application as WanderlustApp).gUser?.userId ?: return
        launch {
            val retrofit = Retrofit.Builder()
                .baseUrl(UrlUtils.SERVICE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
            val response = try {
                withContext(Dispatchers.IO) {
                    val request = QRCodeEvent.QRContentReq(id)
                    retrofit.getQRContent(Gson().toJson(request).toRequestBody()).execute()
                }
            } catch (e: ConnectException) {
                R.string.connection_error.toast(this@QRCodeActivity)
                null
            } catch (e: Exception) {
                R.string.timeout_error.toast(this@QRCodeActivity)
                null
            }
            Log.d(TAG, response?.body().toString())
            if (response?.body() == null) return@launch
            val qrContent = response.body()!!.qrContent
            val riskAreas = withContext(Dispatchers.IO) {
                requestRiskAreaData()
            }
            val qrCodeMessage = QRCodeUtils.getQRCodeColor(qrContent, riskAreas)
            val color = when (qrCodeMessage.color) { //二维码颜色
                QRCodeMessage.GREEN -> {
                    mIvQRMsg.setImageResource(R.drawable.ic_baseline_qr_code_green)
                    mLlBackground.setBackgroundColor(getColor(R.color.qr_code_green))
                    R.color.qr_code_green
                }
                QRCodeMessage.YELLOW -> {
                    mIvQRMsg.setImageResource(R.drawable.ic_baseline_qr_code_yellow)
                    mLlBackground.setBackgroundColor(getColor(R.color.qr_code_yellow))
                    R.color.qr_code_yellow
                }
                QRCodeMessage.RED -> {
                    mIvQRMsg.setImageResource(R.drawable.ic_baseline_qr_code_red)
                    mLlBackground.setBackgroundColor(getColor(R.color.qr_code_red))
                    R.color.qr_code_red
                }
                else -> R.color.qr_code_green
            }
            val qrCodeBytes = withContext(Dispatchers.IO) {
                QRCodeUtils.createQRCode(Gson().toJson(qrContent.content), color, this@QRCodeActivity)
            }
            Glide.with(this@QRCodeActivity).load(qrCodeBytes).centerCrop().into(mQRCode)
            updateHealthHint(qrCodeMessage, qrContent)
        }
    }

    private fun requestRiskAreaData(): RiskAreaEvent.RiskAreaRsp? {
        //首先尝试本地获取，若有，直接返回
        val localResult = MapUtils.readRiskAreaMMKV(kv)
        if (localResult != null) return localResult
        //获得当前时间戳
        val timestamp = System.currentTimeMillis() / 1000
        //封装Header数据
        val client = OkHttpClient.Builder().addInterceptor(object : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
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
        val retrofit = Retrofit.Builder()
            .baseUrl(UrlUtils.AREA_DATA_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(ApiService::class.java)
        val response = try {
            retrofit.getRiskAreaData(RiskAreaEvent.RiskAreaReq(timestamp = timestamp)).execute()
        } catch (e: Exception) {
            null
        }
        val result = response?.body()
        MapUtils.saveRiskAreaMMKV(kv, result)
        return result
    }

    private fun updateHealthHint(qrCodeMessage: QRCodeMessage, qrContent: QRContent) {
        if (qrCodeMessage.color == QRCodeMessage.GREEN) {
            mTvHealthHint.text = "暂未发现健康问题"
        } else {
            mTvHealthHint.text = "查看我的健康问题"
            mTvHealthHint.setTextColor(getColor(if (qrCodeMessage.color == QRCodeMessage.YELLOW)
                R.color.qr_code_yellow else
                R.color.qr_code_red)
            )
            mTvHealthHint.setOnClickListener {
                QRCodeUtils.showMyHealthProblem(this, qrCodeMessage)
            }
        }
        mRlDetail.setOnClickListener {
            val user = (application as WanderlustApp).gUser ?: return@setOnClickListener
            val dialog = BottomSheetDialog(this)
            dialog.setContentView(R.layout.dialog_my_qr_detail)
            dialog.findViewById<TextView>(R.id.tv_qr_detail_name)?.text = user.userName
            dialog.findViewById<TextView>(R.id.tv_qr_detail_cid)?.text = "${user.cid} / ${user.phone}"
            if (qrContent.temperature.isEmpty()) {
                dialog.findViewById<TextView>(R.id.tv_qr_detail_temper)?.text = "暂无体温记录"
                dialog.findViewById<TextView>(R.id.tv_qr_detail_diagnose)?.visibility = View.GONE
                dialog.findViewById<TextView>(R.id.tv_qr_detail_approach)?.visibility = View.GONE
            } else {
                dialog.findViewById<TextView>(R.id.tv_qr_detail_temper)?.text = "最近体温记录：${qrContent.temperature}°C"
                dialog.findViewById<TextView>(R.id.tv_qr_detail_diagnose)?.text = HealthType.toString(qrContent.diagnose)
                dialog.findViewById<TextView>(R.id.tv_qr_detail_approach)?.text = HealthType.toString(qrContent.approach)
            }
            if (qrContent.outside.isEmpty()) {
                dialog.findViewById<TextView>(R.id.tv_qr_detail_outside_title)?.text = "暂无出行记录"
                dialog.findViewById<TextView>(R.id.tv_qr_detail_outside)?.visibility = View.GONE
            } else {
                dialog.findViewById<TextView>(R.id.tv_qr_detail_outside)?.text = buildString {
                    for (i in qrContent.outside.indices) {
                        append(qrContent.outside[i]).append(if (i < qrContent.outside.size - 1) "\n" else "")
                    }
                }
            }
            dialog.delegate.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                ?.setBackgroundColor(getColor(android.R.color.transparent))
            dialog.show()
        }
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
    private val mLocationListener = object : BDAbstractLocationListener() {
        override fun onReceiveLocation(location: BDLocation?) {
            if (location == null || location.district == null) return
            mLocationText = location.district
            mTvArea.text = location.district
            kv.encode(resources.getString(R.string.mmkv_def_loc_city), location.district)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mJob.cancel()
        mCountDownTimer.cancel()
    }

}