package com.example.homeassistant

import java.util.UUID

data class Device(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    var type: DeviceType,
    var relayId: String,
    val espId: String,
    var isOn: Boolean = false
) {
    fun getDisplayName(): String = name
    fun getEmoji(): String = type.emoji
}