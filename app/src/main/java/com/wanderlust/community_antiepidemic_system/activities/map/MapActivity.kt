package com.wanderlust.community_antiepidemic_system.activities.map

import android.graphics.Color
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.mapapi.map.*
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.model.LatLngBounds
import com.baidu.mapapi.search.core.SearchResult
import com.baidu.mapapi.search.district.DistrictResult
import com.baidu.mapapi.search.district.DistrictSearch
import com.baidu.mapapi.search.district.DistrictSearchOption
import com.baidu.mapapi.search.poi.*
import com.baidu.mapapi.utils.SpatialRelationUtil
import com.wanderlust.community_antiepidemic_system.R
import com.wanderlust.community_antiepidemic_system.activities.BaseActivity
import com.wanderlust.community_antiepidemic_system.event.CommunityEvent
import com.wanderlust.community_antiepidemic_system.event.RiskAreaEvent
import com.wanderlust.community_antiepidemic_system.network.ServiceManager
import com.wanderlust.community_antiepidemic_system.utils.*
import com.wanderlust.community_antiepidemic_system.widget.DangerAreaView
import kotlinx.coroutines.*
import kotlin.collections.ArrayList

class MapActivity : BaseActivity() {

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

    private var mTotalAreaCount = 0   //待搜索的地区总数
    private var mCurrSearchedArea = 0 //当前已搜索的地区数
    private var isStartSearch = false
    private var isLocated = false      //当前是否定位成功
    private var isInDangerZone = false //当前定位位置是否在风险地区

    //包含所有风险地区的列表
    private val mPolyLineList: MutableList<List<List<LatLng>>> = ArrayList()

    //区域检索
    private val mDistrictSearchList = mutableListOf<DistrictSearch>()
    //搜索疫情风险地区的地点位置
    private val mPoiSearch: PoiSearch by lazy {
        PoiSearch.newInstance().apply {
            setOnGetPoiSearchResultListener(mOnRiskAreaPoiResult)
        }
    }
    //搜索自己的社区的位置
    private val mCommunityPoiSearch : PoiSearch by lazy {
        PoiSearch.newInstance().apply {
            setOnGetPoiSearchResultListener(mOnCommunityPoiResult)
        }
    }

    private val mOnCommunityPoiResult = object: OnPoiSearchResultAdapter() {
        override fun onGetPoiResult(result: PoiResult?) {
            //获取列表第一个poi点，即最符合要求的点作为标记位置
            val poiInfo = result?.allPoi?.iterator()?.next() ?: return
            mBaiduMap.addOverlay(poiInfo.drawMarker(this@MapActivity, true, "我的社区"))
        }
    }

    private val mOnRiskAreaPoiResult = object: OnPoiSearchResultAdapter() {
        override fun onGetPoiResult(result: PoiResult?) {
            //获取列表第一个poi点，即最符合要求的点作为标记位置
            val poiInfo = result?.allPoi?.iterator()?.next() ?: return
            mBaiduMap.addOverlay(poiInfo.drawMarker(this@MapActivity, false))
        }
    }

    override fun contentView() = R.layout.activity_map

    override fun findView() {
        mMapView = findViewById(R.id.map_view)
        mDangerAreaView = findViewById(R.id.dav_map_top)
    }

    override fun initView() {
        mBaiduMap = mMapView.map
        mBaiduMap.setViewPadding(30, 0, 30, 20)
        CommonUtils.requestPermission(this)
        CommonUtils.startOneLocation(mLocationClient, mOneLocationListener)
        requestRiskArea()
        requestCommunityAddress()
    }

