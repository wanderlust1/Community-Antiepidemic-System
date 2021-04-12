package com.wanderlust.community_antiepidemic_system.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.*
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ViewPortHandler
import com.wanderlust.community_antiepidemic_system.R
import com.wanderlust.community_antiepidemic_system.entity.CommunityStatistics

class AdminStatisticView : ConstraintLayout {

    private var mContext: Context? = null

    private lateinit var mTvTotalCount: TextView
    private lateinit var mTvSubmitCount: TextView
    private lateinit var mTvNoSubmitCount: TextView
    private lateinit var mPieChart: PieChart
    private lateinit var mBarChart: HorizontalBarChart

    constructor(context: Context?) : super(context, null) {
        mContext = context
        initView()
    }

    constructor(context: Context?, attrs: AttributeSet?): super(context, attrs, 0) {
        mContext = context
        initView()
    }

    private fun initView() {
        LayoutInflater.from(mContext).inflate(R.layout.layout_admin_stats, this, true).apply {
            mTvTotalCount = findViewById(R.id.tv_admin_stat_item_1)
            mTvSubmitCount = findViewById(R.id.tv_admin_stat_item_2)
            mTvNoSubmitCount = findViewById(R.id.tv_admin_stat_item_3)
            mPieChart = findViewById(R.id.pie_admin_stat_health)
            mBarChart = findViewById(R.id.bar_admin_stat_outside)
        }
    }

    fun setData(data: CommunityStatistics) {
        //健康登记人数统计数字
        mTvTotalCount.text = "${data.total}"
        mTvSubmitCount.text = "${data.submit}"
        mTvNoSubmitCount.text = "${data.noneSubmit}"

        //饼状图
        val pieEntries = ArrayList<PieEntry>().apply {
            add(PieEntry(data.health.toFloat(), "健康"))
            add(PieEntry(data.highTemper.toFloat(), "体温异常"))
            add(PieEntry(data.diagnose.toFloat(), "确诊病例"))
            add(PieEntry(data.suspect.toFloat(), "疑似病例"))
            add(PieEntry(data.approach.toFloat(), "密切接触者"))
        }
        val pieColors = ArrayList<Int>().apply {
            add(mContext?.getColor(R.color.qr_code_green) ?: Color.GREEN)
            add(mContext?.getColor(R.color.qr_code_yellow) ?: Color.YELLOW)
            add(mContext?.getColor(R.color.qr_code_red) ?: Color.RED)
            add(mContext?.getColor(R.color.gray) ?: Color.GRAY)
            add(mContext?.getColor(R.color.yellow_middle) ?: Color.BLUE)
        }
        mPieChart.data = PieData(PieDataSet(pieEntries, null).apply {
            colors = pieColors
            sliceSpace = 3f
            xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
            yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
        }).apply {
            setValueTextSize(12f)
            setValueTextColor(Color.GRAY)
            setValueFormatter(PeopleCountFormatter())
        }
        mPieChart.legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        mPieChart.legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        mPieChart.legend.isWordWrapEnabled = true
        mPieChart.setDrawEntryLabels(false)
        mPieChart.isRotationEnabled = false
        mPieChart.description.text = ""
        mPieChart.invalidate()

        //柱状图
        val barEntries = ArrayList<BarEntry>().apply {
            add(BarEntry(1f, data.highRisk.toFloat()))
            add(BarEntry(2f, data.midRisk.toFloat()))
            add(BarEntry(3f, data.outside.toFloat()))
        }
        val barColors = ArrayList<Int>().apply {
            add(mContext?.getColor(R.color.qr_code_red) ?: Color.RED)
            add(mContext?.getColor(R.color.qr_code_yellow) ?: Color.YELLOW)
            add(mContext?.getColor(R.color.colorPrimary) ?: Color.BLUE)
        }
        val barLegendLabel = listOf("登记人数", "到达中风险地区", "到达高风险地区")
        //柱状图样式
        mBarChart.setDrawGridBackground(false)
        mBarChart.setDrawBorders(false)
        mBarChart.xAxis.isEnabled = false
        mBarChart.axisLeft.isEnabled = false
        mBarChart.axisRight.granularity = 0.5f
        mBarChart.axisRight.setDrawAxisLine(false)
        mBarChart.axisRight.valueFormatter = BarItemFormatter()
        mBarChart.axisRight.enableGridDashedLine(10f, 10f, 0f)
        mBarChart.description.text = ""
        mBarChart.isDragEnabled = false
        mBarChart.setPinchZoom(false)
        mBarChart.isScaleXEnabled = false
        mBarChart.isScaleYEnabled = false
        //图例
        mBarChart.legend.setCustom(listOf(LegendEntry().apply {
            label = barLegendLabel[0]
            formColor = barColors[2]
        }, LegendEntry().apply {
            label = barLegendLabel[1]
            formColor = barColors[1]
        }, LegendEntry().apply {
            label = barLegendLabel[2]
            formColor = barColors[0]
        }))
        mBarChart.extraBottomOffset = 15f
        mBarChart.legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        mBarChart.legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        mBarChart.legend.isWordWrapEnabled = true
        //柱状图数据
        val barData = BarData(BarDataSet(barEntries, null).apply {
            colors = barColors
        })
        mBarChart.data = barData
        mBarChart.invalidate()
    }

    class BarItemFormatter : ValueFormatter() {
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            return "${value.toInt()}人"
        }
    }

    class PeopleCountFormatter: ValueFormatter() {
        override fun getPieLabel(value: Float, pieEntry: PieEntry?): String {
            return "${value.toInt()}人"
        }
    }

}