package com.wanderlust.community_antiepidemic_system.activities.register

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.wanderlust.community_antiepidemic_system.R

class OutsideActivity : AppCompatActivity() {

    private lateinit var mTabLayout: TabLayout
    private lateinit var mViewPager: ViewPager
    private lateinit var mIvBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_outside)
        initView()
    }

    private fun initView() {
        mTabLayout = findViewById(R.id.tab_outside)
        mViewPager = findViewById(R.id.vp_outside)
        mIvBack = findViewById(R.id.iv_outside_back)
        mIvBack.setOnClickListener {
            finish()
        }
        initViewPager()
    }

    private fun initViewPager() {
        val titles = arrayOf("外出登记", "外出记录")
        val fragments: MutableList<Fragment> = ArrayList()
        fragments.add(OutsideRegFragment.newInstance())
        fragments.add(OutsideRecordFragment.newInstance())
        mViewPager.adapter = ViewPagerAdapter(supportFragmentManager, fragments, titles)
        mTabLayout.setupWithViewPager(mViewPager)
    }

}