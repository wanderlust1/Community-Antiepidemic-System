package com.wanderlust.community_antiepidemic_system.activities.register

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.gson.Gson
import com.wanderlust.community_antiepidemic_system.R
import com.wanderlust.community_antiepidemic_system.WanderlustApp
import com.wanderlust.community_antiepidemic_system.entity.OutSideReg
import com.wanderlust.community_antiepidemic_system.event.RegEvent
import com.wanderlust.community_antiepidemic_system.network.ServiceManager
import com.wanderlust.community_antiepidemic_system.utils.CommonUtils
import com.wanderlust.community_antiepidemic_system.utils.toast
import kotlinx.coroutines.*
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.ConnectException
import kotlin.coroutines.CoroutineContext

class OutsideRecordFragment : Fragment(), CoroutineScope {

    private var mUserId = ""

    private lateinit var mRefreshLayout: SwipeRefreshLayout
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mTvNoData: TextView

    private val mAdapter = OutsideRecordAdapter()

    private val mRecordList = mutableListOf<OutSideReg>()

    private val mJob: Job by lazy { Job() }
    override val coroutineContext: CoroutineContext get() = mJob + Dispatchers.Main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mUserId = arguments?.getString(USER_ID) ?: ""
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_outside_record, container, false).apply {
            mRefreshLayout = findViewById(R.id.srl_outside_record)
            mRecyclerView = findViewById(R.id.rv_outside_record)
            mTvNoData = findViewById(R.id.tv_outside_record_no_data)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
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
                    val request = RegEvent.GetOutSideRecordReq(id)
                    ServiceManager.client.getOutsideRecord(Gson().toJson(request).toRequestBody()).execute()
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
                mRecordList.clear()
                mRecordList.addAll(result.list)
                mAdapter.update(mRecordList)
                requestRiskAreaData()
            }
        }
    }

    private fun requestRiskAreaData() {
        launch {
            val result = ServiceManager.requestRiskAreaData() ?: return@launch
            val areas = mutableListOf<String>()
            result.data.highList.forEach {
                areas.add(it.county)
            }
            result.data.midList.forEach {
                areas.add(it.county)
            }
            mRecordList.forEach {
                for (area in areas) {
                    if (it.city.contains(area)) {
                        it.isRiskArea = true
                        break
                    }
                }
            }
            mAdapter.update(mRecordList)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mJob.cancel()
    }

    companion object {
        const val TAG = "OutsideRecordFragment"
        private const val USER_ID = "USER_ID"
        @JvmStatic fun newInstance(userId: String = "") = OutsideRecordFragment().apply {
            arguments = Bundle().apply {
                putString(USER_ID, userId)
            }
        }
    }

}