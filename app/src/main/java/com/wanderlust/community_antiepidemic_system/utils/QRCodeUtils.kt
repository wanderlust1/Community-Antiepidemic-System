package com.wanderlust.community_antiepidemic_system.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.text.TextUtils
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import com.wanderlust.community_antiepidemic_system.entity.QRCodeMessage
import com.wanderlust.community_antiepidemic_system.entity.QRContent
import com.wanderlust.community_antiepidemic_system.event.RiskAreaEvent
import java.io.ByteArrayOutputStream
import java.lang.StringBuilder
import java.util.*

object QRCodeUtils {

    /**
     * 将Bitmap二维码转为ByteArray
     */
    fun createQRCode(content: String, @ColorRes color: Int, context: Context): ByteArray {
        val length = CommonUtils.dp2px(context, 220f)
        val bitmap = createQRCodeBitmap(content, length, length, ContextCompat.getColor(context, color))
        val stream = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    /**
     * 生成二维码 Bitmap
     * @param content 字符串内容
     * @param width   二维码宽度
     * @param height  二维码高度
     * @param color   色块颜色
     * @return BitMap
     */
    fun createQRCodeBitmap(content: String, width: Int, height: Int, color: Int): Bitmap? {
        return if (TextUtils.isEmpty(content) || width < 0 || height < 0) {
            null
        } else try {
            // 1.设置二维码相关配置
            val hints: Hashtable<EncodeHintType, String> = Hashtable()
            hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
            hints[EncodeHintType.ERROR_CORRECTION] = "Q"
            hints[EncodeHintType.MARGIN] = "0"
            // 2.将配置参数传入到QRCodeWriter的encode方法生成BitMatrix(位矩阵)对象
            val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints)
            // 3.创建像素数组,并根据BitMatrix(位矩阵)对象为数组元素赋颜色值
            val pixels = IntArray(width * height)
            for (y in 0 until height) {
                for (x in 0 until width) {
                    //bitMatrix.get(x,y)方法返回true是黑色色块，false是白色色块
                    if (bitMatrix[x, y]) {
                        pixels[y * width + x] = color //黑色色块像素设置
                    } else {
                        pixels[y * width + x] = Color.WHITE // 白色色块像素设置
                    }
                }
            }
            // 4.创建Bitmap对象,根据像素数组设置Bitmap每个像素点的颜色值,并返回Bitmap对象
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
            bitmap
        } catch (e: WriterException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 根据体温记录、出行记录计算 健康二维码颜色
     * @return QRCodeMessage
     */
    fun getQRCodeColor(qrContent: QRContent, riskAreaRsp: RiskAreaEvent.RiskAreaRsp?): QRCodeMessage {
        if (riskAreaRsp == null) return QRCodeMessage(QRCodeMessage.GREEN)
        //红码条件1：用户已确诊或是疑似病例，或者接触过确诊或疑似病例
        if (qrContent.diagnose != HealthType.NONE) {
            return QRCodeMessage(QRCodeMessage.RED, QRCodeMessage.RED_DIAGNOSE)
        }
        if (qrContent.approach != HealthType.APPROACH_NONE) {
            return QRCodeMessage(QRCodeMessage.RED, QRCodeMessage.RED_APPROACH)
        }
        //红码条件2：用户近一个月的出行记录中高风险地区数 >= 1，中风险地区数 >= 2
        for (highArea in riskAreaRsp.data.highList) {
            for (area in qrContent.outside) {
                if (area.contains(highArea.county)) {
                    return QRCodeMessage(QRCodeMessage.RED, QRCodeMessage.RED_ENTER_HIGH_RISK, highArea.county)
                }
            }
        }
        var midCount  = 0
        var midAreaName = ""
        for (midArea in riskAreaRsp.data.midList) {
            for (area in qrContent.outside) {
                if (area.contains(midArea.county)) {
                    midAreaName = midArea.county
                    if (++midCount > 1) {
                        return QRCodeMessage(QRCodeMessage.RED, QRCodeMessage.RED_ENTER_MID_RISK, midAreaName)
                    }
                }
            }
        }
        //黄码条件：用户近一个月的出行记录中中风险地区数0 <= x <= 1 或用户最近一次体温记录 >= 37.5
        if (midCount > 0) {
            return QRCodeMessage(QRCodeMessage.YELLOW, QRCodeMessage.YELLOW_ENTER_MID_RISK, midAreaName)
        }
        if (qrContent.temperature.isNotEmpty() && qrContent.temperature.toDouble() >= 37.5) {
            return QRCodeMessage(QRCodeMessage.YELLOW, QRCodeMessage.YELLOW_FEVER, qrContent.temperature)
        }
        //绿码条件：
        // 1）最新体温记录 <= 37.4°C
        // 2）最近一个月的外出记录无中高风险地区
        // 3）无确诊或接触
        return QRCodeMessage(QRCodeMessage.GREEN)
    }

    /**
     * 根据体温记录、出行记录，显示健康问题dialog
     * @return QRCodeMessage
     */
    fun showMyHealthProblem(context: Context, qrCodeMessage: QRCodeMessage) {
        when (qrCodeMessage.cause) {
            QRCodeMessage.YELLOW_FEVER -> {
                DialogUtils(context, "我的健康问题",
                    "在最近一次的健康记录中，体温达${qrCodeMessage.message}°C，因此显示为黄色健康码。",
                    "详情请见“我的健康码信息”或“健康登记记录”",
                    "若身体状况好转，请及时更新你的健康登记表，以恢复绿码。"
                ).show()
            }
            QRCodeMessage.YELLOW_ENTER_MID_RISK -> {
                DialogUtils(context, "我的健康问题",
                    "在近一个月内到达过中风险地区${qrCodeMessage.message}，因此显示为黄色健康码。",
                    "详情请见“我的健康码信息”或“外出登记记录”",
                    "请积极配合社区的隔离流程。"
                ).show()
            }
            QRCodeMessage.RED_DIAGNOSE -> {
                DialogUtils(context, "我的健康问题",
                    "在最近一次的健康记录中，你已被诊断为新冠肺炎病例（或疑似病例），因此显示为红色健康码。",
                    "详情请见“我的健康码信息”或“健康登记记录”",
                    "请积极配合社区的隔离流程，若情况有误，请联系社区管理员并及时更新健康登记表。"
                ).show()
            }
            QRCodeMessage.RED_APPROACH -> {
                DialogUtils(context, "我的健康问题",
                    "在最近一次的健康记录中，接触过确诊病例或疑似病例，因此显示为红色健康码。",
                    "详情请见“我的健康码信息”或“健康登记记录”",
                    "请积极配合社区的隔离流程，若情况有误，请联系社区管理员并及时更新健康登记表。"
                ).show()
            }
            QRCodeMessage.RED_ENTER_MID_RISK -> {
                DialogUtils(context, "我的健康问题",
                    "在近一个月内到达过${qrCodeMessage.message}等中风险地区（两个或以上），因此显示为红色健康码。",
                    "详情请见“我的健康码信息”或“外出登记记录”",
                    "请积极配合社区的隔离流程。"
                ).show()
            }
            QRCodeMessage.RED_ENTER_HIGH_RISK -> {
                DialogUtils(context, "我的健康问题",
                    "在近一个月内到达过高风险地区${qrCodeMessage.message}，因此显示为红色健康码。",
                    "详情请见“我的健康码信息”或“外出登记记录”",
                    "请积极配合社区的隔离流程。"
                ).show()
            }
        }
    }

}