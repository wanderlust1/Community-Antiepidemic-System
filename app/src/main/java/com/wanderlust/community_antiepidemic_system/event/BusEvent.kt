package com.wanderlust.community_antiepidemic_system.event

interface BusEvent {

    data class OnCommunityChange(val newCommunityName: String, val newCommunityId: String)

}