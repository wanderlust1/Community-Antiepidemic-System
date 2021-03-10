package com.wanderlust.community_antiepidemic_system.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.wanderlust.community_antiepidemic_system.utils.LoginType
import com.wanderlust.community_antiepidemic_system.R
import com.wanderlust.community_antiepidemic_system.WanderlustApplication
import com.wanderlust.community_antiepidemic_system.utils.DialogUtils

class LoginActivity : AppCompatActivity() {

    private lateinit var mTilUsername: TextInputLayout
    private lateinit var mTilPassword: TextInputLayout
    private lateinit var mTieUser: EditText
    private lateinit var mTiePassword: EditText
    private lateinit var mBtnSubmit: Button
    private lateinit var mTvSignUp: TextView
    private lateinit var mSpinner: Spinner

    /** 已选择的登录类型、账号密码  */
    private var selectedType = LoginType.USER
    private var userText = ""
    private var pwText = ""

    /** 登录进度条  */
    private val mDialog: DialogUtils by lazy { DialogUtils(this) }

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
        mSpinner = findViewById(R.id.sp_type)
        mBtnSubmit.setOnClickListener {
            onSubmit()
        }
        mTvSignUp.setOnClickListener {
            startActivityForResult(Intent(this@LoginActivity, SignUpActivity::class.java), 1)
        }
        initSpinner()
    }

    private fun onSubmit() {
        //检验账号登录流程
        userText = mTieUser.text.toString().trim()
        pwText = mTiePassword.text.toString().trim()
        if (userText.isNullOrEmpty()) {
            mTilUsername.isErrorEnabled = true
            mTilUsername.error = "账号不能为空"
        }
        if (pwText.isNullOrEmpty()) {
            mTilPassword.isErrorEnabled = true
            mTilPassword.error = "密码不能为空"
        }
        if (!(userText.isEmpty() || pwText.isEmpty())) { //输入框不为空，执行下一步
            mTilUsername.isErrorEnabled = false
            mTilPassword.isErrorEnabled = false
            mDialog.show()
            //对这个账号密码进行查询
            val table = if (selectedType == LoginType.USER) "Users" else "Shops"
            val id = if (selectedType == LoginType.USER) "UserID" else "MerchantID"
            val statement = ("select * from " + table
                    + " where " + id + "='" + userText
                    + "' and Pasword='" + pwText + "'")
            /*SQLServerEngine.getDefault().query(statement, 6, object : SQLCallback() {
                fun onSuccess(data: List<String>) {
                    onQueryUserCallback(data)
                }

                fun onFail(error: String) {
                    mDialog.dismiss()
                    MsgDialog(this@LoginActivity, "数据库查询失败：$error").show()
                }
            })*/
        }
    }
/*
    /** 查询用户名及密码之后的回调方法  */
    fun onQueryUserCallback(data: List<String>) {
        if (data.isEmpty()) { //该账号密码不在数据库中，账号密码输入错误
            mDialog.dismiss()
            mTilUsername.setErrorEnabled(true)
            mTilPassword.setErrorEnabled(true)
            mTilUsername.setError("账号或密码错误")
            mTilPassword.setError("账号或密码错误")
        } else { //说明该账号密码均在数据库中，登录成功
            mDialog.dismiss()
            initApplication(data) //将账户信息写入全局变量
            val intent = if (selectedType == App.LOGIN_USER)
                Intent(this@LoginActivity, UserMainActivity::class.java) else
                Intent(this@LoginActivity, ShopMainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
*/
    /** 初始化用户类型选择器控件  */
    private fun initSpinner() {
        val list = mutableListOf("用户登录", "社区管理员登录")
        val arrayAdapter: ArrayAdapter<*> = ArrayAdapter(this, R.layout.spinner_text_view, list)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mSpinner.adapter = arrayAdapter
        mSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                mTilUsername.isErrorEnabled = false
                mTilPassword.isErrorEnabled = false
                when (position) {
                    0 -> {
                        selectedType = LoginType.USER
                        mTilUsername.hint = "用户账号"
                        mTilPassword.hint = "用户密码"
                    }
                    1 -> {
                        selectedType = LoginType.ADMIN
                        mTilUsername.hint = "社区管理员账号"
                        mTilPassword.hint = "社区管理员密码"
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    /** 将账户信息写入全局变量  */
    private fun initApplication(list: List<String>) {
        val app = this.application as WanderlustApplication
        app.gType = selectedType
        if (selectedType == LoginType.USER) { //登录类型为用户登录，把用户信息写入全局变量
            //id, name, password, money, address, phone
            /*val user = User(
                list[0]
                , list[2]
                , list[1]
                , list[3].toDouble(), list[4]
                , list[5]
            )
            app.setUser(user)
            Log.d("`11111111", user.getName())*/
        } else if (selectedType == LoginType.ADMIN) { //登录类型为商户登录，把商户信息写入全局变量
            /*app.setType(App.LOGIN_SHOP)
            //String id, String name, String password, int sales, int goodsNum, String address
            val shop = Shop(
                list[0]
                , list[2]
                , list[1]
                , list[4].toInt(), list[5].toInt(), list[3]
            )
            app.setShop(shop)*/
        }
    }
/*
    /** 启动连接  */
    fun connectSQL() {
        SQLServerEngine.getDefault().connect(object : SQLCallback() {
            fun onSuccess(data: List<String?>?) {}
            fun onFail(error: String) {
                //如果连接失败，则弹出对话框提示重新连接
                val dialog: AlertDialog = Builder(this@LoginActivity)
                    .setMessage("数据库连接失败：$error")
                    .setPositiveButton("重新连接", DialogInterface.OnClickListener { dialog, which -> connectSQL() })
                    .setNegativeButton("退出", DialogInterface.OnClickListener { dialog, which -> finish() })
                    .setCancelable(false).create()
                dialog.show()
            }
        })
    }
*/

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                mTieUser.setText(if (data != null) data.getStringExtra("u") else "?")
                mTiePassword.setText(if (data != null) data.getStringExtra("p") else "?")
                mSpinner.setSelection(data?.getIntExtra("s", 0) ?: 0)
                mTilUsername.isErrorEnabled = false
                mTilPassword.isErrorEnabled = false
            }
        }
    }

}