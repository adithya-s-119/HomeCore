package com.example.homeassistant

import java.util.UUID

data class ESPModule(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    var ipAddress: String,
    var port: Int = 80
) {
    fun getDisplayName(): String = name
    fun getBaseUrl(): String = "http://$ipAddress:$port"
}