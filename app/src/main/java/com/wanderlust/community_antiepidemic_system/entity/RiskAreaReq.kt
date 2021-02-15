package com.wanderlust.community_antiepidemic_system.entity

import com.google.gson.annotations.Expose
import okhttp3.internal.and
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

data class RiskAreaReq (

    val appId: String = STATE_COUNCIL_APP_ID,

    val paasHeader: String = STATE_COUNCIL_PASSID,

    val key: String = STATE_COUNCIL_KEY,

    val nonceHeader: String = STATE_COUNCIL_NONCE,

    var signatureHeader: String = "",

    var timestampHeader: String = "",

    @Expose(serialize = false) val timestamp: Long

) {

    companion object {

        const val STATE_COUNCIL_SIGNATURE_KEY = "fTN2pfuisxTavbTuYVSsNJHetwq5bJvCQkjjtiLM2dCratiA"

        const val STATE_COUNCIL_X_WIF_NONCE = "QkjjtiLM2dCratiA"

        const val STATE_COUNCIL_X_WIF_PAASID = "smt-application"

        //国务院疫情防控查询 appId
        const val STATE_COUNCIL_APP_ID = "NcApplication"

        //国务院疫情防控查询 PASSID
        const val STATE_COUNCIL_PASSID = "zdww"

        //国务院疫情防控查询 秘钥
        const val STATE_COUNCIL_NONCE = "123456789abcdefg"

        //国务院疫情防控查询 token
        const val STATE_COUNCIL_TOEKN = "23y0ufFl5YxIyGrI8hWRUZmKkvtSjLQA"

        //国务院疫情防控查询 key
        const val STATE_COUNCIL_KEY = "3C502C97ABDA40D0A60FBEE50FAAD1DA"

        /**
         * 国务院疫情风险接口 SHA-256 加密
         * @param str 被加密的字符串
         */
        fun getSHA256StrJava(str: String): String {
            val messageDigest: MessageDigest
            var encodeStr = ""
            try {
                messageDigest = MessageDigest.getInstance("SHA-256")
                messageDigest.update(str.toByteArray(StandardCharsets.UTF_8))
                val bytes: ByteArray = messageDigest.digest()
                val stringBuffer = StringBuffer()
                var temp: String
                for (i in bytes.indices) {
                    temp = Integer.toHexString(bytes[i] and 0xFF)
                    if (temp.length == 1) {
                        stringBuffer.append("0")
                    }
                    stringBuffer.append(temp)
                }
                encodeStr = stringBuffer.toString()
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            }
            return encodeStr
        }

    }

    init {
        val signatureStr = String.format("%d%s%s%d", timestamp, STATE_COUNCIL_TOEKN, STATE_COUNCIL_NONCE, timestamp)
        signatureHeader = getSHA256StrJava(signatureStr).toUpperCase(Locale.ROOT)
        timestampHeader = timestamp.toString()
    }

}