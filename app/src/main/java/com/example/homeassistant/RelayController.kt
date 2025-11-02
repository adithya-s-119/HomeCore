package com.example.homeassistant

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class RelayController {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    suspend fun toggleRelay(baseUrl: String, relayId: String, state: Boolean) {
        val action = if (state) "turn_on" else "turn_off"
        val url = "$baseUrl/switch/relay_$relayId/$action"

        val request = Request.Builder()
            .url(url)
            .post(ByteArray(0).toRequestBody())
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("HTTP ${response.code}: ${response.message}")
            }
        }
    }

    // NEW: Get current state of a relay
    suspend fun getRelayState(baseUrl: String, relayId: String): Boolean {
        val url = "$baseUrl/switch/relay_$relayId"

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("HTTP ${response.code}: ${response.message}")
            }

            val body = response.body?.string() ?: return false
            val json = JSONObject(body)
            return json.optBoolean("value", false)
        }
    }

    // NEW: Get states of all devices for an ESP
    suspend fun getAllRelayStates(
        baseUrl: String,
        relayIds: List<String>
    ): Map<String, Boolean> {
        val states = mutableMapOf<String, Boolean>()
        relayIds.forEach { relayId ->
            try {
                states[relayId] = getRelayState(baseUrl, relayId)
            } catch (e: Exception) {
                // If one fails, continue with others
                states[relayId] = false
            }
        }
        return states
    }
}
