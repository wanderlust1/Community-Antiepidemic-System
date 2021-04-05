package com.wanderlust.community_antiepidemic_system.activities.home

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.google.gson.Gson
import com.tencent.mmkv.MMKV
import com.wanderlust.community_antiepidemic_system.R
import com.wanderlust.community_antiepidemic_system.WanderlustApp
import com.wanderlust.community_antiepidemic_system.activities.BaseActivity
import com.wanderlust.community_antiepidemic_system.activities.map.MapActivity
import com.wanderlust.community_antiepidemic_system.activities.qrcode.QRCodeActivity
import com.wanderlust.community_antiepidemic_system.activities.register.OutsideActivity
import com.wanderlust.community_antiepidemic_system.activities.register.TemperatureActivity
import com.wanderlust.community_antiepidemic_system.activities.community.SearchCommunityActivity
import com.wanderlust.community_antiepidemic_system.activities.notice.NoticeListActivity
import com.wanderlust.community_antiepidemic_system.event.BusEvent
import com.wanderlust.community_antiepidemic_system.event.DiseaseDataEvent
import com.wanderlust.community_antiepidemic_system.event.NoticeEvent
import com.wanderlust.community_antiepidemic_system.network.Service
import com.wanderlust.community_antiepidemic_system.utils.DialogUtils
import com.wanderlust.community_antiepidemic_system.utils.MapUtils
import com.wanderlust.community_antiepidemic_system.utils.toast
import com.wanderlust.community_antiepidemic_system.widget.DiseaseStatsView
import kotlinx.coroutines.*
import okhttp3.RequestBody.Companion.toRequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.lang.Integer.max
import java.net.ConnectException

class UserHomeActivity : BaseActivity() {

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
    private lateinit var mTvMyCommunityTips: TextView
    private lateinit var mRlMyCommunity: RelativeLayout
    private lateinit var mTvQRCode: TextView
    private lateinit var mTvTemperature: TextView
    private lateinit var mTvOutSide: TextView
    private lateinit var mDrawer: DrawerLayout
    private lateinit var mDrawerName: TextView
    private lateinit var mDrawerId: TextView
    private lateinit var mDrawerCommunity: TextView
    private lateinit var mDrawerCid: TextView
    private lateinit var mDrawerPhone: TextView
    private lateinit var mRefreshLayout: SwipeRefreshLayout
    private lateinit var mTvNoReadCount: TextView
    private lateinit var mFlNoReadCount: FrameLayout

    private val mLocationClient: LocationClient by lazy { LocationClient(this) }

    private val kv: MMKV by lazy { MMKV.defaultMMKV() }

    private var mResult: DiseaseDataEvent.AntiepidemicRsp? = null

    override fun contentView() = R.layout.activity_user_home

    override fun findView() {
        mIvPerson = findViewById(R.id.iv_home_self)
        mIvNotify = findViewById(R.id.iv_home_notification)
        mTvDate = findViewById(R.id.tv_home_stats_time)
        mRlMap = findViewById(R.id.rl_home_danger_map)
        mDsvCountry = findViewById(R.id.dsv_home_stats_country)
        mDsvProvince = findViewById(R.id.dsv_home_stats_province)
        mTvMyCommunity = findViewById(R.id.tv_home_my_community)
        mTvMyCommunityTips = findViewById(R.id.tv_home_my_community_tips)
        mRlMyCommunity = findViewById(R.id.rl_home_my_community)
        mTvQRCode = findViewById(R.id.tv_home_health_qrcode)
        mTvTemperature = findViewById(R.id.tv_home_register_temperature)
        mTvOutSide = findViewById(R.id.tv_home_register_out_side)
        mDrawer = findViewById(R.id.dl_home_drawer)
        mDrawerName = findViewById(R.id.tv_home_drawer_name)
        mDrawerCid = findViewById(R.id.tv_home_drawer_cid)
        mDrawerCommunity = findViewById(R.id.tv_home_drawer_community)
        mDrawerId = findViewById(R.id.tv_home_drawer_id)
        mDrawerPhone = findViewById(R.id.tv_home_drawer_phone)
        mRefreshLayout = findViewById(R.id.srl_home)
        mTvNoReadCount = findViewById(R.id.tv_home_red_point)
        mFlNoReadCount = findViewById(R.id.fl_home_no_read)
    }

