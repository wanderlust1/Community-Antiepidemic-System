package com.wanderlust.community_antiepidemic_system.activities.login

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
import com.wanderlust.community_antiepidemic_system.WanderlustApp
import com.wanderlust.community_antiepidemic_system.entity.Admin
import com.wanderlust.community_antiepidemic_system.entity.User
import com.wanderlust.community_antiepidemic_system.event.UserEvent
import com.wanderlust.community_antiepidemic_system.activities.home.UserHomeActivity
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

class LoginActivity : AppCompatActivity(), CoroutineScope {

    companion object {
        const val TAG = "LoginActivity"
    }

    private lateinit var mTilUsername: TextInputLayout
    private lateinit var mTilPassword: TextInputLayout
    private lateinit var mTieUser: TextInputEditText
    private lateinit var mTiePassword: TextInputEditText
    private lateinit var mBtnSubmit: Button
    private lateinit var mTvSignUp: TextView
    private lateinit var mRadioGroup: RadioGroup
    private lateinit var mRbUser: RadioButton
    private lateinit var mRbAdmin: RadioButton

    // 已选择的登录类型、账号密码
    private var mSelectedType = LoginType.USER

    private val mDialog: DialogUtils by lazy { DialogUtils(this) }

    //协程
    private val mJob: Job by lazy { Job() }
    override val coroutineContext: CoroutineContext get() = mJob + Dispatchers.Main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initView()
    }
    
    private fun initView() {
        mTilUsername = findViewById(R.id.til_username)
        mTilPassword = findViewById(R.id.til_password)
        mTieUser = findViewById(R.id.et_user)
        mTiePassword = findViewById(R.id.et_password)
        mBtnSubmit = findViewById(R.id.btn_submit)
        mTvSignUp = findViewById(R.id.tv_sign_up)
        mRadioGroup = findViewById(R.id.rg_login_type)
        mRbUser = findViewById(R.id.rb_login_user)
        mRbAdmin = findViewById(R.id.rb_login_admin)
        mBtnSubmit.setOnClickListener {
            onSubmit()
        }
        mTvSignUp.setOnClickListener {
            startActivityForResult(Intent(this@LoginActivity, SignUpActivity::class.java), 1)
        }
        mRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_login_user -> {
                    mSelectedType = LoginType.USER
                    mTilUsername.hint = "用户账号"
                    mTilPassword.hint = "密码"
                }
                R.id.rb_login_admin -> {
                    mSelectedType = LoginType.ADMIN
                    mTilUsername.hint = "社区管理员账号"
                    mTilPassword.hint = "密码"
                }
            }
        }
        mTieUser.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (mTieUser.text.isNullOrBlank()) {
                    mTilUsername.error = "账号不能为空"
                } else {
                    mTilUsername.isErrorEnabled = false
                }
            }
        })
        mTiePassword.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (mTiePassword.text.isNullOrBlank()) {
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
        val id = mTieUser.text.toString().trim()
        val password = mTiePassword.text.toString().trim()
        if (id.isNotEmpty() && password.isNotEmpty()) {
            mDialog.show()
            mBtnSubmit.isClickable = false
            //对这个账号密码进行查询
            requestLogin(id, password)
        } else {
            mTilUsername.error = "账号不能为空"
            mTilUsername.isErrorEnabled = id.isEmpty()
            mTilPassword.error = "密码不能为空"
            mTilPassword.isErrorEnabled = password.isEmpty()
        }
    }

    private fun requestLogin(id: String, password: String) {
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
                        UserEvent.LoginReq(user = user, loginType = type)
                    } else {
                        val admin = Admin(adminId = id, password = password)
                        UserEvent.LoginReq(admin = admin, loginType = type)
                    }
                    retrofit.login(Gson().toJson(request).toRequestBody()).execute()
                }
            } catch (e: ConnectException) {
                R.string.connection_error.toast(this@LoginActivity)
                null
            } catch (e: Exception) {
                R.string.timeout_error.toast(this@LoginActivity)
                null
            }
            Log.d(TAG, "onResponse: " + response?.body())
            mDialog.dismiss()
            mBtnSubmit.isClickable = true
            if (response?.body() == null) return@launch
            //处理结果
            val result = response.body()!!
            result.msg.toast(this@LoginActivity)
            if (result.code == UserEvent.SUCC) {
                val app = application as WanderlustApp
                if (type == LoginType.USER) {
                    app.gUser = result.user
                } else if (type == LoginType.USER)  {
                    app.gAdmin = result.admin
                }
                app.gType = type
                startActivity(Intent(this@LoginActivity, UserHomeActivity::class.java))
                finish()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                mTieUser.setText(if (data != null) data.getStringExtra("u") else "?")
                mTiePassword.setText(if (data != null) data.getStringExtra("p") else "?")
                val selection = data?.getIntExtra("s", LoginType.USER) ?: LoginType.USER
                if (selection == LoginType.USER) {
                    mRbUser.isChecked = true
                } else {
                    mRbAdmin.isChecked = true
                }
                mTilUsername.isErrorEnabled = false
                mTilPassword.isErrorEnabled = false
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mJob.cancel()
    }

}