package com.wanderlust.community_antiepidemic_system.activities.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.wanderlust.community_antiepidemic_system.network.ApiService
import com.wanderlust.community_antiepidemic_system.utils.LoginType
import com.wanderlust.community_antiepidemic_system.R
import com.wanderlust.community_antiepidemic_system.entity.Admin
import com.wanderlust.community_antiepidemic_system.entity.User
import com.wanderlust.community_antiepidemic_system.event.UserEvent
import com.wanderlust.community_antiepidemic_system.utils.DialogUtils
import com.wanderlust.community_antiepidemic_system.utils.UrlUtils
import com.wanderlust.community_antiepidemic_system.utils.toast
import kotlinx.coroutines.*
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Exception
import java.net.ConnectException
import kotlin.coroutines.CoroutineContext

class SignUpActivity : AppCompatActivity(), CoroutineScope {

    private lateinit var mTilUsername: TextInputLayout
    private lateinit var mTilPassword: TextInputLayout
    private lateinit var mEtUser: TextInputEditText
    private lateinit var mEtPassword: TextInputEditText
    private lateinit var mBtnSignUp: Button
    private lateinit var mRadioGroup: RadioGroup

    private var mSelectedType = LoginType.USER //已选择的注册类型

    /** 登录进度条  */
    private val mDialog: DialogUtils by lazy { DialogUtils(this) }

    //协程
    private val mJob: Job by lazy { Job() }
    override val coroutineContext: CoroutineContext get() = mJob + Dispatchers.Main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        initView()
    }

    private fun initView() {
        mEtUser  = findViewById(R.id.et_new_user)
        mEtPassword =  findViewById(R.id.et_new_password)
        mBtnSignUp  = findViewById(R.id.btn_sign_up)
        mTilUsername  = findViewById(R.id.til_new_username)
        mTilPassword  = findViewById(R.id.til_new_password)
        mRadioGroup = findViewById(R.id.rg_sign_up_type)
        mBtnSignUp.setOnClickListener {
            onSubmit()
        }
        mRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_sign_up_user -> {
                    mSelectedType = LoginType.USER
                    mTilUsername.hint = "新用户账号"
                    mTilPassword.hint = "密码"
                }
                R.id.rb_sign_up_admin -> {
                    mSelectedType = LoginType.ADMIN
                    mTilUsername.hint = "新社区管理员账号"
                    mTilPassword.hint = "密码"
                }
            }
        }
        mEtUser.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (mEtUser.text.isNullOrBlank()) {
                    mTilUsername.error = "账号不能为空"
                } else {
                    mTilUsername.isErrorEnabled = false
                }
            }
        })
        mEtPassword.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (mEtPassword.text.isNullOrBlank()) {
                    mTilPassword.error = "账号不能为空"
                } else {
                    mTilPassword.isErrorEnabled = false
                }
            }
        })
        mSelectedType = LoginType.USER
        mTilUsername.hint = "用户账号"
        mTilPassword.hint = "密码"
    }

    private fun onSubmit() {
        val id = mEtUser.text.toString().trim()
        val password = mEtPassword.text.toString().trim()
        if (id.isNotEmpty() && password.isNotEmpty()) {
            mDialog.show()
            mBtnSignUp.isClickable = false
            //对这个账号密码进行查询
            requestRegister(id, password)
        } else {
            mTilUsername.error = "账号不能为空"
            mTilUsername.isErrorEnabled = id.isEmpty()
            mTilPassword.error = "密码不能为空"
            mTilPassword.isErrorEnabled = password.isEmpty()
        }
    }

    private fun requestRegister(id: String, password: String) {
        launch {
            val type = mSelectedType
            val retrofit = Retrofit.Builder()
                .baseUrl(UrlUtils.SERVICE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
            val response = try {
                withContext(Dispatchers.IO) {
                    val request = if (type == LoginType.USER) {
                        val user = User(userId = id, password = password)
                        UserEvent.RegisterReq(user = user, loginType = type)
                    } else {
                        val admin = Admin(adminId = id, password = password)
                        UserEvent.RegisterReq(admin = admin, loginType = type)
                    }
                    retrofit.register(Gson().toJson(request).toRequestBody()).execute()
                }
            } catch (e: ConnectException) {
                R.string.connection_error.toast(this@SignUpActivity)
                null
            } catch (e: Exception) {
                R.string.other_error.toast(this@SignUpActivity)
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

    override fun onDestroy() {
        super.onDestroy()
        mJob.cancel()
    }

}