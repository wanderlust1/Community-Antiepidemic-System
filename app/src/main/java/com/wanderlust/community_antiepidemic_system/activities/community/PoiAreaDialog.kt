package com.wanderlust.community_antiepidemic_system.activities.community

import android.content.Context
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.core.content.ContextCompat.getColor
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.baidu.mapapi.search.core.SearchResult
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener
import com.baidu.mapapi.search.sug.SuggestionResult
import com.baidu.mapapi.search.sug.SuggestionSearch
import com.baidu.mapapi.search.sug.SuggestionSearchOption
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.wanderlust.community_antiepidemic_system.R


class PoiAreaDialog(context: Context): BottomSheetDialog(context), OnGetSuggestionResultListener {

    private val mSuggestionSearch: SuggestionSearch by lazy {
        SuggestionSearch.newInstance().apply {
            setOnGetSuggestionResultListener(this@PoiAreaDialog)
        }
    }

    private var mEtCity: EditText? = null
    private var mEtKeyword: EditText? = null
    private var mRecyclerView: RecyclerView? = null

    private val mAdapter: PoiAreaAdapter by lazy {
        PoiAreaAdapter().apply {
            setOnItemClickListener {
                mOnSelectListener?.invoke(it)
                dismiss()
            }
        }
    }

    private val mAreaTextWatcher = object: TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable) {
            searchArea()
        }
    }

    private var mOnSelectListener: ((SuggestionResult.SuggestionInfo) -> Unit)? = null

    fun setOnSelectListener(listener: (SuggestionResult.SuggestionInfo) -> Unit) {
        mOnSelectListener = listener
    }

    init {
        setContentView(R.layout.dialog_search_area)
        mEtCity = findViewById(R.id.et_poi_city)
        mEtKeyword = findViewById(R.id.et_poi_keyword)
        mRecyclerView = findViewById(R.id.rv_poi_area)
        delegate.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            ?.setBackgroundColor(getColor(context, android.R.color.transparent))
        window?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)?.let {
            val height = context.resources.displayMetrics.heightPixels
            BottomSheetBehavior.from(it).peekHeight = height
            it.layoutParams.height = height
        }
        mEtKeyword?.addTextChangedListener(mAreaTextWatcher)
        mEtCity?.addTextChangedListener(mAreaTextWatcher)
        mRecyclerView?.layoutManager = LinearLayoutManager(context)
        mRecyclerView?.adapter = mAdapter
    }

    private fun searchArea() {
        val cityStr = mEtCity?.text?.toString()
        val keyWordStr = mEtKeyword?.text?.toString()
        if (!cityStr.isNullOrBlank() && !keyWordStr.isNullOrBlank()) {
            mSuggestionSearch.requestSuggestion(
                SuggestionSearchOption().city(cityStr).keyword(keyWordStr).citylimit(true)
            )
        }
    }

    override fun onGetSuggestionResult(suggestionResult: SuggestionResult?) {
        if (suggestionResult == null || suggestionResult.error === SearchResult.ERRORNO.RESULT_NOT_FOUND) {
            mAdapter.update(emptyList())
            return
        }
        val result = suggestionResult.allSuggestions ?: return
        mRecyclerView!!.visibility = View.VISIBLE
        mAdapter.update(result.filter {
            it.address.isNotEmpty()
        })
    }

}