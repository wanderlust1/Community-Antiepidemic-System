package com.wanderlust.community_antiepidemic_system.event

import com.wanderlust.community_antiepidemic_system.entity.QRContent

interface QRCodeEvent {

    data class QRContentReq(val userId: String)

    data class QRContentRsp(val qrContent: QRContent)

}