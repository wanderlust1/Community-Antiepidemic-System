package com.wanderlust.community_antiepidemic_system.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.wanderlust.community_antiepidemic_system.R
import com.wanderlust.community_antiepidemic_system.event.Statistics

class DiseaseStatsView: LinearLayout {

    private var mContext: Context? = null

    private lateinit var mTvNowConfirmed: TextView
    private lateinit var mTvNowConfirmedHint: TextView
    private lateinit var mTvTotalCured: TextView
    private lateinit var mTvTotalCuredHint: TextView
    private lateinit var mTvTotalDeath: TextView
    private lateinit var mTvTotalDeathHint: TextView
    private lateinit var mTvTotalDoubtful: TextView
    private lateinit var mTvTotalDoubtfulHint: TextView

    private var mData: Statistics? = null

    constructor(context: Context?) : super(context, null) {
        mContext = context
        initView()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs, 0) {
        mContext = context
        initView()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr, 0) {
        mContext = context
        initView()
    }

    private fun initView() {
        val view = LayoutInflater.from(mContext).inflate(R.layout.layout_disease_stats, this, true)
        mTvNowConfirmed = view.findViewById(R.id.tv_lds_now_confirmed)
        mTvNowConfirmedHint = view.findViewById(R.id.tv_lds_now_confirmed_hint)
        mTvTotalCured = view.findViewById(R.id.tv_lds_total_cured)
        mTvTotalCuredHint = view.findViewById(R.id.tv_lds_total_cured_hint)
        mTvTotalDeath = view.findViewById(R.id.tv_lds_total_death)
        mTvTotalDeathHint = view.findViewById(R.id.tv_lds_total_death_hint)
        mTvTotalDoubtful = view.findViewById(R.id.tv_lds_total_doubtful)
        mTvTotalDoubtfulHint = view.findViewById(R.id.tv_lds_total_doubtful_hint)
    }

    @SuppressLint("SetTextI18n")
    fun setData(data: Statistics?) {
        if (data == null || mData != null) {
            if (mData == null) visibility = View.GONE
            return
        }
        mData = data
        visibility = View.VISIBLE
        val area = data.childStatistic.replace("省", "").replace("市", "")
        val totalConfirmed = if (data.totalConfirmed.isBlank()) "0" else data.totalConfirmed
        val totalCured = if (data.totalCured.isBlank()) "0" else data.totalCured
        val totalDeath = if (data.totalDeath.isBlank()) "0" else data.totalDeath
        val totalDoubtful = if (data.totalDoubtful.isBlank()) "0" else data.totalDoubtful
        val nowConfirm = totalConfirmed.toInt() - totalCured.toInt() - totalDeath.toInt()
        mTvNowConfirmed.text  = nowConfirm.toString()
        mTvTotalCured.text    = totalCured
        mTvTotalDeath.text    = totalDeath
        mTvTotalDoubtful.text = totalDoubtful
        mTvNowConfirmedHint.text  = "${area}现存确诊"
        mTvTotalCuredHint.text    = "${area}累计治愈"
        mTvTotalDeathHint.text    = "${area}累计死亡"
        mTvTotalDoubtfulHint.text = "${area}疑似病例"
    }

}