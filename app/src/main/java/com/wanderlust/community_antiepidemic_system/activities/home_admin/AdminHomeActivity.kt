package com.wanderlust.community_antiepidemic_system.activities.home_admin

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.gson.Gson
import com.wanderlust.community_antiepidemic_system.R
import com.wanderlust.community_antiepidemic_system.activities.BaseActivity
import com.wanderlust.community_antiepidemic_system.activities.community.CreateCommunityActivity
import com.wanderlust.community_antiepidemic_system.activities.community.SearchCommunityActivity
import com.wanderlust.community_antiepidemic_system.activities.notice.NoticeListActivity
import com.wanderlust.community_antiepidemic_system.activities.userlist.AdminUserListActivity
import com.wanderlust.community_antiepidemic_system.entity.Community
import com.wanderlust.community_antiepidemic_system.entity.CommunityStatistics
import com.wanderlust.community_antiepidemic_system.event.BusEvent
import com.wanderlust.community_antiepidemic_system.event.CommunityEvent
import com.wanderlust.community_antiepidemic_system.event.RiskAreaEvent
import com.wanderlust.community_antiepidemic_system.network.ServiceManager
import com.wanderlust.community_antiepidemic_system.utils.DialogUtils
import com.wanderlust.community_antiepidemic_system.utils.toJsonRequest
import com.wanderlust.community_antiepidemic_system.utils.toast
import com.wanderlust.community_antiepidemic_system.widget.AdminStatisticView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.RequestBody.Companion.toRequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.net.ConnectException

class AdminHomeActivity : BaseActivity() {

    companion object {
        const val TAG = "AdminHomeActivity"
    }

    private lateinit var mRefreshLayout: SwipeRefreshLayout
    private lateinit var mTvCommunity: TextView
    private lateinit var mTvNoCommunity: TextView
    private lateinit var mTvChange: TextView
    private lateinit var mTvAddress: TextView
    private lateinit var mTvPhone: TextView
    private lateinit var mRlMyCommunity: RelativeLayout
    private lateinit var mTvNotice: TextView
    private lateinit var mTvUserList: TextView
    private lateinit var mStatisticView: AdminStatisticView

    private val mChangeDialog by lazy {
        AlertDialog.Builder(this).setItems(arrayOf("搜索社区并绑定", "创建新社区并绑定")) { _, which ->
            startActivity(when (which) {
                0 -> Intent(this, SearchCommunityActivity::class.java)
                1 -> Intent(this, CreateCommunityActivity::class.java)
                else -> return@setItems
            })
        }.setTitle(null).setIcon(null).create()
    }

    override fun contentView() = R.layout.activity_admin_home

    override fun findView() {
        mTvCommunity = findViewById(R.id.tv_admin_my_community)
        mTvNotice = findViewById(R.id.tv_admin_notification)
        mTvUserList = findViewById(R.id.tv_admin_user_list)
        mTvNoCommunity = findViewById(R.id.tv_admin_no_community)
        mTvChange = findViewById(R.id.tv_admin_my_community_change)
        mTvAddress = findViewById(R.id.tv_admin_my_community_address)
        mTvPhone = findViewById(R.id.tv_admin_my_community_phone)
        mRlMyCommunity = findViewById(R.id.rl_admin_my_community)
        mRefreshLayout = findViewById(R.id.srl_admin_overview)
        mStatisticView = findViewById(R.id.asv_admin_stat)
    }

    override fun initView() {
        EventBus.getDefault().register(this)
        mTvNotice.setOnClickListener {
            intentJudgement("公告和提醒", NoticeListActivity::class.java)
        }
        mTvUserList.setOnClickListener {
            intentJudgement("用户列表", AdminUserListActivity::class.java)
        }
        mRefreshLayout.setOnRefreshListener {
            requestCommunityMessage()
        }
        requestCommunityMessage()
    }

    private fun requestCommunityMessage() {
        if (mAdmin == null) return
        launch {
            val response = ServiceManager.request {
                val request = CommunityEvent.CommunityMessageReq(mAdmin!!.adminId)
                it.getCommunityMessage(request.toJsonRequest())
            }
            mRefreshLayout.isRefreshing = false
            Log.d(TAG, response.toString())
            if (response != null) {
                setMyCommunity(if (response.code == CommunityEvent.SUCC) response.community else null)
                val riskAreas = ServiceManager.requestRiskAreaData()
                initChartData(response.statistics, riskAreas)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setMyCommunity(community: Community?) {
        if (mAdmin == null) return
        if (community != null) {
            mTvNoCommunity.visibility = View.GONE
            mRlMyCommunity.visibility = View.VISIBLE
            mTvCommunity.text = community.name
            mTvAddress.text = community.location
            mTvPhone.text = "联系方式：${community.phone}"
            mTvChange.setOnClickListener {
                mChangeDialog.show()
            }
        } else {
            mTvNoCommunity.visibility = View.VISIBLE
            mRlMyCommunity.visibility = View.GONE
            mTvNoCommunity.setOnClickListener {
                mChangeDialog.show()
            }
        }
    }

    private fun initChartData(statistics: CommunityStatistics, riskAreas: RiskAreaEvent.RiskAreaRsp?) {
        if (statistics.total <= 0) {
            mStatisticView.visibility = View.GONE
            return
        }
        mStatisticView.visibility = View.VISIBLE
        val outsideUserSet = HashSet<String>()
        riskAreas?.data?.highList?.forEach {
            for (area in statistics.outsideRegs) {
                if (area.city.contains(it.county) && !outsideUserSet.contains(area.userId)) {
                    outsideUserSet.add(area.userId)
                    statistics.highRisk++
                }
            }
        }
        outsideUserSet.clear()
        riskAreas?.data?.midList?.forEach {
            for (area in statistics.outsideRegs) {
                if (area.city.contains(it.county) && !outsideUserSet.contains(area.userId)) {
                    outsideUserSet.add(area.userId)
                    statistics.midRisk++
                }
            }
        }
        mStatisticView.setData(statistics)
    }

    private fun intentJudgement(name: String, clazz: Class<*>) {
        if (mAdmin?.communityId.isNullOrEmpty()) {
            DialogUtils(this, "使用${name}功能之前，你必须先绑定一个社区。").show()
        } else {
            startActivity(Intent(this, clazz))
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCommunityChange(event: BusEvent.OnAdminCommunityChange) {
        requestCommunityMessage()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

}