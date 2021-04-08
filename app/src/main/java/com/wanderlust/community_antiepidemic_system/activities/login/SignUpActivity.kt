package com.wanderlust.community_antiepidemic_system.activities.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.wanderlust.community_antiepidemic_system.network.ApiService
import com.wanderlust.community_antiepidemic_system.R
import com.wanderlust.community_antiepidemic_system.activities.BaseActivity
import com.wanderlust.community_antiepidemic_system.entity.Admin
import com.wanderlust.community_antiepidemic_system.entity.User
import com.wanderlust.community_antiepidemic_system.event.UserEvent
import com.wanderlust.community_antiepidemic_system.network.Service
import com.wanderlust.community_antiepidemic_system.utils.*
import kotlinx.coroutines.*
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Exception
import java.net.ConnectException
import kotlin.coroutines.CoroutineContext

class SignUpActivity : BaseActivity() {

    private lateinit var mTilUsername: TextInputLayout
    private lateinit var mEtUser: TextInputEditText
    private lateinit var mTilPassword: TextInputLayout
    private lateinit var mEtPassword: TextInputEditText
    private lateinit var mTilTrueName: TextInputLayout
    private lateinit var mEtTrueName: TextInputEditText
    private lateinit var mTilCID: TextInputLayout
    private lateinit var mEtCID: TextInputEditText
    private lateinit var mTilPhone: TextInputLayout
    private lateinit var mEtPhone: TextInputEditText
    private lateinit var mBtnSignUp: Button
    private lateinit var mRadioGroup: RadioGroup

    private var mSelectedType = LoginType.USER //已选择的注册类型

    /** 登录进度条  */
    private val mDialog: DialogUtils by lazy { DialogUtils(this) }

    override fun contentView() = R.layout.activity_sign_up

    override fun findView() {
        mEtUser  = findViewById(R.id.et_new_user)
        mEtPassword =  findViewById(R.id.et_new_password)
        mBtnSignUp  = findViewById(R.id.btn_sign_up)
        mTilUsername  = findViewById(R.id.til_new_username)
        mTilPassword  = findViewById(R.id.til_new_password)
        mTilTrueName  = findViewById(R.id.til_new_true_name)
        mTilCID  = findViewById(R.id.til_new_cid)
        mEtTrueName  = findViewById(R.id.et_new_true_name)
        mTilPhone  = findViewById(R.id.til_new_phone)
        mEtPhone  = findViewById(R.id.et_new_phone)
        mEtCID  = findViewById(R.id.et_new_cid)
        mRadioGroup = findViewById(R.id.rg_sign_up_type)
    }

    override fun initView() {
        mBtnSignUp.setOnClickListener {
            onSubmit()
        }
        mRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_sign_up_user -> {
                    mSelectedType = LoginType.USER
                    mTilUsername.hint = "新用户账号"
                    mTilPassword.hint = "密码"
                    mTilTrueName.visibility = View.VISIBLE
                    mTilCID.visibility = View.VISIBLE
                    mTilPhone.visibility = View.VISIBLE
                }
                R.id.rb_sign_up_admin -> {
                    mSelectedType = LoginType.ADMIN
                    mTilUsername.hint = "新社区管理员账号"
                    mTilPassword.hint = "密码"
                    mTilTrueName.visibility = View.GONE
                    mTilCID.visibility = View.GONE
                    mTilPhone.visibility = View.GONE
                }
            }
        }
        mEtUser.addErrorTextWatcher(mTilUsername, "账号不能为空")
        mEtPassword.addErrorTextWatcher(mTilPassword, "密码不能为空")
        mEtCID.addErrorTextWatcher(mTilCID, "身份证号码不能为空")
        mEtTrueName.addErrorTextWatcher(mTilTrueName, "真实姓名不能为空")
        mEtPhone.addErrorTextWatcher(mTilPhone, "手机号码不能为空")
        mSelectedType = LoginType.USER
        mTilUsername.hint = "用户账号"
        mTilPassword.hint = "密码"
        mTilTrueName.hint = "真实姓名"
        mTilCID.hint = "身份证号码"
        mTilPhone.hint = "手机号码"
    }

    private fun onSubmit() {
        val id = mEtUser.text.toString().trim()
        val password = mEtPassword.text.toString().trim()
        val cid = mEtCID.text.toString().trim()
        val trueName = mEtTrueName.text.toString().trim()
        val phone = mEtPhone.text.toString().trim()
        if (id.isNotEmpty() && password.isNotEmpty()) {
            if (mSelectedType == LoginType.ADMIN || (mSelectedType == LoginType.USER
                        && cid.isNotEmpty() && trueName.isNotEmpty() && phone.isNotEmpty())) {
                mDialog.show()
                mBtnSignUp.isClickable = false
                //对这个账号密码进行查询
                requestRegister(id, password, cid, trueName, phone)
            }
        }
        mTilUsername.error = "账号不能为空"
        mTilUsername.isErrorEnabled = id.isEmpty()
        mTilPassword.error = "密码不能为空"
        mTilPassword.isErrorEnabled = password.isEmpty()
        mTilCID.error = "身份证号码不能为空"
        mTilCID.isErrorEnabled = cid.isEmpty()
        mTilTrueName.error = "真实姓名不能为空"
        mTilTrueName.isErrorEnabled = trueName.isEmpty()
        mTilPhone.error = "手机号码不能为空"
        mTilPhone.isErrorEnabled = phone.isEmpty()
    }

    private fun requestRegister(id: String, password: String,
                                cid: String = "", trueName: String = "", phone: String = "") {
        launch {
            val type = mSelectedType
            val response = try {
                withContext(Dispatchers.IO) {
                    val request = if (type == LoginType.USER) {
                        val user = User(id, password, trueName, cid, phone)
                        UserEvent.RegisterReq(user = user, loginType = type)
                    } else {
                        val admin = Admin(id, password)
                        UserEvent.RegisterReq(admin = admin, loginType = type)
                    }
                    Service.request.register(Gson().toJson(request).toRequestBody()).execute()
                }
            } catch (e: ConnectException) {
                R.string.connection_error.toast(this@SignUpActivity)
                null
            } catch (e: Exception) {
                R.string.timeout_error.toast(this@SignUpActivity)
                null
            }
            Log.d(LoginActivity.TAG, "onResponse: " + response?.body())
            mDialog.dismiss()
            mBtnSignUp.isClickable = true
            if (response?.body() == null) return@launch
            //处理结果
            val result = response.body()!!
            result.msg.toast(this@SignUpActivity)
            if (result.code == UserEvent.SUCC) {
                val intent = Intent()
                intent.putExtra("u", id)
                intent.putExtra("p", password)
                intent.putExtra("s", type)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }
    }

}