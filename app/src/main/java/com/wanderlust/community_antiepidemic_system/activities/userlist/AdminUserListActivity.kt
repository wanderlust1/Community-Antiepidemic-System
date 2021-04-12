package com.wanderlust.community_antiepidemic_system.activities.userlist

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Rect
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.gson.Gson
import com.wanderlust.community_antiepidemic_system.R
import com.wanderlust.community_antiepidemic_system.activities.BaseActivity
import com.wanderlust.community_antiepidemic_system.activities.register.AdminRegRecordActivity
import com.wanderlust.community_antiepidemic_system.event.BusEvent
import com.wanderlust.community_antiepidemic_system.event.UserEvent
import com.wanderlust.community_antiepidemic_system.network.ServiceManager
import com.wanderlust.community_antiepidemic_system.utils.CommonUtils
import com.wanderlust.community_antiepidemic_system.utils.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.RequestBody.Companion.toRequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.net.ConnectException

class AdminUserListActivity : BaseActivity() {

    companion object {
        const val TAG = "AdminUserListActivity"
        val OPERATES = arrayOf("查看健康记录", "查看外出记录", "移除用户")
    }

    private lateinit var mRvUsers: RecyclerView
    private lateinit var mRefreshLayout: SwipeRefreshLayout

    private val mAdapter by lazy {
        val adapter = UserListAdapter()
        adapter.setItemSelectListener {
            AlertDialog.Builder(this).setItems(OPERATES) { _, which ->
                operateUser(which, it.userId, it.userName)
            }.setTitle(null).create().show()
        }
        adapter
    }

    override fun contentView() = R.layout.activity_admin_user_list

    override fun findView() {
        mRvUsers = findViewById(R.id.rv_user_list)
        mRefreshLayout = findViewById(R.id.srl_user_list)
    }

    override fun initView() {
        EventBus.getDefault().register(this)
        findViewById<ImageView>(R.id.iv_user_list_back).setOnClickListener {
            finish()
        }
        mRvUsers.adapter = mAdapter
        mRvUsers.layoutManager = LinearLayoutManager(this)
        mRvUsers.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                if (parent.getChildAdapterPosition(view) == 0) {
                    outRect.top = CommonUtils.dp2px(this@AdminUserListActivity, 10f)
                }
            }
        })
        mRefreshLayout.setOnRefreshListener {
            requestData()
        }
        requestData()
    }

    private fun operateUser(which: Int, userId: String, username: String) {
        when (which) {
            0 -> {
                startActivity(Intent(this, AdminRegRecordActivity::class.java).apply {
                    putExtra(AdminRegRecordActivity.SHOW_TYPE, AdminRegRecordActivity.TYPE_TEMPERATURE)
                    putExtra(AdminRegRecordActivity.USER_ID, userId)
                    putExtra(AdminRegRecordActivity.USER_NAME, username)
                })
            }
            1 -> {
                startActivity(Intent(this, AdminRegRecordActivity::class.java).apply {
                    putExtra(AdminRegRecordActivity.SHOW_TYPE, AdminRegRecordActivity.TYPE_OUTSIDE)
                    putExtra(AdminRegRecordActivity.USER_ID, userId)
                    putExtra(AdminRegRecordActivity.USER_NAME, username)
                })
            }
            2 -> requestKickUser(userId)
        }
    }

    private fun requestData() {
        val communityId = mAdmin?.communityId ?: return
        launch {
            val response = try {
                withContext(Dispatchers.IO) {
                    val request = UserEvent.GetCommunityUsersReq(communityId)
                    ServiceManager.client.getCommunityUsers(Gson().toJson(request).toRequestBody()).execute()
                }
            } catch (e: ConnectException) {
                R.string.connection_error.toast(this@AdminUserListActivity)
                null
            } catch (e: Exception) {
                e.printStackTrace()
                R.string.timeout_error.toast(this@AdminUserListActivity)
                null
            }
            Log.d(TAG, response?.body().toString())
            mRefreshLayout.isRefreshing = false
            if (response?.body() == null) return@launch
            val result = response.body()!!
            mAdapter.update(result.result)
        }
    }

    private fun requestKickUser(userId: String) {
        val communityId = mAdmin?.communityId ?: return
        launch {
            val response = try {
                withContext(Dispatchers.IO) {
                    val request = UserEvent.KickUserReq(userId, communityId)
                    ServiceManager.client.kickUser(Gson().toJson(request).toRequestBody()).execute()
                }
            } catch (e: ConnectException) {
                R.string.connection_error.toast(this@AdminUserListActivity)
                null
            } catch (e: Exception) {
                e.printStackTrace()
                R.string.timeout_error.toast(this@AdminUserListActivity)
                null
            }
            Log.d(TAG, response?.body().toString())
            mRefreshLayout.isRefreshing = false
            if (response?.body() == null) return@launch
            val result = response.body()!!
            result.msg.toast(this@AdminUserListActivity)
            if (result.code == UserEvent.SUCC) {
                requestData()
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCommunityChange(event: BusEvent.OnAdminCommunityChange) {
        requestData()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

}