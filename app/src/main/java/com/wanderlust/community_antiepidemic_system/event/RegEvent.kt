package com.wanderlust.community_antiepidemic_system.event

import com.wanderlust.community_antiepidemic_system.entity.OutSideReg
import com.wanderlust.community_antiepidemic_system.entity.TemperReg

interface RegEvent {

    companion object {
        const val SUCC = 0
        const val FAIL = 1
    }

    data class GetTemperRecordReq(val userId: String)

    data class GetTemperRecordRsp(val list: List<TemperReg>)

    data class GetOutSideRecordReq(val userId: String)

    data class GetOutSideRecordRsp(val list: List<OutSideReg>)

    data class AddTemperRecordReq(val record: TemperReg)

    data class AddTemperRecordRsp(val code: Int, val msg: String)

    data class AddOutsideRecordReq(val record: OutSideReg)

    data class AddOutsideRecordRsp(val code: Int, val msg: String)

}