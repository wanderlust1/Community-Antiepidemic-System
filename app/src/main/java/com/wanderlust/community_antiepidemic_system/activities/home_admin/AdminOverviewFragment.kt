package com.wanderlust.community_antiepidemic_system.activities.home_admin

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.wanderlust.community_antiepidemic_system.R
import com.wanderlust.community_antiepidemic_system.WanderlustApp
import com.wanderlust.community_antiepidemic_system.activities.community.CreateCommunityActivity
import com.wanderlust.community_antiepidemic_system.activities.community.SearchCommunityActivity
import com.wanderlust.community_antiepidemic_system.activities.notice.NoticeListActivity
import com.wanderlust.community_antiepidemic_system.event.BusEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.coroutines.CoroutineContext

class AdminOverviewFragment : Fragment(), CoroutineScope {

    private lateinit var mTvCommunity: TextView
    private lateinit var mTvNotice: TextView
    private lateinit var mPieChart: PieChart

    private val mJob: Job by lazy { Job() }
    override val coroutineContext: CoroutineContext get() = mJob + Dispatchers.Main

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_admin_overview, container, false).apply {
            mPieChart = findViewById(R.id.pie_chart)
            mTvCommunity = findViewById(R.id.tv_admin_my_community)
            mTvNotice = findViewById(R.id.tv_admin_notification)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        EventBus.getDefault().register(this)
        setMyCommunity()

        mTvNotice.setOnClickListener {
            startActivity(Intent(activity, NoticeListActivity::class.java))
        }


        val entries: MutableList<PieEntry> = ArrayList()
        entries.add(PieEntry(30.4f, "健康"))
        entries.add(PieEntry(22.5f, "确诊病例"))
        entries.add(PieEntry(25.1f, "疑似病例"))
        entries.add(PieEntry(22.0f, "密切接触者"))
        val colors: MutableList<Int> = ArrayList()
        colors.add(activity?.getColor(R.color.qr_code_green) ?: Color.GREEN)
        colors.add(activity?.getColor(R.color.qr_code_red) ?: Color.RED)
        colors.add(activity?.getColor(R.color.qr_code_yellow) ?: Color.BLACK)
        colors.add(Color.GRAY)
        val set = PieDataSet(entries, "")
        set.colors = colors
        set.sliceSpace = 5f   // 每块之间的距离
        val data = PieData(set)
        mPieChart.data = data
        mPieChart.invalidate() // 刷新

    }

    private fun setMyCommunity() {
        val admin = (activity?.application as WanderlustApp).gAdmin ?: return
        mTvCommunity.setOnClickListener {
            AlertDialog.Builder(activity).setItems(arrayOf("搜索社区并绑定", "创建新社区并绑定")) { _, which ->
                activity?.startActivity(when (which) {
                    0 -> Intent(activity, SearchCommunityActivity::class.java)
                    1 -> Intent(activity, CreateCommunityActivity::class.java)
                    else -> return@setItems
                })
            }.setTitle(null).setIcon(null).show()
        }
        mTvCommunity.text = if (admin.adminId.isNotEmpty()) admin.communityName else "未绑定社区"
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCommunityChange(event: BusEvent.OnAdminCommunityChange) {
        setMyCommunity()
    }

    override fun onDestroy() {
        super.onDestroy()
        mJob.cancel()
        EventBus.getDefault().unregister(this)
    }

    companion object {
        const val TAG = "AdminOverviewFragment"
        @JvmStatic fun newInstance() = AdminOverviewFragment()
    }

}