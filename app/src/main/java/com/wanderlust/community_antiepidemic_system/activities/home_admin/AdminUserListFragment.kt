package com.wanderlust.community_antiepidemic_system.activities.home_admin

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.gson.Gson
import com.wanderlust.community_antiepidemic_system.R
import com.wanderlust.community_antiepidemic_system.WanderlustApp
import com.wanderlust.community_antiepidemic_system.activities.register.AdminRegRecordActivity
import com.wanderlust.community_antiepidemic_system.event.UserEvent
import com.wanderlust.community_antiepidemic_system.network.Service
import com.wanderlust.community_antiepidemic_system.utils.CommonUtils
import com.wanderlust.community_antiepidemic_system.utils.toast
import kotlinx.coroutines.*
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.ConnectException
import kotlin.coroutines.CoroutineContext

class AdminUserListFragment : Fragment(), CoroutineScope {

    private val mJob: Job by lazy { Job() }
    override val coroutineContext: CoroutineContext get() = mJob + Dispatchers.Main
    
    private lateinit var mRvUsers: RecyclerView
    private lateinit var mRefreshLayout: SwipeRefreshLayout

    private val mAdapter by lazy {
        val adapter = UserListAdapter()
        adapter.setItemSelectListener {
            AlertDialog.Builder(activity).setItems(OPERATES) { _, which ->
                operateUser(which, it.userId, it.userName)
            }.setTitle(null).create().show()
        }
        adapter
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_admin_user_list, container, false).apply {
            mRvUsers = findViewById(R.id.rv_user_list)
            mRefreshLayout = findViewById(R.id.srl_user_list)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mRvUsers.adapter = mAdapter
        mRvUsers.layoutManager = LinearLayoutManager(activity)
        mRvUsers.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                if (parent.getChildAdapterPosition(view) == 0 && activity != null) {
                    outRect.top = CommonUtils.dp2px(activity!!, 10f)
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
                activity?.startActivity(Intent(activity, AdminRegRecordActivity::class.java).apply {
                    putExtra(AdminRegRecordActivity.SHOW_TYPE, AdminRegRecordActivity.TYPE_TEMPERATURE)
                    putExtra(AdminRegRecordActivity.USER_ID, userId)
                    putExtra(AdminRegRecordActivity.USER_NAME, username)
                })
            }
            1 -> {
                activity?.startActivity(Intent(activity, AdminRegRecordActivity::class.java).apply {
                    putExtra(AdminRegRecordActivity.SHOW_TYPE, AdminRegRecordActivity.TYPE_OUTSIDE)
                    putExtra(AdminRegRecordActivity.USER_ID, userId)
                    putExtra(AdminRegRecordActivity.USER_NAME, username)
                })
            }
            2 -> requestKickUser(userId)
        }
    }

    private fun requestData() {
        val admin = (activity?.application as WanderlustApp?)?.gAdmin ?: return
        launch {
            val response = try {
                withContext(Dispatchers.IO) {
                    val request = UserEvent.GetCommunityUsersReq(admin.communityId)
                    Service.request.getCommunityUsers(Gson().toJson(request).toRequestBody()).execute()
                }
            } catch (e: ConnectException) {
                R.string.connection_error.toast(activity)
                null
            } catch (e: Exception) {
                e.printStackTrace()
                R.string.timeout_error.toast(activity)
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
        val admin = (activity?.application as WanderlustApp?)?.gAdmin ?: return
        launch {
            val response = try {
                withContext(Dispatchers.IO) {
                    val request = UserEvent.KickUserReq(userId, admin.communityId)
                    Service.request.kickUser(Gson().toJson(request).toRequestBody()).execute()
                }
            } catch (e: ConnectException) {
                R.string.connection_error.toast(activity)
                null
            } catch (e: Exception) {
                e.printStackTrace()
                R.string.timeout_error.toast(activity)
                null
            }
            Log.d(TAG, response?.body().toString())
            mRefreshLayout.isRefreshing = false
            if (response?.body() == null) return@launch
            val result = response.body()!!
            result.msg.toast(activity)
            if (result.code == UserEvent.SUCC) {
                requestData()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mJob.cancel()
    }

    companion object {
        const val TAG = "AdminUserListFragment"
        @JvmStatic fun newInstance() = AdminUserListFragment()
        val OPERATES = arrayOf("查看健康记录", "查看外出记录", "移除用户")
    }

}