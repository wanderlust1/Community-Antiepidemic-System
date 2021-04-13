package com.wanderlust.community_antiepidemic_system.activities.community

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.baidu.mapapi.search.sug.SuggestionResult.SuggestionInfo
import com.wanderlust.community_antiepidemic_system.R

class PoiAreaAdapter : RecyclerView.Adapter<PoiAreaAdapter.ViewHolder>() {

    private var mSuggestInfos: List<SuggestionInfo> = emptyList()

    private var mOnItemClickListener: ((SuggestionInfo) -> Unit)? = null

    fun update(suggestInfoList: List<SuggestionInfo>) {
        mSuggestInfos = suggestInfoList
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(listener: (SuggestionInfo) -> Unit) {
        mOnItemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_poi_area, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = mSuggestInfos.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val suggestInfo = mSuggestInfos[position]
        holder.mItemView.setOnClickListener {
            mOnItemClickListener?.invoke(suggestInfo)
        }
        holder.mPoiName.text = suggestInfo.getKey()
        holder.mPoiAddress.text = suggestInfo.getAddress()
    }

    class ViewHolder(var mItemView: View) : RecyclerView.ViewHolder(mItemView) {
        var mPoiName: TextView = mItemView.findViewById(R.id.tv_poi_area_name)
        var mPoiAddress: TextView = mItemView.findViewById(R.id.tv_poi_area_detail)
    }

}