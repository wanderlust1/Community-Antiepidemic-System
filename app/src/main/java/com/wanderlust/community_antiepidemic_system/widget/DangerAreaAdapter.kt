package com.wanderlust.community_antiepidemic_system.widget

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wanderlust.community_antiepidemic_system.R
import com.wanderlust.community_antiepidemic_system.event.Area

class DangerAreaAdapter(private val mHighList: List<Area>,
                        private val mMidList: List<Area>,
                        private val mListener: (area: Area) -> Unit): RecyclerView.Adapter<DangerAreaAdapter.ContentViewHolder>() {

    private var mContext: Context? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContentViewHolder {
        if (mContext == null) {
            mContext = parent.context
        }
        val view: View = LayoutInflater.from(mContext).inflate(R.layout.item_danger_area, parent, false)
        return ContentViewHolder(view)
    }

    override fun getItemCount(): Int {
        return (if (mHighList.isEmpty()) 1 else mHighList.size) + (if (mMidList.isEmpty()) 1 else mMidList.size)
    }

    override fun onBindViewHolder(holder: ContentViewHolder, position: Int) {
        val empty = Area()
        val area = if (mHighList.isEmpty()) {
            if (position == 0) empty else if (mMidList.isEmpty() && position == 1) empty else mMidList[position - 1]
        } else if (mMidList.isEmpty() && position == mHighList.size) {
            empty
        } else {
            if (position < mHighList.size) mHighList[position] else mMidList[position - mHighList.size]
        }
        val hSize = if (mHighList.isEmpty()) 1 else mHighList.size
        holder.title?.visibility = if (position == 0 || position == hSize) View.VISIBLE else View.GONE
        holder.title?.text = when (position) {
            0 -> "高风险地区（${mHighList.size}个）"
            hSize -> "中风险地区（${mMidList.size}个）"
            else -> ""
        }
        holder.cityName?.text = if (area === empty) {
            holder.cityName?.visibility = View.GONE
            ""
        } else {
            holder.cityName?.visibility = View.VISIBLE
            holder.cityName?.setOnClickListener {
                mListener.invoke(area)
            }
            "${area.province} ${area.city} ${area.county}"
        }
        holder.community?.text = if (area === empty || area.communitys.isEmpty()) {
            holder.community?.visibility = View.GONE
            ""
        } else {
            holder.community?.visibility = View.VISIBLE
            holder.community?.setOnClickListener {
                mListener.invoke(area)
            }
            buildString {
                for (i in 0 until area.communitys.size) {
                    append(area.communitys[i] + if (i < area.communitys.size - 1) "、" else "")
                }
            }
        }
        holder.point?.visibility = if (area === empty) View.GONE else View.VISIBLE
    }

    class ContentViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var title: TextView? = null
        var cityName: TextView? = null
        var community: TextView? = null
        var point: ImageView? = null
        init {
            title = itemView.findViewById(R.id.tv_item_danger_area_title)
            cityName = itemView.findViewById(R.id.tv_item_danger_area_city_name)
            community = itemView.findViewById(R.id.tv_item_danger_area_community)
            point = itemView.findViewById(R.id.iv_item_danger_area_point)
        }
    }

}