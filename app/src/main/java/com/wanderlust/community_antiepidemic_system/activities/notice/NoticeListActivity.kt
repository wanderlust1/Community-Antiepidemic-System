package com.wanderlust.community_antiepidemic_system.activities.notice

import android.app.AlertDialog
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.wanderlust.community_antiepidemic_system.R
import com.wanderlust.community_antiepidemic_system.activities.BaseActivity
import com.wanderlust.community_antiepidemic_system.activities.register.TemperatureActivity
import com.wanderlust.community_antiepidemic_system.event.BusEvent
import com.wanderlust.community_antiepidemic_system.event.NoticeEvent
import com.wanderlust.community_antiepidemic_system.network.ServiceManager
import com.wanderlust.community_antiepidemic_system.utils.LoginType
import com.wanderlust.community_antiepidemic_system.utils.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.RequestBody.Companion.toRequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.net.ConnectException

class NoticeListActivity : BaseActivity() {

    companion object {
        const val TAG = "NoticeListActivity"
    }

    private lateinit var mTvCommunity: TextView
    private lateinit var mRvNoticeList: RecyclerView
    private lateinit var mSrlNotice: SwipeRefreshLayout
    private lateinit var mFabCreateNotice: FloatingActionButton

    private val mAdapter: NoticeAdapter by lazy {
        NoticeAdapter(mType).apply {
            setJumpListener {
                startActivity(Intent(this@NoticeListActivity, TemperatureActivity::class.java))
            }
            setOnItemClickListener {
                if (mType == LoginType.ADMIN || it.hasRead == 1) return@setOnItemClickListener
                AlertDialog.Builder(this@NoticeListActivity)
                    .setTitle(null)
                    .setMessage("将此公告标记为已读？")
                    .setPositiveButton("确定") { _, _ ->
                        requestSetNoticeRead(it.id)
                    }.setNegativeButton("取消") { _, _ -> }
                    .create()
                    .show()
            }
            setLongClickListener {
                if (mType == LoginType.USER) return@setLongClickListener
                AlertDialog.Builder(this@NoticeListActivity)
                    .setTitle(null)
                    .setMessage("确定删除要此公告吗？")
                    .setPositiveButton("确定") { _, _ ->
                        requestDeleteNotice(it.id)
                    }.setNegativeButton("取消") { _, _ -> }
                    .create()
                    .show()
            }
        }
    }

    override fun contentView() = R.layout.activity_notice_list

    override fun findView() {
        EventBus.getDefault().register(this)
        mTvCommunity = findViewById(R.id.tv_notice_community)
        mRvNoticeList = findViewById(R.id.rv_notice)
        mSrlNotice = findViewById(R.id.srl_notice)
        mFabCreateNotice = findViewById(R.id.fab_add_notice)
    }

    override fun initView() {
        if (mType == LoginType.USER) {
            mTvCommunity.text = mUser?.communityName
            mFabCreateNotice.visibility = View.GONE
        } else if (mType == LoginType.ADMIN) {
            mTvCommunity.text = mAdmin?.communityName
            mFabCreateNotice.visibility = View.VISIBLE
            mFabCreateNotice.setOnClickListener {
                startActivity(Intent(this, EditNoticeActivity::class.java))
            }
        }
        findViewById<ImageView>(R.id.iv_notice_back).setOnClickListener {
            finish()
        }
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        mRvNoticeList.layoutManager = layoutManager
        mRvNoticeList.adapter = mAdapter
        requestNoticeList()
        mSrlNotice.setOnRefreshListener {
            requestNoticeList()
        }
    }

    private fun requestNoticeList() {
        if (mType == 0 || (mUser == null && mAdmin == null)) return
        launch {
            val response = try {
                withContext(Dispatchers.IO) {
                    val request = if (mType == LoginType.USER) {
                        NoticeEvent.GetNoticesListReq(mUser!!.userId, mUser!!.communityId, mType)
                    } else {
                        NoticeEvent.GetNoticesListReq("", mAdmin!!.communityId, mType)
                    }
                    ServiceManager.client.getNoticesList(Gson().toJson(request).toRequestBody()).execute()
                }
            } catch (e: ConnectException) {
                R.string.connection_error.toast(this@NoticeListActivity)
                null
            } catch (e: Exception) {
                e.printStackTrace()
                R.string.timeout_error.toast(this@NoticeListActivity)
                null
            }
            mSrlNotice.isRefreshing = false
            Log.d(TAG, response?.body().toString())
            if (response?.body() == null) return@launch
            val result = response.body()!!
            if (result.result.isEmpty()) {
                mAdapter.update("社区暂未发布公告")
            } else {
                mAdapter.update(result.result)
            }
        }
    }

    //标为已读
    private fun requestSetNoticeRead(noticeId: String) {
        if (mUser == null) return
        launch {
            val response = try {
                withContext(Dispatchers.IO) {
                    val request = NoticeEvent.SetNoticeReadReq(mUser!!.userId, noticeId, mUser!!.communityId)
                    ServiceManager.client.setNoticeRead(Gson().toJson(request).toRequestBody()).execute()
                }
            } catch (e: ConnectException) {
                R.string.connection_error.toast(this@NoticeListActivity)
                null
            } catch (e: Exception) {
                e.printStackTrace()
                R.string.timeout_error.toast(this@NoticeListActivity)
                null
            }
            Log.d(TAG, response?.body().toString())
            if (response?.body() == null) return@launch
            val result = response.body()!!
            result.msg.toast(this@NoticeListActivity)
            if (result.code == NoticeEvent.SUCC) {
                requestNoticeList()
                EventBus.getDefault().post(BusEvent.NoReadCountChange(result.noReadCount))
            }
        }
    }

    //标为已读
    private fun requestDeleteNotice(noticeId: String) {
        launch {
            val response = try {
                withContext(Dispatchers.IO) {
                    val request = NoticeEvent.DeleteNoticeReq(noticeId)
                    ServiceManager.client.deleteNotice(Gson().toJson(request).toRequestBody()).execute()
                }
            } catch (e: ConnectException) {
                R.string.connection_error.toast(this@NoticeListActivity)
                null
            } catch (e: Exception) {
                e.printStackTrace()
                R.string.timeout_error.toast(this@NoticeListActivity)
                null
            }
            Log.d(TAG, response?.body().toString())
            if (response?.body() == null) return@launch
            val result = response.body()!!
            result.msg.toast(this@NoticeListActivity)
            if (result.code == NoticeEvent.SUCC) {
                requestNoticeList()
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNoticeListUpdate(event: BusEvent.NoticeListUpdate) {
        requestNoticeList()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}