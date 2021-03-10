package com.wanderlust.community_antiepidemic_system.utils

import android.app.AlertDialog
import android.content.Context
import android.view.View
import com.wanderlust.community_antiepidemic_system.R

/**
 * 快速创建对话框
 */
class DialogUtils {

    private var mDialog: AlertDialog

    /** 参数只有context：创建一个圆形进度条窗口，不显示文字  */
    constructor(context: Context?) {
        val dialogView = View.inflate(context, R.layout.dialog_processing, null)
        mDialog = AlertDialog.Builder(context)
            .setView(dialogView).setCancelable(false).create()
    }

    /** 创建一个确认信息窗口，含有一段文字确认按钮  */
    constructor(context: Context?, message: String?) {
        mDialog = AlertDialog.Builder(context)
            .setMessage(message)
            .setPositiveButton("确定") { dialog, _ -> dialog.dismiss() }.setCancelable(false).create()
    }

    fun show() = mDialog.show()

    fun dismiss() {
        if (mDialog.isShowing) {
            mDialog.dismiss()
        }
    }

}