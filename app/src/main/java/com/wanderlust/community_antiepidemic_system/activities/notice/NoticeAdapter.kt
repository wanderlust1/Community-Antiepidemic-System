package com.wanderlust.community_antiepidemic_system.activities.notice

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wanderlust.community_antiepidemic_system.R
import com.wanderlust.community_antiepidemic_system.entity.Admin
import com.wanderlust.community_antiepidemic_system.entity.Notice
import com.wanderlust.community_antiepidemic_system.utils.LoginType

class NoticeAdapter(private val mType: Int): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_ERROR = 0
    private val TYPE_NORMAL = 1

    private var mList = listOf<Notice>()

    private var mMessage = ""

    private var mListener: ((Notice) -> Unit)? = null
    private var mJumpListener: (() -> Unit)? = null

    private var mContext: Context? = null

    fun update(list: List<Notice>) {
        mList = list
        notifyDataSetChanged()
    }

    fun update(message: String) {
        mList = listOf()
        mMessage = message
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(listener: (Notice) -> Unit) {
        mListener = listener
    }

    fun setJumpListener(listener: () -> Unit) {
        mJumpListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (mContext == null) {
            mContext = parent.context
        }
        return if (viewType == TYPE_NORMAL) {
            val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_notice, parent, false)
            ViewHolder(view)
        } else {
            val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_empty, parent, false)
            EmptyViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {
            val item = mList[position]
            holder.date?.text = "发布于 ${item.date}"
            if (item.content.isBlank()) {
                holder.content?.visibility = View.GONE
            } else {
                holder.content?.visibility = View.VISIBLE
                holder.content?.text = item.content
            }
            if (item.title.isBlank()) {
                holder.title?.visibility = View.GONE
            } else {
                holder.title?.visibility = View.VISIBLE
                holder.title?.text = item.title
            }
            holder.tagNoRead?.visibility = if (mType == LoginType.ADMIN || item.hasRead == 1)
                View.GONE else View.VISIBLE
            holder.tagRead?.visibility = if (item.hasRead == 1 && mType == LoginType.USER)
                View.VISIBLE else View.GONE
            holder.tagAlert?.visibility = if (item.type == Notice.NOTICE_ALERT) View.VISIBLE else View.GONE
            holder.toRegister?.visibility = if (item.type == Notice.NOTICE_ALERT && mType == LoginType.USER)
                View.VISIBLE else View.GONE
            holder.toRegister?.setOnClickListener {
                mJumpListener?.invoke()
            }
            holder.readCount?.visibility = if (mType == LoginType.ADMIN) View.VISIBLE else View.GONE
            holder.readCount?.text = "${item.readCount}人已读"
            holder.itemView.setOnClickListener {
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
        var date: TextView? = null
        var content: TextView? = null
        var toRegister: TextView? = null
        var tagAlert: TextView? = null
        var tagNoRead: TextView? = null
        var tagRead: TextView? = null
        var title: TextView? = null
        var readCount: TextView? = null
        init {
            date = itemView.findViewById(R.id.tv_item_notice_date)
            content = itemView.findViewById(R.id.tv_item_notice_content)
            toRegister = itemView.findViewById(R.id.tv_item_notice_to_register)
            tagAlert = itemView.findViewById(R.id.tv_item_notice_register_alert)
            tagNoRead = itemView.findViewById(R.id.tv_item_notice_no_read)
            tagRead = itemView.findViewById(R.id.tv_item_notice_read)
            title = itemView.findViewById(R.id.tv_item_notice_title)
            readCount = itemView.findViewById(R.id.tv_item_read_count)
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