    //定位的回调监听
    private val mOneLocationListener: BDAbstractLocationListener = object : BDAbstractLocationListener() {
        override fun onReceiveLocation(location: BDLocation?) {
            if (null == location) return
            val latLng = LatLng(location.latitude, location.longitude)
            mOneLocMarker?.remove()
            val markerOptions = MarkerOptions()
            markerOptions.position(latLng)
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(
                CommonUtils.drawBitmapFromVector(this@MapActivity, R.drawable.ic_location_blue)))
            markerOptions.zIndex(9)
            val textView = TextView(this@MapActivity).apply {
                textSize = 13f
                text = "我的位置"
                setTextColor(Color.WHITE)
                setPadding(10, 5, 10, 5)
                background = ContextCompat.getDrawable(this@MapActivity, R.drawable.bg_label_map_mark_blue)
            }
            markerOptions.scaleX(0.8f).scaleY(0.8f).infoWindow(InfoWindow(textView, latLng, -60))
            mOneLocMarker = mBaiduMap.addOverlay(markerOptions) as Marker
            val builder = LatLngBounds.Builder().include(latLng)
            val mapStatusUpdate = MapStatusUpdateFactory.newLatLngBounds(builder.build(), 0, 0, 0, 600)
            // 更新地图状态
            mBaiduMap.animateMapStatus(mapStatusUpdate)
            isLocated = location.locType == BDLocation.TypeGpsLocation ||
                    location.locType == BDLocation.TypeNetWorkLocation ||
                    location.locType == BDLocation.TypeOffLineLocation
            judgementDangerZone()
            CommonUtils.formatLocType(location.locType).toast(this@MapActivity)
        }
    }

    //停止单次定位
    private fun stopOneLocation() = mLocationClient.stop()

    //调用卫健委的接口并处理返回
    private fun requestRiskArea() {
        launch {
            val result = ServiceManager.requestRiskAreaData() ?: return@launch
            mTotalAreaCount = result.data.highList.size + result.data.midList.size
            isStartSearch = true
            mDangerAreaView.setData(result.data.highList, result.data.midList) { area ->
                //将地图移动至该风险地区
                val status = MapStatus.Builder().target(area.position).zoom(12f).build()
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(status))
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

    private fun requestCommunityAddress() {
        if (mUser == null || mUser?.communityId == null) return
        launch {
            val result = ServiceManager.request {
                val request = CommunityEvent.GetCommunityReq(mUser!!.communityId)
                it.getCommunityById(request.toJsonRequest())
            }
            if (result == null || result.code == CommunityEvent.FAIL) return@launch
            result.community.location.split("-").let {
                if (it.isEmpty()) return@let
                val option = PoiCitySearchOption()
                    .city(it[0])
                    .keyword(it[it.size - 1])
                    .pageCapacity(1).cityLimit(true)
                mCommunityPoiSearch.searchInCity(option)
            }
        }
    }

    //开始区域检索
    private fun startSearchDistrict(area: RiskAreaEvent.Area, isHigh: Boolean) {
        val districtSearch = DistrictSearch.newInstance()
        mDistrictSearchList.add(districtSearch)
        districtSearch.setOnDistrictSearchListener {
            area.isHigh = isHigh
            onSearchResult(it, area)
        }
        districtSearch.searchDistrict(DistrictSearchOption().cityName(area.city).districtName(area.county))
    }

    //地区检索结果
    private fun onSearchResult(result: DistrictResult?, area: RiskAreaEvent.Area) {
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
        polyLines.forEach { polyline ->
            val ooPolygon = PolygonOptions()
                .points(polyline)
                .stroke(Stroke(5, ContextCompat.getColor(this, strokeColor)))
                .fillColor(ContextCompat.getColor(this, fillColor))
                .zIndex(1)
            //添加边界到地图
            mBaiduMap.addOverlay(ooPolygon)
            for (latLng in polyline) {
                builder.include(latLng)
            }
        }
        //检索具体疫情风险社区的位置（标记点）
        for (community in area.communitys) {
            val option = PoiCitySearchOption().city(area.city).keyword(community).pageCapacity(1).cityLimit(true)
            mPoiSearch.searchInCity(option)
        }
        judgementDangerZone()
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
        mPoiSearch.destroy()
        mCommunityPoiSearch.destroy()
        mDistrictSearchList.forEach {
            it.destroy()
        }
    }

}