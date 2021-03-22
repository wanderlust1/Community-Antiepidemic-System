package com.wanderlust.community_antiepidemic_system.activities.register

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.gson.Gson
import com.wanderlust.community_antiepidemic_system.R
import com.wanderlust.community_antiepidemic_system.WanderlustApp
import com.wanderlust.community_antiepidemic_system.event.RegEvent
import com.wanderlust.community_antiepidemic_system.network.ApiService
import com.wanderlust.community_antiepidemic_system.utils.DensityUtils
import com.wanderlust.community_antiepidemic_system.utils.UrlUtils
import com.wanderlust.community_antiepidemic_system.utils.toast
import kotlinx.coroutines.*
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.ConnectException
import kotlin.coroutines.CoroutineContext

class TemperRecordFragment : Fragment(), CoroutineScope {

    private lateinit var mRefreshLayout: SwipeRefreshLayout
    private lateinit var mRecyclerView: RecyclerView

    private val mAdapter = TemperRecordAdapter()

    private val mJob: Job by lazy { Job() }
    override val coroutineContext: CoroutineContext get() = mJob + Dispatchers.Main

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_temper_record, container, false).apply {
            mRefreshLayout = findViewById(R.id.srl_temper_record)
            mRecyclerView = findViewById(R.id.rv_temper_record)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mRecyclerView.adapter = mAdapter
        mRecyclerView.layoutManager = LinearLayoutManager(activity)
        mRecyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                if (parent.getChildAdapterPosition(view) == 0 && activity != null) {
                    outRect.top = DensityUtils.dp2px(activity!!, 10f)
                }
            }
        })
        mRefreshLayout.setOnRefreshListener {
            requestData()
        }
        requestData()
    }

    private fun requestData() {
        val id = (activity?.application as WanderlustApp).gUser?.userId ?: return
        launch {
            val retrofit = Retrofit.Builder()
                .baseUrl(UrlUtils.SERVICE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
            val response = try {
                withContext(Dispatchers.IO) {
                    val request = RegEvent.GetTemperRecordReq(id)
                    retrofit.getTemperRecord(Gson().toJson(request).toRequestBody()).execute()
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
            mAdapter.update(result.list)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mJob.cancel()
    }

    companion object {
        const val TAG = "TemperRecordFragment"
        @JvmStatic fun newInstance() = TemperRecordFragment()
    }

}