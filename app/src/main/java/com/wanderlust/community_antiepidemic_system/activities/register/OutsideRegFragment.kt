package com.wanderlust.community_antiepidemic_system.activities.register

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bigkoo.pickerview.builder.OptionsPickerBuilder
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.bigkoo.pickerview.listener.OnOptionsSelectListener
import com.bigkoo.pickerview.listener.OnTimeSelectListener
import com.bigkoo.pickerview.view.OptionsPickerView
import com.bigkoo.pickerview.view.TimePickerView
import com.google.gson.Gson
import com.wanderlust.community_antiepidemic_system.R
import com.wanderlust.community_antiepidemic_system.WanderlustApp
import com.wanderlust.community_antiepidemic_system.entity.Country
import com.wanderlust.community_antiepidemic_system.entity.OutSideReg
import com.wanderlust.community_antiepidemic_system.event.RegEvent
import com.wanderlust.community_antiepidemic_system.network.ApiService
import com.wanderlust.community_antiepidemic_system.utils.UrlUtils
import com.wanderlust.community_antiepidemic_system.utils.toast
import kotlinx.coroutines.*
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ConnectException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.CoroutineContext

class OutsideRegFragment : Fragment(), CoroutineScope {

    private lateinit var mTvDestination: TextView
    private lateinit var mTvStartTime: TextView
    private lateinit var mTvEndTime: TextView
    private lateinit var mTvReason: TextView
    private lateinit var mEtPhone: EditText
    private lateinit var mBtnSubmit: Button

    private val mArea1 = mutableListOf<String>()
    private val mArea2 = mutableListOf<MutableList<String>>()
    private val mArea3 = mutableListOf<MutableList<MutableList<String>>>()

    private val START = 0
    private val END   = 1
    private var mSelected = START

    private val mOutsideReg = OutSideReg()

    private val mJob: Job by lazy { Job() }
    override val coroutineContext: CoroutineContext get() = mJob + Dispatchers.Main

    private val mTimePicker: TimePickerView by lazy {
        TimePickerBuilder(activity, OnTimeSelectListener { date, _ ->
            val dateString = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(date)
            when (mSelected) {
                START ->  if (mOutsideReg.endTime.isEmpty() || judgeTime(dateString, mOutsideReg.endTime)) {
                    mTvStartTime.text = dateString
                    mOutsideReg.startTime = dateString
                    mTvStartTime.setTextColor(Color.BLACK)
                }
                END -> if (mOutsideReg.startTime.isEmpty() || judgeTime(mOutsideReg.startTime, dateString)) {
                    mTvEndTime.text = dateString
                    mOutsideReg.endTime = dateString
                    mTvEndTime.setTextColor(Color.BLACK)
                }
            }
        }).setLayoutRes(R.layout.dialog_time_picker) {
            it.findViewById<TextView>(R.id.tv_timepick_ok).setOnClickListener {
                mTimePicker.returnData()
                mTimePicker.dismiss()
            }
            it.findViewById<TextView>(R.id.tv_timepick_cancel).setOnClickListener {
                mTimePicker.dismiss()
            }
            it.findViewById<RelativeLayout>(R.id.ll_timepick_head).setOnClickListener {}
        }.build()
    }

    private val mAreaPicker: OptionsPickerView<String> by lazy {
        val optionsPicker = OptionsPickerBuilder(activity, OnOptionsSelectListener { options1, options2, options3, _ ->
            val opt1tx = if (mArea1.size > 0) mArea1[options1] else ""
            val opt2tx = if (mArea2.size > 0
                && mArea2[options1].size > 0) mArea2[options1][options2] else ""
            val opt3tx = if (mArea2.size > 0
                && mArea3[options1].size > 0
                && mArea3[options1][options2].size > 0) mArea3[options1][options2][options3] else ""
            val areaString = "$opt1tx $opt2tx $opt3tx"
            mTvDestination.text = areaString
            mOutsideReg.city = areaString
            mTvDestination.setTextColor(Color.BLACK)
        }).setLayoutRes(R.layout.dialog_area_picker) {
            it.findViewById<TextView>(R.id.tv_areapick_ok).setOnClickListener {
                mAreaPicker.returnData()
                mAreaPicker.dismiss()
            }
            it.findViewById<TextView>(R.id.tv_areapick_cancel).setOnClickListener {
                mAreaPicker.dismiss()
            }
            it.findViewById<RelativeLayout>(R.id.ll_areapick_head).setOnClickListener {}
        }.build<String>()
        optionsPicker.setPicker(mArea1, mArea2, mArea3)
        optionsPicker
    }

