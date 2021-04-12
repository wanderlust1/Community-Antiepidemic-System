package com.wanderlust.community_antiepidemic_system.activities.login

import android.content.Intent
import android.util.Log
import android.widget.*
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.wanderlust.community_antiepidemic_system.R
import com.wanderlust.community_antiepidemic_system.WanderlustApp
import com.wanderlust.community_antiepidemic_system.activities.BaseActivity
import com.wanderlust.community_antiepidemic_system.activities.home_admin.AdminHomeActivity
import com.wanderlust.community_antiepidemic_system.entity.Admin
import com.wanderlust.community_antiepidemic_system.entity.User
import com.wanderlust.community_antiepidemic_system.event.UserEvent
import com.wanderlust.community_antiepidemic_system.activities.home_user.UserHomeActivity
import com.wanderlust.community_antiepidemic_system.network.ServiceManager
import com.wanderlust.community_antiepidemic_system.utils.*
import kotlinx.coroutines.*
import okhttp3.RequestBody.Companion.toRequestBody
import java.lang.Exception
import java.net.ConnectException

class LoginActivity : BaseActivity() {

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

    override fun contentView() = R.layout.activity_login

    override fun findView() {
        mTilUsername = findViewById(R.id.til_username)
        mTilPassword = findViewById(R.id.til_password)
        mTieUser = findViewById(R.id.et_user)
        mTiePassword = findViewById(R.id.et_password)
        mBtnSubmit = findViewById(R.id.btn_submit)
        mTvSignUp = findViewById(R.id.tv_sign_up)
        mRadioGroup = findViewById(R.id.rg_login_type)
        mRbUser = findViewById(R.id.rb_login_user)
        mRbAdmin = findViewById(R.id.rb_login_admin)
    }

    override fun initView() {
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
        mTieUser.addErrorTextWatcher(mTilUsername, "账号不能为空")
        mTiePassword.addErrorTextWatcher(mTilPassword, "账号不能为空")
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
            val result = ServiceManager.request {
                val request = if (type == LoginType.USER) {
                    val user = User(userId = id, password = password)
                    UserEvent.LoginReq(user = user, loginType = type)
                } else {
                    val admin = Admin(adminId = id, password = password)
                    UserEvent.LoginReq(admin = admin, loginType = type)
                }
                it.login(request.toJsonRequest())
            }
            Log.d(TAG, result.toString())
            mDialog.dismiss()
            mBtnSubmit.isClickable = true
            result?.msg?.toast()
            //处理结果
            if (result != null && result.code == UserEvent.SUCC) {
                val app = application as WanderlustApp
                app.gType = type
                if (type == LoginType.USER) {
                    app.gUser = result.user
                    startActivity(Intent(this@LoginActivity, UserHomeActivity::class.java))
                    finish()
                } else if (type == LoginType.ADMIN)  {
                    app.gAdmin = result.admin
                    startActivity(Intent(this@LoginActivity, AdminHomeActivity::class.java))
                    finish()
                }
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

}