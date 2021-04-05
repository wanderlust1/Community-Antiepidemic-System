package com.wanderlust.community_antiepidemic_system.activities.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.wanderlust.community_antiepidemic_system.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

class AdminUserListFragment : Fragment(), CoroutineScope {

    private val mJob: Job by lazy { Job() }
    override val coroutineContext: CoroutineContext get() = mJob + Dispatchers.Main

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_admin_user_list, container, false).apply {

        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

    }

    override fun onDestroy() {
        super.onDestroy()
        mJob.cancel()
    }

    companion object {
        const val TAG = "AdminUserListFragment"
        @JvmStatic fun newInstance() = AdminUserListFragment()
    }

}