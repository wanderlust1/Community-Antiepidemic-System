package com.wanderlust.community_antiepidemic_system.widget

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wanderlust.community_antiepidemic_system.R
import com.wanderlust.community_antiepidemic_system.entity.Area
import com.wanderlust.community_antiepidemic_system.utils.DensityUtils

class DangerAreaListView : LinearLayout {

    private var mContext: Context? = null

    private lateinit var mRecyclerView: RecyclerView

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
        mRecyclerView.layoutManager = LinearLayoutManager(mContext)
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

}