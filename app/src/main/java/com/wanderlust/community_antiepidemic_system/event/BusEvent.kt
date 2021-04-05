package com.wanderlust.community_antiepidemic_system.event

interface BusEvent {

    class OnCommunityChange

    class OnAdminCommunityChange

    data class NoReadCountChange(val noReadCount: Int)

    class NoticeListUpdate

}