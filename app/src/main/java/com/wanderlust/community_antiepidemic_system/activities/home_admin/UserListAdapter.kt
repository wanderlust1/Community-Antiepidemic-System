package com.wanderlust.community_antiepidemic_system.activities.home_admin

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wanderlust.community_antiepidemic_system.R
import com.wanderlust.community_antiepidemic_system.entity.User

class UserListAdapter: RecyclerView.Adapter<UserListAdapter.ViewHolder>() {

    private var mList = listOf<User>()

    private var mContext: Context? = null

    private var mItemSelectListener: ((User) -> Unit)? = null

    fun update(list: List<User>) {
        mList = list
        notifyDataSetChanged()
    }

    fun setItemSelectListener(listener: (User) -> Unit) {
        mItemSelectListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (mContext == null) {
            mContext = parent.context
        }
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mList[position]
        holder.name?.text = item.userName
        holder.id?.text = "ID ${item.userId}"
        holder.cid?.text = "${item.cid} / ${item.phone}"
        holder.itemView.setOnClickListener {
            mItemSelectListener?.invoke(item)
        }
    }

    override fun getItemCount(): Int = mList.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name: TextView? = null
        var id: TextView? = null
        var cid: TextView? = null
        init {
            name = itemView.findViewById(R.id.tv_user_list_name)
            id = itemView.findViewById(R.id.tv_user_list_id)
            cid = itemView.findViewById(R.id.tv_user_list_cid)
        }
    }

}