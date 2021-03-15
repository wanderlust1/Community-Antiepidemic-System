package com.wanderlust.community_antiepidemic_system.activities.map

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.mapapi.map.*
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.model.LatLngBounds
import com.baidu.mapapi.search.core.PoiInfo
import com.baidu.mapapi.search.core.SearchResult
import com.baidu.mapapi.search.district.DistrictResult
import com.baidu.mapapi.search.district.DistrictSearch
import com.baidu.mapapi.search.district.DistrictSearchOption
import com.baidu.mapapi.search.poi.*
import com.baidu.mapapi.utils.SpatialRelationUtil
import com.wanderlust.community_antiepidemic_system.network.ApiService
import com.wanderlust.community_antiepidemic_system.R
import com.wanderlust.community_antiepidemic_system.event.Area
import com.wanderlust.community_antiepidemic_system.event.RiskAreaReq
import com.wanderlust.community_antiepidemic_system.utils.MapUtils
import com.wanderlust.community_antiepidemic_system.utils.UrlUtils
import com.wanderlust.community_antiepidemic_system.widget.DangerAreaView
import kotlinx.coroutines.*
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import kotlin.collections.ArrayList
import kotlin.coroutines.CoroutineContext

class MapActivity : AppCompatActivity(), OnGetPoiSearchResultListener, CoroutineScope {

    companion object {
        const val TAG = "MapActivity"
    }

    private lateinit var mBaiduMap: BaiduMap
    private lateinit var mMapView: MapView
    private lateinit var mDangerAreaView: DangerAreaView

    //定位
    private var mOneLocMarker: Marker? = null
    private val mLocationClient: LocationClient by lazy {
        LocationClient(this)
    }

    //包含所有风险地区的列表
    private val mPolyLineList: MutableList<List<List<LatLng>>> = ArrayList()

    //地点/区域检索
    private val mDistrictSearchList = mutableListOf<DistrictSearch>()
    private val mPoiSearch: PoiSearch by lazy {
        val poiSearch = PoiSearch.newInstance()
        poiSearch.setOnGetPoiSearchResultListener(this)
        poiSearch
    }

    private var mTotalAreaCount = 0   //待搜索的地区总数
    private var mCurrSearchedArea = 0 //当前已搜索的地区数
    private var isStartSearch = false
    private var isLocated = false      //当前是否定位成功
    private var isInDangerZone = false //当前定位位置是否在风险地区

