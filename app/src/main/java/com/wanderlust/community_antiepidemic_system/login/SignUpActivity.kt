package com.wanderlust.community_antiepidemic_system.login

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.wanderlust.community_antiepidemic_system.utils.LoginType
import com.wanderlust.community_antiepidemic_system.R
import com.wanderlust.community_antiepidemic_system.utils.DialogUtils

/**
 *
 * 【活动】注册主页面
 *
 */
class SignUpActivity : AppCompatActivity() {

    private lateinit var mSpinner: Spinner
    private lateinit var mEtUser: EditText
    private lateinit var mEtPassword: EditText
    private lateinit var mBtnSignUp: Button
    private lateinit var mTilUsername: TextInputLayout
    private lateinit var mTilPassword: TextInputLayout

    private var userText: String? = null
    private var pwText: String? = null
    private var selectedType = LoginType.USER //已选择的注册类型

    /** 登录进度条  */
    private val mDialog: DialogUtils by lazy { DialogUtils(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        initView()
    }

    private fun initView() {
        mSpinner  = findViewById(R.id.sp_type)
        mEtUser  = findViewById(R.id.et_new_user)
        mEtPassword =  findViewById(R.id.et_new_password)
        mBtnSignUp  = findViewById(R.id.btn_sign_up)
        mTilUsername  = findViewById(R.id.til_new_username)
        mTilPassword  = findViewById(R.id.til_new_password)
        mBtnSignUp.setOnClickListener {
            onSubmit()
        }
        initSpinner()
    }

    private fun onSubmit() {
        userText = mEtUser.text.toString().trim()
        pwText = mEtPassword.text.toString().trim()
        if (userText.isNullOrEmpty()) {
            mTilUsername.isErrorEnabled = true
            mTilUsername.error = "账号不能为空"
            return
        }
        if (pwText.isNullOrEmpty()) {
            mTilPassword.isErrorEnabled = true
            mTilPassword.error = "密码不能为空"
            return
        }
        mDialog.show()
        val table = if (selectedType == LoginType.USER) "Users" else "Shops"
        val id = if (selectedType == LoginType.USER) "UserID" else "MerchantID"
        val statement = ("select " + id + " from " + table
                + " where " + id + "='" + userText + "'")
    }

    /** 查询操作的回调方法  */
    private fun onQueryCallback(data: List<String>) {
        if (data.isNotEmpty()) { //说明该账号在数据库中，注册失败
            mDialog.dismiss()
            mTilUsername.error = "该账号已存在"
        } else { //该账号不在数据库中，注册成功，写入数据库
            var statement = ""
            statement = when (selectedType) {
                LoginType.USER -> ("insert into Users values('"
                        + userText + "','" + pwText + "','user" + userText
                        + "','0','未设置','未设置')")
                LoginType.ADMIN -> ("insert into Shops values('"
                        + userText + "','" + pwText + "','shop" + userText
                        + "','未设置','0','0')")
            }
            //执行插入操作，将新用户信息写入数据库
            /*SQLServerEngine.getDefault().operate(statement, object : SQLCallback() {
                fun onSuccess(data: List<String?>?) {
                    mDialog.dismiss()
                    val intent = Intent()
                    intent.putExtra("u", userText)
                    intent.putExtra("p", pwText)
                    intent.putExtra("s", selectedType)
                    setResult(Activity.RESULT_OK, intent)
                    Toast.makeText(this@SignUpActivity, "账号注册成功", Toast.LENGTH_SHORT).show()
                    finish()
                }

                fun onFail(error: String) {
                    mDialog.dismiss()
                    MsgDialog(this@SignUpActivity, "注册失败：$error").show()
                }
            })*/
        }
    }

    private fun initSpinner() {
        val list = mutableListOf("用户注册", "社区管理员注册")
        val arrayAdapter: ArrayAdapter<*> = ArrayAdapter(this, R.layout.spinner_text_view, list)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mSpinner.adapter = arrayAdapter
        mSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                when (position) {
                    0 -> {
                        selectedType = LoginType.USER
                        mTilUsername.hint = "新用户账号"
                        mTilPassword.hint = "密码"
                    }
                    1 -> {
                        selectedType = LoginType.ADMIN
                        mTilPassword.hint = "新社区管理员账号"
                        mTilUsername.hint = "密码"
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
}