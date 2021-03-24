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
import com.baidu.mapapi.map.MapStatusUpdateFactory
import com.baidu.mapapi.model.LatLngBounds
import com.google.gson.Gson
import com.tencent.mmkv.MMKV
import com.wanderlust.community_antiepidemic_system.R
import com.wanderlust.community_antiepidemic_system.WanderlustApp
import com.wanderlust.community_antiepidemic_system.activities.map.MapActivity
import com.wanderlust.community_antiepidemic_system.entity.OutSideReg
import com.wanderlust.community_antiepidemic_system.event.RegEvent
import com.wanderlust.community_antiepidemic_system.event.RiskAreaEvent
import com.wanderlust.community_antiepidemic_system.network.ApiService
import com.wanderlust.community_antiepidemic_system.utils.DensityUtils
import com.wanderlust.community_antiepidemic_system.utils.MapUtils
import com.wanderlust.community_antiepidemic_system.utils.UrlUtils
import com.wanderlust.community_antiepidemic_system.utils.toast
import kotlinx.coroutines.*
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.ConnectException
import java.util.*
import kotlin.coroutines.CoroutineContext

class OutsideRecordFragment : Fragment(), CoroutineScope {

    private lateinit var mRefreshLayout: SwipeRefreshLayout
    private lateinit var mRecyclerView: RecyclerView

    private val mAdapter = OutsideRecordAdapter()

    private val mRecordList = mutableListOf<OutSideReg>()

    private val mJob: Job by lazy { Job() }
    override val coroutineContext: CoroutineContext get() = mJob + Dispatchers.Main

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_outside_record, container, false).apply {
            mRefreshLayout = findViewById(R.id.srl_outside_record)
            mRecyclerView = findViewById(R.id.rv_outside_record)
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
                    val request = RegEvent.GetOutSideRecordReq(id)
                    retrofit.getOutsideRecord(Gson().toJson(request).toRequestBody()).execute()
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
            mRecordList.clear()
            mRecordList.addAll(result.list)
            mAdapter.update(mRecordList)
            requestRiskAreaData()
        }
    }

    private fun requestRiskAreaData() {
        launch {
            val kv = MMKV.defaultMMKV()
            val localResult = MapUtils.readRiskAreaMMKV(kv)
            val result = if (localResult != null) {
                localResult
            } else {
                //获得当前时间戳
                val timestamp = System.currentTimeMillis() / 1000
                //封装Header数据
                val client = withContext(Dispatchers.IO) {
                    OkHttpClient.Builder().addInterceptor(object : Interceptor {
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
                }
                val retrofit = Retrofit.Builder()
                    .baseUrl(UrlUtils.AREA_DATA_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build()
                    .create(ApiService::class.java)
                val response = try {
                    withContext(Dispatchers.IO) {
                        retrofit.getRiskAreaData(RiskAreaEvent.RiskAreaReq(timestamp = timestamp)).execute()
                    }
                } catch (e: ConnectException) {
                    R.string.connection_error.toast(activity)
                    null
                } catch (e: Exception) {
                    R.string.timeout_error.toast(activity)
                    null
                }
                Log.d(TAG, "onResponse: " + response?.body())
                if (response?.body() == null) return@launch
                MapUtils.saveRiskAreaMMKV(kv, response.body())
                response.body()!!
            }
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
        @JvmStatic fun newInstance() = OutsideRecordFragment()
    }

}