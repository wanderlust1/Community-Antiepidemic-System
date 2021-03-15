package com.wanderlust.community_antiepidemic_system

import android.app.Application
import com.baidu.mapapi.CoordType
import com.baidu.mapapi.SDKInitializer
import com.tencent.mmkv.MMKV
import com.wanderlust.community_antiepidemic_system.entity.Admin
import com.wanderlust.community_antiepidemic_system.entity.User


class WanderlustApp: Application() {

    var gType = 0

    var gUser: User? = null

    var gAdmin: Admin? = null

    override fun onCreate() {
        super.onCreate()
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        SDKInitializer.initialize(this)
        SDKInitializer.setCoordType(CoordType.BD09LL)
        //MMKV
        MMKV.initialize(filesDir.absolutePath + "/mmkv")
    }

}