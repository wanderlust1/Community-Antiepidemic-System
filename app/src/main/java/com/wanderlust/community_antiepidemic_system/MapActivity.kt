package com.wanderlust.community_antiepidemic_system

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.baidu.mapapi.map.*
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.model.LatLngBounds
import com.baidu.mapapi.search.core.SearchResult
import com.baidu.mapapi.search.district.DistrictResult
import com.baidu.mapapi.search.district.DistrictSearch
import com.baidu.mapapi.search.district.DistrictSearchOption
import com.baidu.mapapi.search.district.OnGetDistricSearchResultListener
import com.baidu.mapapi.utils.SpatialRelationUtil
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MapActivity : AppCompatActivity(), OnGetDistricSearchResultListener, View.OnClickListener {

    private lateinit var mBaiduMap: BaiduMap
    private lateinit var mMapView: MapView
    private lateinit var mButton: FloatingActionButton
    private lateinit var mText: TextView

    //定位
    private var mLocClientOne: LocationClient? = null
    private var mOneLocMarker: Marker? = null
    private val mBitmapBlue: BitmapDescriptor? = null

    private val mPolygonList: MutableList<Polygon> = ArrayList()

    //地点检索对象
    private val mDistrictSearch: DistrictSearch by lazy {
        val districtSearch = DistrictSearch.newInstance()
        districtSearch.setOnDistrictSearchListener(this)
        districtSearch
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        requestPermission()
        mMapView = findViewById(R.id.map_view)
        mButton = findViewById(R.id.btn_danger_zone)
        mText = findViewById(R.id.tv_msg)
        mBaiduMap = mMapView.map
        startOneLocation()
        mButton.setOnClickListener(this)
        mBaiduMap.setViewPadding(30, 0, 30, 20)
    }

    /**
     * 启动单次定位
     */
    private fun startOneLocation() {
        mLocClientOne = LocationClient(this)
        mLocClientOne!!.registerLocationListener(oneLocationListener)
        val locationClientOption = LocationClientOption()
        // 可选，设置定位模式，默认高精度 LocationMode.Hight_Accuracy：高精度；
        locationClientOption.locationMode = LocationClientOption.LocationMode.Hight_Accuracy
        // 可选，设置返回经纬度坐标类型，默认GCJ02
        locationClientOption.setCoorType("bd09ll")
        // 如果设置为0，则代表单次定位，即仅定位一次，默认为0
        // 如果设置非0，需设置1000ms以上才有效
        locationClientOption.setScanSpan(0)
        // 设置是否进行单次定位，单次定位时调用start之后会默认返回一次定位结果
        locationClientOption.setOnceLocation(true)
        //可选，设置是否使用gps，默认false
        locationClientOption.isOpenGps = true
        // 可选，是否需要地址信息，默认为不需要，即参数为false
        // 如果开发者需要获得当前点的地址信息，此处必须为true
        locationClientOption.setIsNeedAddress(true)
        // 设置定位参数
        mLocClientOne!!.locOption = locationClientOption
        // 开启定位
        mLocClientOne!!.start()
    }

    /** 单次定位回调监听 */
    private val oneLocationListener: BDAbstractLocationListener = object : BDAbstractLocationListener() {
        /**
         * 定位请求回调函数
         * @param location 定位结果
         */
        override fun onReceiveLocation(location: BDLocation?) {
            if (null == location) {
                return
            }
            val latLng = LatLng(location.latitude, location.longitude)
            addOneLocMarker(latLng)
            val builder = LatLngBounds.Builder()
            builder.include(latLng)
            val padding = 0
            val paddingBottom = 600
            val mapStatusUpdate = MapStatusUpdateFactory.newLatLngBounds(builder.build(), padding,
                    padding, padding, paddingBottom)
            val sb = StringBuffer(256)
            // 更新地图状态
            mBaiduMap.animateMapStatus(mapStatusUpdate)
            when (location.locType) {
                BDLocation.TypeGpsLocation -> { // GPS定位结果
                    sb.append("gps定位成功")
                }
                BDLocation.TypeNetWorkLocation -> { // 网络定位结果
                    sb.append("网络定位成功")
                }
                BDLocation.TypeOffLineLocation -> { // 离线定位结果
                    sb.append("离线定位成功")
                }
                BDLocation.TypeServerError -> {
                    sb.append("服务端网络定位失败")
                }
                BDLocation.TypeNetWorkException -> {
                    sb.append("网络不同导致定位失败，请检查网络是否通畅")
                }
                BDLocation.TypeCriteriaException -> {
                    sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机")
                }
            }
            val locationStr: String? = MapUtils.getLocationStr(location, mLocClientOne)
            if (!TextUtils.isEmpty(locationStr)) {
                sb.append(locationStr)
            }
            mText.text = sb.toString()
        }
    }

    /**
     * 添加地图标记
     * @param latLng
     */
    private fun addOneLocMarker(latLng: LatLng) {
        if (null != mOneLocMarker) {
            mOneLocMarker?.remove()
        }
        val markerOptions = MarkerOptions()
        markerOptions.position(latLng)
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(drawBitmapFromVector(R.drawable.ic_baseline_location_on_24)))
        markerOptions.zIndex(9)
        mOneLocMarker = mBaiduMap.addOverlay(markerOptions) as Marker
    }

    /**
     * 停止单次定位
     */
    private fun stopOneLocation() {
        if (null != mLocClientOne) {
            mLocClientOne!!.stop()
        }
    }

    override fun onClick(v: View?) {
        mDistrictSearch.searchDistrict(DistrictSearchOption().cityName("梅州").districtName("兴宁"))
        mDistrictSearch.searchDistrict(DistrictSearchOption().cityName("广州").districtName("番禺"))
        mDistrictSearch.searchDistrict(DistrictSearchOption().cityName("梅州").districtName("梅江区"))
    }

    /**
     * 检索回调结果
     * @param districtResult 行政区域信息查询结果
     */
    override fun onGetDistrictResult(districtResult: DistrictResult?) {
        if (districtResult == null || districtResult.error != SearchResult.ERRORNO.NO_ERROR) {
            return
        }
        val polyLines = districtResult.getPolylines() ?: return
        val builder = LatLngBounds.Builder()
        for (polyline in polyLines) {
            val ooPolygon: OverlayOptions = PolygonOptions()
                    .points(polyline)
                    .stroke(Stroke(5, Color.parseColor("#EC5050")))
                    .fillColor(Color.parseColor("#8AE86161"))
                    .zIndex(1)
            val polygon = mBaiduMap.addOverlay(ooPolygon) as Polygon
            mPolygonList.add(polygon)
            for (latLng in polyline) {
                builder.include(latLng)
            }
        }
        mText.text = buildString {
            append(mText.text.toString())
            append("\n定位位置")
            append(if (isPointInPolygon(polyLines, mOneLocMarker?.position)) "在" else "不在")
            append("疫情高风险区域（${districtResult.cityName}）内")
        }
        mButton.visibility = View.GONE
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
    }

    /**
     * Android6.0之后需要动态申请权限
     */
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