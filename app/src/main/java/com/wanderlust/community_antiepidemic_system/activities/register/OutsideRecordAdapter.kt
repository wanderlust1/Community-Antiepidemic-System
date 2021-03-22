package com.wanderlust.community_antiepidemic_system.activities.register

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wanderlust.community_antiepidemic_system.R
import com.wanderlust.community_antiepidemic_system.entity.OutSideReg

class OutsideRecordAdapter: RecyclerView.Adapter<OutsideRecordAdapter.ViewHolder>() {

    private var mList = listOf<OutSideReg>()

    private var mContext: Context? = null

    fun update(list: List<OutSideReg>) {
        mList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (mContext == null) {
            mContext = parent.context
        }
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_outside_record, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mList[position]
        holder.city?.text = item.city
        holder.date?.text = "外出时间：${item.startTime} 至 ${item.endTime}"
        holder.reason?.text = "外出原因：${item.reason}"
        holder.phone?.text = "紧急联系方式：${item.phone}"
        holder.dangerTips?.visibility = if (item.isRiskArea) View.VISIBLE else View.GONE
    }

    override fun getItemCount(): Int = mList.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var city: TextView? = null
        var date: TextView? = null
        var reason: TextView? = null
        var phone: TextView? = null
        var dangerTips: LinearLayout? = null
        init {
            city = itemView.findViewById(R.id.tv_item_outside_city)
            date = itemView.findViewById(R.id.tv_item_outside_date)
            reason = itemView.findViewById(R.id.tv_item_outside_reason)
            phone = itemView.findViewById(R.id.tv_item_outside_phone)
            dangerTips = itemView.findViewById(R.id.ll_item_outside_danger_tips)
        }
    }

}