package com.wanderlust.community_antiepidemic_system.activities.register

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.gson.Gson
import com.wanderlust.community_antiepidemic_system.R
import com.wanderlust.community_antiepidemic_system.WanderlustApp
import com.wanderlust.community_antiepidemic_system.activities.search.SearchCommunityActivity
import com.wanderlust.community_antiepidemic_system.entity.TemperReg
import com.wanderlust.community_antiepidemic_system.event.CommunityEvent
import com.wanderlust.community_antiepidemic_system.event.RegEvent
import com.wanderlust.community_antiepidemic_system.network.ApiService
import com.wanderlust.community_antiepidemic_system.utils.HealthType
import com.wanderlust.community_antiepidemic_system.utils.UrlUtils
import com.wanderlust.community_antiepidemic_system.utils.toast
import kotlinx.coroutines.*
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.StringBuilder
import java.net.ConnectException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.CoroutineContext

class TemperRegFragment : Fragment(), CoroutineScope {

    private lateinit var mEtTemper: EditText
    private lateinit var mCbState1: MaterialCheckBox
    private lateinit var mCbState2: MaterialCheckBox
    private lateinit var mCbState3: MaterialCheckBox
    private lateinit var mCbState4: MaterialCheckBox
    private lateinit var mCbState5: MaterialCheckBox
    private lateinit var mRgApproach: RadioGroup
    private lateinit var mRbApproach1: MaterialRadioButton
    private lateinit var mRbApproach2: MaterialRadioButton
    private lateinit var mRbApproach3: MaterialRadioButton
    private lateinit var mRgDiagnose: RadioGroup
    private lateinit var mRbDiagnose1: MaterialRadioButton
    private lateinit var mRbDiagnose2: MaterialRadioButton
    private lateinit var mRbDiagnose3: MaterialRadioButton
    private lateinit var mBtnSubmit: Button

    private val mTemperReg = TemperReg()

    private val mJob: Job by lazy { Job() }
    override val coroutineContext: CoroutineContext get() = mJob + Dispatchers.Main

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_temper_reg, container, false).apply {
            mEtTemper     = findViewById(R.id.et_temper)
            mCbState1     = findViewById(R.id.cb_temper_state_1)
            mCbState2     = findViewById(R.id.cb_temper_state_2)
            mCbState3     = findViewById(R.id.cb_temper_state_3)
            mCbState4     = findViewById(R.id.cb_temper_state_4)
            mCbState5     = findViewById(R.id.cb_temper_state_5)
            mRgApproach   = findViewById(R.id.rg_temper_approach)
            mRbApproach1  = findViewById(R.id.rb_temper_approach_1)
            mRbApproach2  = findViewById(R.id.rb_temper_approach_2)
            mRbApproach3  = findViewById(R.id.rb_temper_approach_3)
            mBtnSubmit    = findViewById(R.id.btn_temp_reg_submit)
            mRgDiagnose   = findViewById(R.id.rg_temper_diagnose)
            mRbDiagnose1  = findViewById(R.id.rb_temper_diagnose_1)
            mRbDiagnose2  = findViewById(R.id.rb_temper_diagnose_2)
            mRbDiagnose3  = findViewById(R.id.rb_temper_diagnose_3)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mRgApproach.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_temper_approach_1 -> mTemperReg.approach = HealthType.APPROACH_SUSPECT
                R.id.rb_temper_approach_2 -> mTemperReg.approach = HealthType.APPROACH_DIAGNOSE
                R.id.rb_temper_approach_3 -> mTemperReg.approach = HealthType.APPROACH_NONE
            }
        }
        mRgDiagnose.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_temper_diagnose_1 -> mTemperReg.diagnose = HealthType.DIAGNOSE
                R.id.rb_temper_diagnose_2 -> mTemperReg.diagnose = HealthType.SUSPECT
                R.id.rb_temper_diagnose_3 -> mTemperReg.diagnose = HealthType.NONE
            }
        }
        mBtnSubmit.setOnClickListener {
            submit()
        }
    }

    private fun submit() {
        mTemperReg.status = buildString {
            append(if (mCbState1.isChecked) "${mCbState1.text} " else "")
            append(if (mCbState2.isChecked) "${mCbState2.text} " else "")
            append(if (mCbState3.isChecked) "${mCbState3.text} " else "")
            append(if (mCbState4.isChecked) "${mCbState4.text} " else "")
            append(if (mCbState5.isChecked) "${mCbState5.text} " else "")
        }.trim()
        mTemperReg.temper = mEtTemper.text.toString()
        if (mTemperReg.temper.isEmpty() || mTemperReg.status.isEmpty() ||
            mTemperReg.approach == 0 || mTemperReg.diagnose == 0) {
            "请完整填写表单".toast(activity)
            return
        }
        try {
            if (mTemperReg.temper.toFloat() > 43f || mTemperReg.temper.toFloat() < 34f) {
                "体温输入范围为34.0°C~43.0°C".toast(activity)
                return
            }
        } catch (e: Exception) {
            "体温输入有误".toast(activity)
            return
        }
        mTemperReg.temper = mTemperReg.temper.toFloat().toString()
        mTemperReg.userId = (activity?.application as WanderlustApp).gUser?.userId ?: ""
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
        mTemperReg.date = format.format(System.currentTimeMillis())
        launch {
            val retrofit = Retrofit.Builder()
                .baseUrl(UrlUtils.SERVICE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
            val response = try {
                withContext(Dispatchers.IO) {
                    val request = RegEvent.AddTemperRecordReq(mTemperReg)
                    retrofit.addTemperReg(Gson().toJson(request).toRequestBody()).execute()
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
        const val TAG = "TemperRegFragment"
        @JvmStatic fun newInstance() = TemperRegFragment()
    }

}