    //协程
    private lateinit var mJob: Job
    override val coroutineContext: CoroutineContext get() = mJob + Dispatchers.Main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        MapUtils.requestPermission(this)
        initView()
        MapUtils.startOneLocation(mLocationClient, mOneLocationListener)
        startNetworkRequest()
    }

    private fun initView() {
        mMapView = findViewById(R.id.map_view)
        mDangerAreaView = findViewById(R.id.dav_map_top)
        mBaiduMap = mMapView.map
        mBaiduMap.setViewPadding(30, 0, 30, 20)
    }

    //单次定位回调监听
    private val mOneLocationListener: BDAbstractLocationListener = object : BDAbstractLocationListener() {
        override fun onReceiveLocation(location: BDLocation?) {
            if (null == location) return
            val latLng = LatLng(location.latitude, location.longitude)
            mOneLocMarker?.remove()
            val markerOptions = MarkerOptions()
            markerOptions.position(latLng)
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(
                MapUtils.drawBitmapFromVector(this@MapActivity, R.drawable.ic_baseline_location_on_24_blue)))
            markerOptions.zIndex(9)
            mOneLocMarker = mBaiduMap.addOverlay(markerOptions) as Marker
            val builder = LatLngBounds.Builder().include(latLng)
            val mapStatusUpdate = MapStatusUpdateFactory.newLatLngBounds(builder.build(), 0, 0, 0, 600)
            // 更新地图状态
            mBaiduMap.animateMapStatus(mapStatusUpdate)
            isLocated = location.locType == BDLocation.TypeGpsLocation ||
                    location.locType == BDLocation.TypeNetWorkLocation ||
                    location.locType == BDLocation.TypeOffLineLocation
            judgementDangerZone()
            Toast.makeText(this@MapActivity, MapUtils.formatLocType(location.locType), Toast.LENGTH_SHORT).show()
        }
    }

    //停止单次定位
    private fun stopOneLocation() = mLocationClient.stop()

    //调用卫健委的接口并处理返回
    private fun startNetworkRequest() {
        mJob = Job()
        launch {
            //获得当前时间戳
            val timestamp = System.currentTimeMillis() / 1000
            //封装Header数据
            val client = withContext(Dispatchers.IO) {
                OkHttpClient.Builder().addInterceptor(object : Interceptor {
                    override fun intercept(chain: Interceptor.Chain): Response {
                        val signatureStr = "$timestamp${RiskAreaReq.STATE_COUNCIL_SIGNATURE_KEY}$timestamp"
                        val signature = RiskAreaReq.getSHA256StrJava(signatureStr).toUpperCase(Locale.ROOT)
                        val build = chain.request().newBuilder()
                            .addHeader("x-wif-nonce", RiskAreaReq.STATE_COUNCIL_X_WIF_NONCE)
                            .addHeader("x-wif-paasid", RiskAreaReq.STATE_COUNCIL_X_WIF_PAASID)
                            .addHeader("x-wif-signature", signature)
                            .addHeader("x-wif-timestamp", timestamp.toString())
                            .build()
                        return chain.proceed(build)
                    }
                }).retryOnConnectionFailure(true).build()
            }
            //创建发送请求
            val retrofit = Retrofit.Builder()
                .baseUrl(UrlUtils.AREA_DATA_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
                .create(ApiService::class.java)
            val response = withContext(Dispatchers.IO) {
                retrofit.getRiskAreaData(RiskAreaReq(timestamp = timestamp)).execute()
            }
            Log.d(TAG, "onResponse: " + response.body())
            if (response.body() == null) return@launch
            //处理结果
            val result = response.body()!!
            mTotalAreaCount = result.data.highCount + result.data.midCount
            isStartSearch = true
            mDangerAreaView.setData(result.data.highList, result.data.midList) { area ->
                //将地图移动至该风险地区
                val builder = LatLngBounds.Builder().include(area.position)
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLngBounds(builder.build(), 0, 600, 0, 0))
            }
            mDangerAreaView.updateFooter("数据来源：卫生健康委（${result.data.time}）")
            if (mTotalAreaCount <= 0) {
                judgementDangerZone()
            } else {
                result.data.highList.forEach {
                    startSearchDistrict(it, true)
                }
                result.data.midList.forEach {
                    startSearchDistrict(it, false)
                }
            }
        }
    }

    //开始区域检索
    private fun startSearchDistrict(area: Area, isHigh: Boolean) {
        val districtSearch = DistrictSearch.newInstance()
        mDistrictSearchList.add(districtSearch)
        districtSearch.setOnDistrictSearchListener {
            area.isHigh = isHigh
            onSearchResult(it, area)
        }
        districtSearch.searchDistrict(DistrictSearchOption().cityName(area.city).districtName(area.county))
    }

    //地区检索结果
    private fun onSearchResult(result: DistrictResult?, area: Area) {
        mCurrSearchedArea++
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            return
        }
        area.position = result.centerPt
        val strokeColor = if (area.isHigh) R.color.red_danger else R.color.yellow_middle
        val fillColor = if (area.isHigh) R.color.red_danger_bg else R.color.yellow_middle_bg
        val polyLines = result.getPolylines() ?: return //绘制的多边形
        val builder = LatLngBounds.Builder()
        mPolyLineList.add(polyLines)
        for (polyline in polyLines) {
            val ooPolygon: OverlayOptions = PolygonOptions()
                .points(polyline)
                .stroke(Stroke(5, ContextCompat.getColor(this, strokeColor)))
                .fillColor(ContextCompat.getColor(this, fillColor))
                .zIndex(1)
            mBaiduMap.addOverlay(ooPolygon)
            for (latLng in polyline) {
                builder.include(latLng)
            }
        }
        //检索具体疫情风险社区的位置
        for (community in area.communitys) {
            val option = PoiCitySearchOption().city(area.city).keyword(community).pageCapacity(1).cityLimit(true)
            mPoiSearch.searchInCity(option)
        }
        judgementDangerZone()
    }

    //poi检索结果
    override fun onGetPoiResult(result: PoiResult?) {
        val poiInfos: List<PoiInfo> = result?.allPoi ?: return
        if (poiInfos.isEmpty()) return
        val poiInfo = poiInfos.iterator().next()
        val markerOptions = MarkerOptions().position(poiInfo.getLocation()).icon(
            BitmapDescriptorFactory.fromBitmap(MapUtils.drawBitmapFromVector(
                this@MapActivity, R.drawable.ic_baseline_location_on_24_red
            )))
        val textView = TextView(this)
        textView.textSize = 12f
        textView.text = poiInfo.getName()
        textView.setPadding(10, 5, 10, 5)
        textView.background = ContextCompat.getDrawable(this, R.drawable.bg_label_map_mark)
        val infoWindow = InfoWindow(textView, poiInfo.getLocation(), -60)
        markerOptions.scaleX(0.8f).scaleY(0.8f).infoWindow(infoWindow)
        mBaiduMap.addOverlay(markerOptions)
    }

    override fun onGetPoiIndoorResult(result: PoiIndoorResult?) {
    }

    override fun onGetPoiDetailResult(result: PoiDetailResult?) {
    }

    override fun onGetPoiDetailResult(result: PoiDetailSearchResult?) {
    }

    //判断当前定位是否在中高风险地区，并更新顶部提示
    private fun judgementDangerZone() {
        if (!isLocated || !isStartSearch || mCurrSearchedArea < mTotalAreaCount) {
            return
        }
        for (polyLines in mPolyLineList) {
            if (isPointInPolygon(polyLines, mOneLocMarker?.position)) {
                isInDangerZone = true
                break
            }
        }
        mDangerAreaView.visibility = View.VISIBLE
        mDangerAreaView.updateTopTips(isInDangerZone)
    }

    // 判断点是否在多边形内
    private fun isPointInPolygon(polyLines: List<List<LatLng>>, position: LatLng?): Boolean {
        for (list in polyLines) {
            if (SpatialRelationUtil.isPolygonContainsPoint(list, position)) {
                return true
            }
        }
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        mMapView.onDestroy()
        stopOneLocation()
        mJob.cancel()
        mPoiSearch.destroy()
        mDistrictSearchList.forEach {
            it.destroy()
        }
    }

}