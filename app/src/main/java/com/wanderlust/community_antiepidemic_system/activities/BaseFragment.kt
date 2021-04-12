package com.wanderlust.community_antiepidemic_system.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.wanderlust.community_antiepidemic_system.R
import com.wanderlust.community_antiepidemic_system.WanderlustApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

abstract class BaseFragment : Fragment(), CoroutineScope {

    private val mJob: Job by lazy { Job() }
    override val coroutineContext: CoroutineContext get() = mJob + Dispatchers.Main

    //全局变量
    val mUser  by lazy { (activity?.application as WanderlustApp?)?.gUser }
    val mAdmin by lazy { (activity?.application as WanderlustApp?)?.gAdmin }
    val mType  by lazy { (activity?.application as WanderlustApp?)?.gType ?: 0 }

    @LayoutRes abstract fun contentView(): Int

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(contentView(), container, false).apply {
            findView(this)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initView()
    }

    abstract fun findView(parent: View)

    abstract fun initView()

    override fun onDestroy() {
        super.onDestroy()
        mJob.cancel()
    }

}