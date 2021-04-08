package com.wanderlust.community_antiepidemic_system.activities.register

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.wanderlust.community_antiepidemic_system.R

class AdminRegRecordActivity : AppCompatActivity() {

    companion object {
        const val SHOW_TYPE        = "SHOW_TYPE"
        const val USER_ID          = "USER_ID"
        const val USER_NAME        = "USER_NAME"
        const val TYPE_OUTSIDE     = 1 // fragment将显示外出记录
        const val TYPE_TEMPERATURE = 2 // fragment将显示健康记录
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_reg_record)
        val type   = intent.extras?.getInt(SHOW_TYPE, 0)
        val userId = intent.extras?.getString(USER_ID, "")
        val username = intent.extras?.getString(USER_NAME, "")
        findViewById<ImageView>(R.id.iv_admin_reg_back).setOnClickListener {
            finish()
        }
        if (!username.isNullOrEmpty() && type != 0) {
            val typename = if (type == TYPE_OUTSIDE) "外出记录" else "健康记录"
            findViewById<TextView>(R.id.tv_admin_reg_title).text = "${username}的${typename}"
        }
        //添加fragment
        val transaction = supportFragmentManager.beginTransaction()
        if (type == TYPE_OUTSIDE && !userId.isNullOrEmpty()) {
            transaction.add(R.id.fl_reg_container, OutsideRecordFragment.newInstance(userId)).commit()
        } else if (type == TYPE_TEMPERATURE && !userId.isNullOrEmpty()) {
            transaction.add(R.id.fl_reg_container, TemperRecordFragment.newInstance(userId)).commit()
        }
    }

}