package com.wanderlust.community_antiepidemic_system.activities.community

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wanderlust.community_antiepidemic_system.R
import com.wanderlust.community_antiepidemic_system.entity.Community
import com.wanderlust.community_antiepidemic_system.utils.LoginType

class SearchAdapter(private var mType: Int): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_ERROR = 0
    private val TYPE_NORMAL = 1

    private var mList = listOf<Community>()

    private var mMessage = ""

    private var mListener: ((Community) -> Unit)? = null

    private var mContext: Context? = null

    fun update(list: List<Community>) {
        mList = list
        notifyDataSetChanged()
    }

    fun update(message: String) {
        mList = listOf()
        mMessage = message
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(listener: (Community) -> Unit) {
        mListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (mContext == null) {
            mContext = parent.context
        }
        return if (viewType == TYPE_NORMAL) {
            val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_search_community, parent, false)
            ViewHolder(view)
        } else {
            val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_empty, parent, false)
            EmptyViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {
            val item = mList[position]
            holder.name?.text = item.name
            holder.address?.text = item.location
            holder.count?.text = "${item.count}人"
            holder.tag?.visibility = if (item.hasJoined == 1) View.VISIBLE else View.GONE
            holder.tag?.text = if (mType == LoginType.USER) "已加入" else "已绑定"
            holder.view.setOnClickListener {
                mListener?.invoke(item)
            }
        } else if (holder is EmptyViewHolder) {
            holder.message?.text = mMessage
            holder.icon?.visibility = if (mMessage.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (mList.isEmpty()) TYPE_ERROR else TYPE_NORMAL
    }

    override fun getItemCount(): Int {
        return if (mList.isEmpty()) 1 else mList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var view = itemView
        var name: TextView? = null
        var address: TextView? = null
        var count: TextView? = null
        var tag: TextView? = null
        init {
            name = itemView.findViewById(R.id.tv_isc_name)
            address = itemView.findViewById(R.id.tv_isc_address)
            count = itemView.findViewById(R.id.tv_isc_count)
            tag = itemView.findViewById(R.id.tv_isc_joined_tag)
        }
    }

    class EmptyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var message: TextView? = null
        var icon: ImageView? = null
        init {
            message = itemView.findViewById(R.id.tv_empty)
            icon = itemView.findViewById(R.id.iv_empty)
        }
    }

}