    @SuppressLint("SetTextI18n")
    override fun initView() {
        EventBus.getDefault().register(this)
        val user = (application as WanderlustApp).gUser
        mIvPerson.setOnClickListener {
            mDrawer.openDrawer(GravityCompat.END)
        }
        mIvNotify.setOnClickListener {
            intentJudgement("社区公告", NoticeListActivity::class.java)
        }
        mRlMap.setOnClickListener {
            startActivity(Intent(this, MapActivity::class.java))
        }
        mTvQRCode.setOnClickListener {
            intentJudgement("健康二维码", QRCodeActivity::class.java)
        }
        mTvTemperature.setOnClickListener {
            intentJudgement("健康登记", TemperatureActivity::class.java)
        }
        mTvOutSide.setOnClickListener {
            intentJudgement("外出登记", OutsideActivity::class.java)
        }
        mRefreshLayout.setOnRefreshListener {
            requestDiseaseData()
            requestNoReadCount()
        }
        mDrawerName.text = user?.userName
        mDrawerId.text = "ID ${user?.userId}"
        mDrawerCid.text = user?.cid
        mDrawerPhone.text = user?.phone
        setMyCommunity()
        requestDiseaseData()
        requestNoReadCount()
    }

    private fun intentJudgement(name: String, clazz: Class<*>) {
        if ((application as WanderlustApp).gUser?.communityId.isNullOrEmpty()) {
            DialogUtils(this, "使用${name}功能之前，你必须先加入一个社区。").show()
        } else {
            startActivity(Intent(this, clazz))
        }
    }

    private fun setMyCommunity() {
        val user = (application as WanderlustApp).gUser
        val community = if (user != null && user.communityName.isNotEmpty()) user.communityName else "尚未加入社区"
        mTvMyCommunity.text = community
        mDrawerCommunity.text = community
        mTvMyCommunityTips.text = if (user != null && user.communityName.isNotEmpty()) "修改我的社区" else "搜索并加入社区"
        mRlMyCommunity.setOnClickListener {
            startActivity(Intent(this, SearchCommunityActivity::class.java))
        }
    }

    private fun setNoReadCount(noReadCount: Int) {
        if (noReadCount > 0) {
            mFlNoReadCount.visibility = View.VISIBLE
            mTvNoReadCount.text = "${noReadCount.coerceAtMost(99)}"
        } else {
            mFlNoReadCount.visibility = View.GONE
        }
    }

    @SuppressLint("SetTextI18n")
    private fun requestDiseaseData() {
        launch {
            val response = try {
                withContext(Dispatchers.IO) {
                    Service.staticData.getAntiepidemicData().execute()
                }
            } catch (e: ConnectException) {
                R.string.connection_error.toast(this@UserHomeActivity)
                null
            } catch (e: Exception) {
                e.printStackTrace()
                R.string.other_error.toast(this@UserHomeActivity)
                null
            }
            mRefreshLayout.isRefreshing = false
            Log.d(TAG, response?.body().toString())
            if (response?.body() == null) return@launch
            mResult = response.body()!!
            mTvDate.text = "截至 ${mResult?.country?.time}"
            mDsvCountry.setData(mResult?.country)
            mDsvProvince.setData(mResult?.provinceArray?.find {
                it.childStatistic.contains(
                    kv.decodeString(resources.getString(R.string.mmkv_def_loc_province), "error"))
            })
            MapUtils.startOneLocation(mLocationClient, mLocationListener)
        }
    }

    private fun requestNoReadCount() {
        if (mUser == null) return
        launch {
            val response = try {
                withContext(Dispatchers.IO) {
                    val request = NoticeEvent.GetNoReadCountReq(mUser!!.userId, mUser!!.communityId)
                    Service.request.getNoReadCount(Gson().toJson(request).toRequestBody()).execute()
                }
            } catch (e: ConnectException) {
                R.string.connection_error.toast(this@UserHomeActivity)
                null
            } catch (e: Exception) {
                e.printStackTrace()
                R.string.other_error.toast(this@UserHomeActivity)
                null
            }
            mRefreshLayout.isRefreshing = false
            Log.d(TAG, response?.body().toString())
            if (response?.body() == null) return@launch
            val result = response.body()!!
            setNoReadCount(result.noReadCount)
        }
    }

    //定位回调
    private val mLocationListener: BDAbstractLocationListener = object : BDAbstractLocationListener() {
        override fun onReceiveLocation(location: BDLocation?) {
            if (location == null || location.province == null) return
            mDsvProvince.setData(mResult?.provinceArray?.find {
                it.childStatistic.contains(location.province)
            })
            kv.encode(resources.getString(R.string.mmkv_def_loc_province), location.province)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCommunityChange(event: BusEvent.OnCommunityChange) {
        setMyCommunity()
        requestNoReadCount()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNoReadCountChange(event: BusEvent.NoReadCountChange) {
        setNoReadCount(event.noReadCount)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

}