package com.wanderlust.community_antiepidemic_system.widget

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wanderlust.community_antiepidemic_system.R
import com.wanderlust.community_antiepidemic_system.event.Area
import com.wanderlust.community_antiepidemic_system.utils.DensityUtils

class DangerAreaView : LinearLayout, View.OnClickListener {

    private var mContext: Context? = null

    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mTopText: TextView
    private lateinit var mTopImage: ImageView
    private lateinit var mDangerTips: TextView
    private lateinit var mDataUpdateDate: TextView
    private lateinit var mDataUpdateDiv: View
    private lateinit var mCollapse: LinearLayout

    constructor(context: Context?) : super(context, null) {
        mContext = context
        initView()
    }

    constructor(context: Context?, attrs: AttributeSet?): super(context, attrs, 0) {
        mContext = context
        initView()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr, 0) {
        mContext = context
        initView()
    }

    private fun initView() {
        val view = LayoutInflater.from(mContext).inflate(R.layout.layout_danger_area_list, this, true)
        mRecyclerView = view.findViewById(R.id.rv_danger_area)
        mTopImage = findViewById(R.id.iv_map_top)
        mTopText = findViewById(R.id.tv_map_top)
        mDangerTips = findViewById(R.id.tv_map_top_danger_tips)
        mDataUpdateDate = findViewById(R.id.tv_danger_area_footer)
        mDataUpdateDiv = findViewById(R.id.tv_danger_area_footer_div)
        mCollapse = findViewById(R.id.ll_danger_area_collapse_layout)
        mRecyclerView.layoutManager = LinearLayoutManager(mContext)
        setOnClickListener(this)
    }

    fun setData(highList: List<Area>, midList: List<Area>, listener: (area: Area) -> Unit) {
        mRecyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                if (parent.getChildAdapterPosition(view) != 0 && mContext != null) {
                    outRect.top = DensityUtils.dp2px(mContext!!, 5f)
                }
            }
        })
        mRecyclerView.adapter = DangerAreaAdapter(highList, midList, listener)
    }

    fun updateFooter(str: String) {
        mDataUpdateDate.text = str
    }

    fun updateTopTips(isInDangerZone: Boolean) {
        background = ContextCompat.getDrawable(mContext ?: return, if (isInDangerZone)
            R.drawable.bg_map_tips_red else R.drawable.bg_map_tips_green)
        mTopText.text = if (isInDangerZone) "当前位置处于中高风险地区中" else "当前位置位于中高风险地区之外"
        mDangerTips.visibility = if (isInDangerZone) View.VISIBLE else View.GONE
        mTopImage.setImageResource(if (isInDangerZone)
            R.drawable.ic_baseline_priority_high_24 else R.drawable.ic_baseline_done_24)
    }

    override fun onClick(v: View?) {
        mCollapse.visibility = if (mCollapse.visibility == View.GONE) View.VISIBLE else View.GONE
    }

}