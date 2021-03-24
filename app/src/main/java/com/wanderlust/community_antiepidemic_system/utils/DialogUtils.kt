package com.wanderlust.community_antiepidemic_system.utils

import android.app.AlertDialog
import android.content.Context
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
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

    /** 创建一个确认信息窗口，含有一段文字以及确认按钮  */
    constructor(context: Context?, message: String?) {
        mDialog = AlertDialog.Builder(context)
            .setMessage(message)
            .setPositiveButton("确定") { dialog, _ -> dialog.dismiss() }.create()
    }

    /** 创建确认一个多条信息的窗口，含有1-3段文字以及确认按钮  */
    constructor(context: Context, title: String, message1: String, message2: String = "", message3: String = "") {
        val dialogView = View.inflate(context, R.layout.dialog_health_problem, null)
        dialogView.findViewById<TextView>(R.id.tv_dialog_health_problem_1)?.text = message1
        if (message2.isEmpty()) {
            dialogView.findViewById<LinearLayout>(R.id.ll_dialog_health_problem_2)?.visibility = View.GONE
        } else {
            dialogView.findViewById<TextView>(R.id.tv_dialog_health_problem_2)?.text = message2
        }
        if (message3.isEmpty()) {
            dialogView.findViewById<LinearLayout>(R.id.ll_dialog_health_problem_3)?.visibility = View.GONE
        } else {
            dialogView.findViewById<TextView>(R.id.tv_dialog_health_problem_3)?.text = message3
        }
        mDialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setTitle(title)
            .setPositiveButton("确定") { dialog, _ -> dialog.dismiss() }.create()
    }

    fun show() = mDialog.show()

    fun dismiss() {
        if (mDialog.isShowing) {
            mDialog.dismiss()
        }
    }

}