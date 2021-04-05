package com.wanderlust.community_antiepidemic_system.activities.home

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.wanderlust.community_antiepidemic_system.R
import com.wanderlust.community_antiepidemic_system.utils.ViewPagerAdapter

class AdminHomeActivity : AppCompatActivity() {

    private lateinit var mTabLayout: TabLayout
    private lateinit var mViewPager: ViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_home)
        initView()
    }

    private fun initView() {
        mTabLayout = findViewById(R.id.tab_home_admin)
        mViewPager = findViewById(R.id.vp_home_admin)
        initViewPager()
    }

    private fun initViewPager() {
        val titles = arrayOf("概览", "用户管理")
        val fragments: MutableList<Fragment> = ArrayList()
        fragments.add(AdminOverviewFragment.newInstance())
        fragments.add(AdminUserListFragment.newInstance())
        mViewPager.adapter = ViewPagerAdapter(supportFragmentManager, fragments, titles)
        mTabLayout.setupWithViewPager(mViewPager)
    }

}