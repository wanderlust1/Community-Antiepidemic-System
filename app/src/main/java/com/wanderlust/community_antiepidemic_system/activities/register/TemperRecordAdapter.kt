package com.wanderlust.community_antiepidemic_system.activities.register

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wanderlust.community_antiepidemic_system.R
import com.wanderlust.community_antiepidemic_system.entity.TemperReg
import com.wanderlust.community_antiepidemic_system.utils.HealthType

class TemperRecordAdapter: RecyclerView.Adapter<TemperRecordAdapter.ViewHolder>() {

    private var mList = listOf<TemperReg>()

    private var mContext: Context? = null

    fun update(list: List<TemperReg>) {
        mList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (mContext == null) {
            mContext = parent.context
        }
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_temper_record, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mList[position]
        holder.itemView.background = mContext?.getDrawable(
            if (item.isDanger) R.drawable.bg_red_round_corner else R.drawable.bg_green_round_corner
        )
        holder.icon?.setImageResource(
            if (item.isDanger) R.drawable.ic_baseline_priority_high_24 else R.drawable.ic_baseline_done_24
        )
        holder.temper?.text = "${item.temper}°C"
        holder.date?.text = item.date
        holder.status?.text = if (item.status.contains("正常")) item.status else "健康状态：${item.status}"
        holder.approach?.text = HealthType.toString(item.approach)
        holder.diagnose?.text = HealthType.toString(item.diagnose)
    }

    override fun getItemCount(): Int = mList.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var temper: TextView? = null
        var date: TextView? = null
        var status: TextView? = null
        var approach: TextView? = null
        var diagnose: TextView? = null
        var icon: ImageView? = null
        init {
            temper = itemView.findViewById(R.id.tv_item_temper_temper)
            date = itemView.findViewById(R.id.tv_item_temper_date)
            status = itemView.findViewById(R.id.tv_item_temper_status)
            approach = itemView.findViewById(R.id.tv_item_temper_approach)
            diagnose = itemView.findViewById(R.id.tv_item_temper_diagnose)
            icon = itemView.findViewById(R.id.iv_item_temper_icon)
        }
    }

}