package com.wanderlust.community_antiepidemic_system

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
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
import com.wanderlust.community_antiepidemic_system.entity.Area
import com.wanderlust.community_antiepidemic_system.entity.RiskAreaReq
import com.wanderlust.community_antiepidemic_system.utils.DensityUtils
import com.wanderlust.community_antiepidemic_system.utils.MapUtils
import com.wanderlust.community_antiepidemic_system.widget.DangerAreaListView
import kotlinx.coroutines.*
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import kotlin.collections.ArrayList
import kotlin.coroutines.CoroutineContext

class MapActivity : AppCompatActivity(), OnGetPoiSearchResultListener, View.OnClickListener, CoroutineScope {

    private lateinit var mBaiduMap: BaiduMap
    private lateinit var mMapView: MapView
    private lateinit var mTopLayout: CardView
    private lateinit var mTopText: TextView
    private lateinit var mTopImage: ImageView
    private lateinit var mDangerListView: DangerAreaListView
    private lateinit var mDangerTips: TextView
    private lateinit var mDataUpdateDate: TextView
    private lateinit var mDataUpdateDiv: View

    //定位
    private var mLocClientOne: LocationClient? = null
    private var mOneLocMarker: Marker? = null

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
        requestPermission()
        initView()
        startOneLocation()
        startNetworkRequest()
    }

    private fun initView() {
        mMapView = findViewById(R.id.map_view)
        mTopLayout = findViewById(R.id.ll_map_top)
        mTopImage = findViewById(R.id.iv_map_top)
        mTopText = findViewById(R.id.tv_map_top)
        mDangerTips = findViewById(R.id.tv_map_top_danger_tips)
        mDangerListView = findViewById(R.id.list_map_top_detail)
        mDataUpdateDate = findViewById(R.id.tv_danger_area_footer)
        mDataUpdateDiv = findViewById(R.id.tv_danger_area_footer_div)
        mTopLayout.setOnClickListener(this)
        mBaiduMap = mMapView.map
        mBaiduMap.setViewPadding(30, 0, 30, 20)
    }

    private fun startNetworkRequest() {
        mJob = Job()
        launch {
            //疫情风险地区API的URL
            val baseUrl = "http://103.66.32.242:8005/zwfwMovePortal/interface/"
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
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
                .create(ApiService::class.java)
            val response = withContext(Dispatchers.IO) {
                retrofit.getRiskAreaData(RiskAreaReq(timestamp = timestamp)).execute()
            }
            Log.d("aaa", "onResponse: " + response.body())
            if (response.body() == null) {
                return@launch
            }
            //处理结果
            val result = response.body()!!
            mTotalAreaCount = result.data.highCount + result.data.midCount
            isStartSearch = true
            /*mDangerListView.setData(result.data.highList, result.data.midList) { area ->
                //将地图移动至该风险地区
                val builder = LatLngBounds.Builder().include(area.position)
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLngBounds(builder.build(), 0, 600, 0, 0))
            }
            mDangerListView.setDateText(result.data.time)
            if (mTotalAreaCount <= 0) {
                judgementDangerZone()
            } else {
                for (area in result.data.highList) {
                    val districtSearch = DistrictSearch.newInstance()
                    mDistrictSearchList.add(districtSearch)
                    districtSearch.setOnDistrictSearchListener {
                        area.isHigh = true
                        onSearchResult(it, area)
                    }
                    districtSearch.searchDistrict(DistrictSearchOption().cityName(area.city).districtName(area.county))
                }
                for (area in result.data.midList) {
                    val districtSearch = DistrictSearch.newInstance()
                    mDistrictSearchList.add(districtSearch)
                    districtSearch.setOnDistrictSearchListener {
                        area.isHigh = false
                        onSearchResult(it, area)
                    }
                    districtSearch.searchDistrict(DistrictSearchOption().cityName(area.city).districtName(area.county))
                }
            }*/
            val list1 = listOf<Area>(
                Area("广东省", "广州市", "番禺区", mutableListOf(
                    "大学城", "广州南站", "番禺广场", "大学城", "广州南站", "番禺广场")),
                Area("广东省", "梅州市", "兴宁市", mutableListOf(
                    "兴田街道", "宁中镇", "新陂镇", "罗岗镇")),
                Area("四川省", "成都市", "武侯区", mutableListOf(
                    "太平园", "九兴大道", "城通路", "高朋大道"))
            )
            val list2 = listOf<Area>(
                Area("广东省", "深圳市", "龙华区", mutableListOf(
                    "长湖", "坂田站", "万科城")),
                Area("山东省", "济南市", "济阳区", mutableListOf(
                    "黄河街道", "曲堤街道", "回河街道"))
            )
            mDangerListView.setData(list1, list2) { area ->
                //将地图移动至该风险地区
                val builder = LatLngBounds.Builder().include(area.position)
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLngBounds(builder.build(), 0, 600, 0, 0))
            }
            mDataUpdateDate.text = "数据来源：卫生健康委（${result.data.time}）"
            mTotalAreaCount = list1.size + list2.size
            if (mTotalAreaCount <= 0) {
                judgementDangerZone()
            } else {
                for (area in list1) {
                    val districtSearch = DistrictSearch.newInstance()
                    mDistrictSearchList.add(districtSearch)
                    districtSearch.setOnDistrictSearchListener {
                        area.isHigh = true
                        onSearchResult(it, area)
                    }
                    districtSearch.searchDistrict(DistrictSearchOption().cityName(area.city).districtName(area.county))
                }
                for (area in list2) {
                    val districtSearch = DistrictSearch.newInstance()
                    mDistrictSearchList.add(districtSearch)
                    districtSearch.setOnDistrictSearchListener {
                        area.isHigh = false
                        onSearchResult(it, area)
                    }
                    districtSearch.searchDistrict(DistrictSearchOption().cityName(area.city).districtName(area.county))
                }
            }
        }
    }

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
            BitmapDescriptorFactory.fromBitmap(drawBitmapFromVector(R.drawable.ic_baseline_location_on_24_red)))
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

    //启动单次定位
    private fun startOneLocation() {
        mLocClientOne = LocationClient(this)
        mLocClientOne!!.registerLocationListener(oneLocationListener)
        val locationClientOption = LocationClientOption()
        locationClientOption.locationMode = LocationClientOption.LocationMode.Hight_Accuracy
        locationClientOption.setCoorType("bd09ll")
        locationClientOption.setScanSpan(0)
        locationClientOption.setOnceLocation(true)
        locationClientOption.isOpenGps = true
        locationClientOption.setIsNeedAddress(true)
        mLocClientOne!!.locOption = locationClientOption
        mLocClientOne!!.start()
    }

    //单次定位回调监听
    private val oneLocationListener: BDAbstractLocationListener = object : BDAbstractLocationListener() {
        override fun onReceiveLocation(location: BDLocation?) {
            if (null == location) return
            val latLng = LatLng(location.latitude, location.longitude)
            if (null != mOneLocMarker) {
                mOneLocMarker?.remove()
            }
            val markerOptions = MarkerOptions()
            markerOptions.position(latLng)
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(
                drawBitmapFromVector(R.drawable.ic_baseline_location_on_24_blue)))
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
    private fun stopOneLocation() {
        if (null != mLocClientOne) {
            mLocClientOne!!.stop()
        }
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
        updateTopTips()
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

    private fun updateTopTips() {
        mTopLayout.visibility = View.VISIBLE
        mTopLayout.background = ContextCompat.getDrawable(this, if (isInDangerZone)
            R.drawable.bg_map_tips_red else R.drawable.bg_map_tips_green)
        mTopText.text = if (isInDangerZone) "当前位置处于中高风险地区中" else "当前位置位于中高风险地区之外"
        mDangerTips.visibility = if (isInDangerZone) View.VISIBLE else View.GONE
        mTopImage.setImageResource(if (isInDangerZone)
            R.drawable.ic_baseline_priority_high_24 else R.drawable.ic_baseline_done_24)
    }

    private fun drawBitmapFromVector(@DrawableRes vectorResId: Int): Bitmap? {
        val vectorDrawable = getDrawable(vectorResId) ?: return null
        val bitmap = Bitmap.createBitmap(
                vectorDrawable.intrinsicWidth,
                vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        vectorDrawable.draw(canvas)
        return bitmap
    }

    override fun onClick(v: View?) {
        mDataUpdateDiv.visibility = if (mDangerListView.visibility == View.GONE) View.VISIBLE else View.GONE
        mDataUpdateDate.visibility = if (mDangerListView.visibility == View.GONE) View.VISIBLE else View.GONE
        mDangerListView.visibility = if (mDangerListView.visibility == View.GONE) View.VISIBLE else View.GONE
    }

    override fun onResume() {
        super.onResume()
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy()
        stopOneLocation()
        mJob.cancel()
        mPoiSearch.destroy()
        mDistrictSearchList.forEach {
            it.destroy()
        }
    }

    //Android6.0之后需要动态申请权限
    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            val permissionsList: ArrayList<String> = ArrayList()
            val permissions = arrayOf(
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.INTERNET,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_WIFI_STATE)
            for (perm in permissions) {
                if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(perm)) {
                    permissionsList.add(perm)
                    // 进入到这里代表没有权限.
                }
            }
            if (permissionsList.isNotEmpty()) {
                val strings = arrayOfNulls<String>(permissionsList.size)
                requestPermissions(permissionsList.toArray(strings), 0)
            }
        }
    }

}