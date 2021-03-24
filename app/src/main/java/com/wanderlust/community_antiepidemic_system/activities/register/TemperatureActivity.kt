package com.wanderlust.community_antiepidemic_system.activities.register

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.wanderlust.community_antiepidemic_system.R

class TemperatureActivity : AppCompatActivity() {

    private lateinit var mTabLayout: TabLayout
    private lateinit var mViewPager: ViewPager
    private lateinit var mIvBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_temperature)
        initView()
    }

    private fun initView() {
        mTabLayout = findViewById(R.id.tab_temper)
        mViewPager = findViewById(R.id.vp_temper)
        mIvBack = findViewById(R.id.iv_temper_back)
        mIvBack.setOnClickListener {
            finish()
        }
        initViewPager()
    }

    private fun initViewPager() {
        val titles = arrayOf("健康登记", "健康登记记录")
        val fragments: MutableList<Fragment> = ArrayList()
        fragments.add(TemperRegFragment.newInstance())
        fragments.add(TemperRecordFragment.newInstance())
        mViewPager.adapter = ViewPagerAdapter(supportFragmentManager, fragments, titles)
        mTabLayout.setupWithViewPager(mViewPager)
    }

}