package com.wanderlust.community_antiepidemic_system.activities.register

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.gson.Gson
import com.wanderlust.community_antiepidemic_system.R
import com.wanderlust.community_antiepidemic_system.WanderlustApp
import com.wanderlust.community_antiepidemic_system.activities.BaseFragment
import com.wanderlust.community_antiepidemic_system.event.RegEvent
import com.wanderlust.community_antiepidemic_system.network.Service
import com.wanderlust.community_antiepidemic_system.utils.CommonUtils
import com.wanderlust.community_antiepidemic_system.utils.toast
import kotlinx.coroutines.*
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.ConnectException

class TemperRecordFragment : BaseFragment() {

    private var mUserId = ""

    private lateinit var mRefreshLayout: SwipeRefreshLayout
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mTvNoData: TextView

    private val mAdapter = TemperRecordAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mUserId = arguments?.getString(USER_ID) ?: ""
    }

    override fun contentView() = R.layout.fragment_temper_record

    override fun findView(parent: View) {
        mRefreshLayout = parent.findViewById(R.id.srl_temper_record)
        mRecyclerView = parent.findViewById(R.id.rv_temper_record)
        mTvNoData = parent.findViewById(R.id.tv_temper_record_no_data)
    }

    override fun initView() {
        mRecyclerView.adapter = mAdapter
        mRecyclerView.layoutManager = LinearLayoutManager(activity)
        mRecyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
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

    private fun requestData() {
        val id = if (mUserId.isNotEmpty()) mUserId else {
            (activity?.application as WanderlustApp).gUser?.userId ?: return
        }
        launch {
            val response = try {
                withContext(Dispatchers.IO) {
                    val request = RegEvent.GetTemperRecordReq(id)
                    Service.request.getTemperRecord(Gson().toJson(request).toRequestBody()).execute()
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
            if (result.list.isEmpty()) {
                mTvNoData.visibility = View.VISIBLE
                mRecyclerView.visibility = View.GONE
            } else {
                mTvNoData.visibility = View.GONE
                mRecyclerView.visibility = View.VISIBLE
                mAdapter.update(result.list)
            }
        }
    }

    companion object {
        const val TAG = "TemperRecordFragment"
        private const val USER_ID = "USER_ID"
        @JvmStatic fun newInstance(userId: String = "") = TemperRecordFragment().apply {
            arguments = Bundle().apply {
                putString(USER_ID, userId)
            }
        }
    }

}