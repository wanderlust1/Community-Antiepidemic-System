package com.wanderlust.community_antiepidemic_system.activities

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import com.wanderlust.community_antiepidemic_system.WanderlustApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.coroutines.CoroutineContext

abstract class BaseActivity : AppCompatActivity(), CoroutineScope {

    //协程
    private val mJob: Job by lazy { Job() }
    override val coroutineContext: CoroutineContext get() = mJob + Dispatchers.Main

    //全局变量
    val mUser  by lazy { (application as WanderlustApp?)?.gUser }
    val mAdmin by lazy { (application as WanderlustApp?)?.gAdmin }
    val mType  by lazy { (application as WanderlustApp?)?.gType ?: 0 }

    @LayoutRes abstract fun contentView(): Int

    abstract fun findView()

    abstract fun initView()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(contentView())
        findView()
        initView()
    }

    override fun onDestroy() {
        super.onDestroy()
        mJob.cancel()
    }

}