package com.wanderlust.community_antiepidemic_system.activities.community

import android.graphics.Color
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.wanderlust.community_antiepidemic_system.R
import com.wanderlust.community_antiepidemic_system.WanderlustApp
import com.wanderlust.community_antiepidemic_system.activities.BaseActivity
import com.wanderlust.community_antiepidemic_system.activities.register.OutsideRegFragment
import com.wanderlust.community_antiepidemic_system.entity.Community
import com.wanderlust.community_antiepidemic_system.event.BusEvent
import com.wanderlust.community_antiepidemic_system.event.CommunityEvent
import com.wanderlust.community_antiepidemic_system.event.RegEvent
import com.wanderlust.community_antiepidemic_system.network.ServiceManager
import com.wanderlust.community_antiepidemic_system.utils.addErrorTextWatcher
import com.wanderlust.community_antiepidemic_system.utils.toJsonRequest
import com.wanderlust.community_antiepidemic_system.utils.toast
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

class CreateCommunityActivity : BaseActivity() {

    companion object {
        const val TAG = "CreateCommunityActivity"
    }

    private val mAreaDialog by lazy {
        PoiAreaDialog(this).apply {
            setOnSelectListener {
                mTieAddress.setText(it.address)
            }
        }
    }

    private lateinit var mTilName: TextInputLayout
    private lateinit var mTilAddress: TextInputLayout
    private lateinit var mTilPhone: TextInputLayout
    private lateinit var mTieName: TextInputEditText
    private lateinit var mTieAddress: TextInputEditText
    private lateinit var mTiePhone: TextInputEditText
    private lateinit var mBtnSubmit: Button

    override fun contentView() = R.layout.activity_create_community

    override fun findView() {
        mTilName = findViewById(R.id.til_community_name)
        mTilAddress = findViewById(R.id.til_community_address)
        mTilPhone = findViewById(R.id.til_community_phone)
        mTieName = findViewById(R.id.et_community_name)
        mTieAddress = findViewById(R.id.et_community_address)
        mTiePhone = findViewById(R.id.et_community_phone)
        mBtnSubmit = findViewById(R.id.btn_create_submit)
    }

    override fun initView() {
        findViewById<ImageView>(R.id.iv_create_back).setOnClickListener {
            finish()
        }
        mTieName.addErrorTextWatcher(mTilName, "社区名称不能为空")
        mTieAddress.addErrorTextWatcher(mTilAddress, "社区地址不能为空")
        mTiePhone.addErrorTextWatcher(mTilPhone, "社区联系方式不能为空")
        mBtnSubmit.setOnClickListener {
            onSubmit()
        }
        mTieAddress.isFocusable = false
        mTieAddress.setOnClickListener {
            mAreaDialog.show()
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
            val result = ServiceManager.request {
                val request = CommunityEvent.CreateCommunityReq(community, adminId)
                it.createCommunity(request.toJsonRequest())
            }
            mBtnSubmit.isClickable = true
            Log.d(OutsideRegFragment.TAG, result.toString())
            if (result == null) return@launch
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