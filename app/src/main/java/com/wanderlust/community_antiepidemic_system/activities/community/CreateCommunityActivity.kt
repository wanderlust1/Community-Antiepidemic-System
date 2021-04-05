package com.wanderlust.community_antiepidemic_system.activities.community

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.wanderlust.community_antiepidemic_system.R
import com.wanderlust.community_antiepidemic_system.WanderlustApp
import com.wanderlust.community_antiepidemic_system.activities.register.OutsideRegFragment
import com.wanderlust.community_antiepidemic_system.entity.Admin
import com.wanderlust.community_antiepidemic_system.entity.Community
import com.wanderlust.community_antiepidemic_system.event.BusEvent
import com.wanderlust.community_antiepidemic_system.event.CommunityEvent
import com.wanderlust.community_antiepidemic_system.event.RegEvent
import com.wanderlust.community_antiepidemic_system.network.ApiService
import com.wanderlust.community_antiepidemic_system.utils.UrlUtils
import com.wanderlust.community_antiepidemic_system.utils.toast
import kotlinx.coroutines.*
import okhttp3.RequestBody.Companion.toRequestBody
import org.greenrobot.eventbus.EventBus
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.ConnectException
import kotlin.coroutines.CoroutineContext

class CreateCommunityActivity : AppCompatActivity(), CoroutineScope {

    companion object {
        const val TAG = "CreateCommunityActivity"
    }

    private lateinit var mTilName: TextInputLayout
    private lateinit var mTilAddress: TextInputLayout
    private lateinit var mTilPhone: TextInputLayout
    private lateinit var mTieName: TextInputEditText
    private lateinit var mTieAddress: TextInputEditText
    private lateinit var mTiePhone: TextInputEditText
    private lateinit var mBtnSubmit: Button

    private val mJob: Job by lazy { Job() }
    override val coroutineContext: CoroutineContext get() = mJob + Dispatchers.Main

    private val mAdmin: Admin? by lazy {
        (application as WanderlustApp).gAdmin
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_community)
        findViewById<ImageView>(R.id.iv_create_back).setOnClickListener {
            finish()
        }
        mTilName = findViewById(R.id.til_community_name)
        mTilAddress = findViewById(R.id.til_community_address)
        mTilPhone = findViewById(R.id.til_community_phone)
        mTieName = findViewById(R.id.et_community_name)
        mTieAddress = findViewById(R.id.et_community_address)
        mTiePhone = findViewById(R.id.et_community_phone)
        mBtnSubmit = findViewById(R.id.btn_create_submit)
        mTieName.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (mTieName.text.isNullOrBlank()) {
                    mTilName.error = "社区名称不能为空"
                } else {
                    mTilName.isErrorEnabled = false
                }
            }
        })
        mTieAddress.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (mTieAddress.text.isNullOrBlank()) {
                    mTilAddress.error = "社区地址不能为空"
                } else {
                    mTilAddress.isErrorEnabled = false
                }
            }
        })
        mTiePhone.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (mTiePhone.text.isNullOrBlank()) {
                    mTilPhone.error = "社区联系方式不能为空"
                } else {
                    mTilPhone.isErrorEnabled = false
                }
            }
        })
        mBtnSubmit.setOnClickListener {
            onSubmit()
        }
    }

    private fun onSubmit() {
        val name = mTieName.text.toString().trim()
        val address = mTieAddress.text.toString().trim()
        val phone = mTiePhone.text.toString().trim()
        if (name.isNotEmpty() && address.isNotEmpty() && phone.isNotEmpty()) {
            mBtnSubmit.isClickable = false
            //对这个账号密码进行查询
            requestCreate(Community(name = name, location = address, phone = phone))
        } else {
            mTilName.error = "社区名称不能为空"
            mTilName.isErrorEnabled = name.isEmpty()
            mTilAddress.error = "社区地址不能为空"
            mTilAddress.isErrorEnabled = address.isEmpty()
            mTilPhone.error = "社区联系方式不能为空"
            mTilPhone.isErrorEnabled = phone.isEmpty()
        }
    }

    private fun requestCreate(community: Community) {
        val adminId = (application as WanderlustApp).gAdmin?.adminId ?: return
        launch {
            val retrofit = Retrofit.Builder()
                .baseUrl(UrlUtils.SERVICE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
            val response = try {
                withContext(Dispatchers.IO) {
                    val request = CommunityEvent.CreateCommunityReq(community, adminId)
                    retrofit.createCommunity(Gson().toJson(request).toRequestBody()).execute()
                }
            } catch (e: ConnectException) {
                R.string.connection_error.toast(this@CreateCommunityActivity)
                null
            } catch (e: Exception) {
                e.printStackTrace()
                R.string.timeout_error.toast(this@CreateCommunityActivity)
                null
            }
            mBtnSubmit.isClickable = true
            Log.d(OutsideRegFragment.TAG, response?.body().toString())
            if (response?.body() == null) return@launch
            val result = response.body()!!
            result.msg.toast(this@CreateCommunityActivity)
            if (result.code == RegEvent.SUCC) {
                mBtnSubmit.background = getDrawable(R.drawable.button_disable)
                mBtnSubmit.setTextColor(Color.parseColor("#787878"))
                mBtnSubmit.text = "已创建"
                mBtnSubmit.isClickable = false
                mAdmin?.communityId = result.communityId
                mAdmin?.communityName = community.name
                EventBus.getDefault().post(BusEvent.OnAdminCommunityChange()) //通知home
            }
        }
    }

}