    private val mReasonPicker: OptionsPickerView<String> by lazy {
        val optionsPicker = OptionsPickerBuilder(activity, OnOptionsSelectListener { options1, _, _, _ ->
            mTvReason.text = REASON[options1]
            mOutsideReg.reason = REASON[options1]
            mTvReason.setTextColor(Color.BLACK)
        }).setLayoutRes(R.layout.dialog_reason_picker) {
            it.findViewById<TextView>(R.id.tv_reasonpick_ok).setOnClickListener {
                mReasonPicker.returnData()
                mReasonPicker.dismiss()
            }
            it.findViewById<TextView>(R.id.tv_reasonpick_cancel).setOnClickListener {
                mReasonPicker.dismiss()
            }
            it.findViewById<RelativeLayout>(R.id.ll_reasonpick_head).setOnClickListener {}
        }.build<String>()
        optionsPicker.setPicker(REASON)
        optionsPicker
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_outside_reg, container, false).apply {
            mTvDestination = findViewById(R.id.tv_outside_reg_position)
            mTvStartTime = findViewById(R.id.tv_outside_reg_start_time)
            mTvEndTime = findViewById(R.id.tv_outside_reg_end_time)
            mTvReason = findViewById(R.id.tv_outside_reg_reason)
            mEtPhone = findViewById(R.id.tv_outside_reg_phone)
            mBtnSubmit = findViewById(R.id.btn_outside_reg_submit)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mTvStartTime.setOnClickListener {
            mSelected = START
            mTimePicker.show()
        }
        mTvEndTime.setOnClickListener {
            mSelected = END
            mTimePicker.show()
        }
        mTvDestination.setOnClickListener {
            mAreaPicker.show()
        }
        mTvReason.setOnClickListener {
            mReasonPicker.show()
        }
        mBtnSubmit.setOnClickListener {
            submit()
        }
        initAreaPickerData()
    }

    private fun judgeTime(start: String, end: String): Boolean {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
        val startDate = format.parse(start)
        val endDate = format.parse(end)
        if (startDate?.after(endDate) == true) {
            "出发时间不能晚于返回时间".toast(activity)
            return false
        }
        return true
    }

    private fun initAreaPickerData() = launch {
        val country = withContext(Dispatchers.IO) {
            //io流载入城市列表，并传入country
            Gson().fromJson(buildString {
                BufferedReader(InputStreamReader(context?.assets?.open(FILE))).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        append(line)
                    }
                }
            }, Country::class.java)
        }
        country.province.forEach {
            mArea1.add(it.name)
        }
        for (province in country.province) {  //遍历省份
            val cities = mutableListOf<String>()
            val areas = mutableListOf<MutableList<String>>()
            for (city in province.city) {     //遍历该省份的所有城市
                cities.add(city.name) //添加城市
                val areasCity = ArrayList<String>() //该城市的所有地区列表
                if (city.area.isEmpty()) {
                    areasCity.add("")
                } else {
                    areasCity.addAll(city.area)
                }
                areas.add(areasCity) //添加该省所有地区数据
            }
            mArea2.add(cities)
            mArea3.add(areas)
        }
    }

    private fun submit() {
        mOutsideReg.phone = mEtPhone.text.toString()
        if (mOutsideReg.startTime.isEmpty() || mOutsideReg.endTime.isEmpty()
            || mOutsideReg.reason.isEmpty() || mOutsideReg.city.isEmpty() || mOutsideReg.phone.isEmpty()) {
            "请完整填写表单".toast(activity)
            return
        }
        mOutsideReg.userId = (activity?.application as WanderlustApp).gUser?.userId ?: ""
        val format = SimpleDateFormat("yyyy-MM-dd kk:mm:ss", Locale.CHINA)
        mOutsideReg.date = format.format(System.currentTimeMillis())
        launch {
            val retrofit = Retrofit.Builder()
                .baseUrl(UrlUtils.SERVICE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
            val response = try {
                withContext(Dispatchers.IO) {
                    val request = RegEvent.AddOutsideRecordReq(mOutsideReg)
                    retrofit.addOutsideReg(Gson().toJson(request).toRequestBody()).execute()
                }
            } catch (e: ConnectException) {
                R.string.connection_error.toast(activity)
                null
            } catch (e: Exception) {
                e.printStackTrace()
                R.string.timeout_error.toast(activity)
                null
            }
            Log.d(TAG, response?.body().toString())
            if (response?.body() == null) return@launch
            val result = response.body()!!
            result.msg.toast(activity)
            if (result.code == RegEvent.SUCC) {
                mBtnSubmit.background = activity?.getDrawable(R.drawable.button_disable)
                mBtnSubmit.setTextColor(Color.parseColor("#787878"))
                mBtnSubmit.text = "已提交"
                mBtnSubmit.isClickable = false
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mJob.cancel()
    }

    companion object {
        val REASON = mutableListOf("工作", "旅游", "探亲", "出差", "办事", "就医", "就学", "科研", "其他")
        const val FILE = "province.json"
        const val TAG = "OutsideRegFragment"
        @JvmStatic fun newInstance() = OutsideRegFragment()
    }

}