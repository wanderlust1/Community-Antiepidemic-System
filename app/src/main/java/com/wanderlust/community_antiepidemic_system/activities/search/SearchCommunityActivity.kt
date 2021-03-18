package com.wanderlust.community_antiepidemic_system.activities.search

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.wanderlust.community_antiepidemic_system.R
import com.wanderlust.community_antiepidemic_system.WanderlustApp
import com.wanderlust.community_antiepidemic_system.entity.Community
import com.wanderlust.community_antiepidemic_system.entity.User
import com.wanderlust.community_antiepidemic_system.event.BusEvent
import com.wanderlust.community_antiepidemic_system.event.CommunityEvent
import com.wanderlust.community_antiepidemic_system.network.ApiService
import com.wanderlust.community_antiepidemic_system.utils.DialogUtils
import com.wanderlust.community_antiepidemic_system.utils.UrlUtils
import com.wanderlust.community_antiepidemic_system.utils.toast
import kotlinx.coroutines.*
import okhttp3.RequestBody.Companion.toRequestBody
import org.greenrobot.eventbus.EventBus
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.ConnectException
import kotlin.coroutines.CoroutineContext

class SearchCommunityActivity : AppCompatActivity(), CoroutineScope {

    companion object {
        const val TAG = "SearchCommunityActivity"
    }

    private lateinit var mEtInput: EditText
    private lateinit var mBtnClear: ImageView
    private lateinit var mRvResult: RecyclerView

    private val mAdapter: SearchAdapter by lazy { SearchAdapter() }

    private val mJob: Job by lazy { Job() }
    override val coroutineContext: CoroutineContext get() = mJob + Dispatchers.Main

    private val mUser: User? by lazy {
        (application as WanderlustApp).gUser
    }

    private var mKeywords = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_community)
        initView()
    }

    private fun initView() {
        mEtInput = findViewById(R.id.et_search_keyword)
        mBtnClear = findViewById(R.id.btn_search_clear)
        mRvResult = findViewById(R.id.rv_search_community)
        //adapter
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        mRvResult.layoutManager = layoutManager
        mRvResult.adapter = mAdapter
        mAdapter.setOnItemClickListener{
            if (it.hasJoined == 1) {
                "已在该社区，无法加入".toast(this)
            } else {
                showJoinDialog(it)
            }
        }
        //开启键盘搜索
        mEtInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val input = mEtInput.text.toString().trim()
                if (input.isNotEmpty()) {
                    mKeywords = input
                    requestSearch()
                }
                return@setOnEditorActionListener true
            }
            false
        }
        //清除按钮
        mBtnClear.setOnClickListener {
            mEtInput.setText("")
        }
        mEtInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                mBtnClear.visibility = if (s.isEmpty()) View.INVISIBLE else View.VISIBLE
            }
        })
        showSoftKeyboard()
    }

    private fun showJoinDialog(community: Community) {
        val user = mUser ?: return
        val message = if (user.communityId.isEmpty()) {
            "是否加入${community.name}？"
        } else {
            "当前已在${user.communityName}，是否修改你的社区为${community.name}？"
        }
        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("确定") { dialog, _ ->
                if (user.communityId.isEmpty()) {
                    requestJoinCommunity(user.userId, community.id, community.name)
                } else {
                    requestJoinCommunity(user.userId, community.id, community.name, user.communityId)
                }
                dialog.dismiss()
            }
            .setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun requestSearch() {
        if (mKeywords.isEmpty()) return
        launch {
            val retrofit = Retrofit.Builder()
                .baseUrl(UrlUtils.SERVICE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
            val response = try {
                withContext(Dispatchers.IO) {
                    val request = CommunityEvent.SearchReq(mKeywords, mUser?.userId ?: "")
                    retrofit.searchCommunity(Gson().toJson(request).toRequestBody()).execute()
                }
            } catch (e: ConnectException) {
                R.string.connection_error.toast(this@SearchCommunityActivity)
                null
            } catch (e: Exception) {
                e.printStackTrace()
                R.string.timeout_error.toast(this@SearchCommunityActivity)
                null
            }
            Log.d(TAG, response?.body().toString())
            if (response?.body() == null) return@launch
            val result = response.body()!!
            if (result.result.isEmpty()) {
                mAdapter.update("未找到“${mKeywords}”的搜索结果")
            } else {
                mAdapter.update(result.result)
            }
        }
    }

    private fun requestJoinCommunity(userId: String, newId: String, newName: String, oldId: String? = null) {
        launch {
            val retrofit = Retrofit.Builder()
                .baseUrl(UrlUtils.SERVICE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
            val response = try {
                withContext(Dispatchers.IO) {
                    val type = if (oldId == null) CommunityEvent.NEW_JOIN else CommunityEvent.CHANGE
                    val request = CommunityEvent.JoinReq(userId, oldId ?: "", newId, type)
                    retrofit.joinCommunity(Gson().toJson(request).toRequestBody()).execute()
                }
            } catch (e: ConnectException) {
                R.string.connection_error.toast(this@SearchCommunityActivity)
                null
            } catch (e: Exception) {
                e.printStackTrace()
                R.string.timeout_error.toast(this@SearchCommunityActivity)
                null
            }
            Log.d(TAG, response?.body().toString())
            if (response?.body() == null) return@launch
            val result = response.body()!!
            result.msg.toast(this@SearchCommunityActivity)
            if (result.code == CommunityEvent.SUCC) {
                mUser?.communityId = newId
                mUser?.communityName = newName
                requestSearch() //目的是更新搜索列表
                EventBus.getDefault().post(BusEvent.OnCommunityChange(newName, newId)) //通知home
            }
        }
    }

    private fun showSoftKeyboard() {
        val imm: InputMethodManager = mEtInput.context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        mEtInput.requestFocus()
        imm.showSoftInput(mEtInput, 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        mJob.cancel()
    }

}