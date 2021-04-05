package com.wanderlust.community_antiepidemic_system.activities.notice

import android.app.AlertDialog
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.google.gson.Gson
import com.wanderlust.community_antiepidemic_system.R
import com.wanderlust.community_antiepidemic_system.activities.BaseActivity
import com.wanderlust.community_antiepidemic_system.entity.Notice
import com.wanderlust.community_antiepidemic_system.event.BusEvent
import com.wanderlust.community_antiepidemic_system.event.NoticeEvent
import com.wanderlust.community_antiepidemic_system.network.Service
import com.wanderlust.community_antiepidemic_system.utils.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.RequestBody.Companion.toRequestBody
import org.greenrobot.eventbus.EventBus
import java.net.ConnectException
import java.text.SimpleDateFormat
import java.util.*

class EditNoticeActivity : BaseActivity() {

    companion object {
        const val TAG = "EditNoticeActivity"
        const val MAX_CONTENT_LEN = 800
        val TYPES = arrayOf("普通公告", "健康登记提醒")
    }

    private lateinit var mTvSubmit: TextView
    private lateinit var mTvDate: TextView
    private lateinit var mTvWordCount: TextView
    private lateinit var mTvType: TextView
    private lateinit var mEtTitle: EditText
    private lateinit var mEtContent: EditText

    private var mNoticeType = Notice.NOTICE_NORMAL

    override fun contentView() = R.layout.activity_edit_notice

    override fun findView() {
        mTvSubmit = findViewById(R.id.tv_notice_edit_submit)
        mTvDate = findViewById(R.id.tv_notice_edit_date)
        mTvWordCount = findViewById(R.id.tv_notice_edit_word_count)
        mTvType = findViewById(R.id.tv_notice_edit_type)
        mEtTitle = findViewById(R.id.et_notice_edit_title)
        mEtContent = findViewById(R.id.et_notice_edit_content)
    }

    override fun initView() {
        mCountDownTimer.start()
        mTvType.setOnClickListener {
            AlertDialog.Builder(this).setItems(TYPES) { _, which ->
                mTvType.text = TYPES[which]
                mNoticeType = when (which) {
                    0 -> Notice.NOTICE_NORMAL
                    1 -> Notice.NOTICE_ALERT
                    else -> Notice.NOTICE_NORMAL
                }
            }.create().show()
        }
        mEtContent.addTextChangedListener(mLimitTextWatcher)
        mTvWordCount.text = "0/$MAX_CONTENT_LEN"
        findViewById<ImageView>(R.id.iv_notice_edit_back).setOnClickListener {
            finish()
        }
        mTvSubmit.setOnClickListener {
            if (mNoticeType == Notice.NOTICE_NORMAL && mEtContent.text.isBlank()) {
                "请填写公告内容".toast(this)
            } else {
                submitNotice(mEtTitle.text.toString(), mEtContent.text.toString())
            }
        }
    }

    private fun submitNotice(title: String, content: String) {
        if (mAdmin == null || mAdmin?.communityId == null) return
        val notice = Notice().apply {
            this.content = content
            this.title = title
            type = mNoticeType
            communityId = mAdmin!!.communityId
        }
        launch {
            val response = try {
                withContext(Dispatchers.IO) {
                    val request = NoticeEvent.AddNoticeReq(notice)
                    Service.request.addNotice(Gson().toJson(request).toRequestBody()).execute()
                }
            } catch (e: ConnectException) {
                R.string.connection_error.toast(this@EditNoticeActivity)
                null
            } catch (e: Exception) {
                e.printStackTrace()
                R.string.timeout_error.toast(this@EditNoticeActivity)
                null
            }
            Log.d(TAG, response?.body().toString())
            if (response?.body() == null) return@launch
            val result = response.body()!!
            result.msg.toast(this@EditNoticeActivity)
            if (result.code == NoticeEvent.SUCC) {
                EventBus.getDefault().post(BusEvent.NoticeListUpdate())
                finish()
            }
        }
    }

    private val mCountDownTimer = object : CountDownTimer(Long.MAX_VALUE, 1000) {

        override fun onFinish() {
        }

        override fun onTick(millisUntilFinished: Long) {
            val date = SimpleDateFormat("发布于 yyyy年MM月dd日 HH:mm:ss", Locale.CHINA).format(System.currentTimeMillis())
            mTvDate.text = date
        }

    }

    private val mLimitTextWatcher =  object: TextWatcher {

        private var limit = MAX_CONTENT_LEN
        private var cursor = 0
        private var beforeLength = 0

        override fun beforeTextChanged (s: CharSequence, start: Int, count: Int, after: Int) {
            beforeLength = s.length
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            cursor = start
        }

        override fun afterTextChanged(s: Editable) {
            val afterLength = s.length
            if (afterLength > limit) {
                val inputNum = afterLength - beforeLength
                val start = cursor + (inputNum - afterLength + limit)
                val end   = cursor + inputNum
                mEtContent.setText(s.delete(start, end).toString())
                mEtContent.setSelection(start)
            }
            mTvWordCount.text = "${afterLength.coerceAtMost(limit)}/$limit"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mCountDownTimer.cancel()
    }

}