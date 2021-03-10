package com.wanderlust.community_antiepidemic_system.utils

import android.graphics.Bitmap
import android.graphics.Color
import android.text.TextUtils
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import java.util.*

object QRCodeUtils {

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

}