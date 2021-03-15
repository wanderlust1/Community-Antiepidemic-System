package com.wanderlust.community_antiepidemic_system.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.wanderlust.community_antiepidemic_system.R
import com.wanderlust.community_antiepidemic_system.activities.qrcode.QRCodeActivity
import java.lang.Runnable

class MainActivity : AppCompatActivity() {

    private lateinit var mBtnAdd: Button
    private lateinit var mTvNum: TextView

    private lateinit var mViewModel: NumberViewModel

    var mCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
        Thread(Runnable {
            //postRetrofit()
        }).start()

    }

    private fun init() {
        mBtnAdd = findViewById(R.id.btn_add)
        mTvNum = findViewById(R.id.tv_num)
        //获取ViewModel
        mViewModel = ViewModelProvider(this, NumberViewModel.NumberViewModelFactory()).get(NumberViewModel::class.java)
        //使用ViewModel中的数据
        mTvNum.text = mViewModel.mNum.toString()

        mBtnAdd.setOnClickListener {
            mViewModel.mNum = mViewModel.mNum + 1
            mTvNum.text = mViewModel.mNum.toString()
        }
        mTvNum.setOnClickListener {
            startActivity(Intent(this, QRCodeActivity::class.java))
        }
    }

}