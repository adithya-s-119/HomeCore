package com.example.homeassistant

import android.content.Context
import android.content.SharedPreferences
import com.example.homeassistant.Device
import com.example.homeassistant.DeviceType
import com.example.homeassistant.ESPModule
import org.json.JSONArray
import org.json.JSONObject

class DataStorage(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("esp_home_data", Context.MODE_PRIVATE)

    // Save ESP modules
    fun saveESPList(espList: List<ESPModule>) {
        val jsonArray = JSONArray()
        espList.forEach { esp ->
            val jsonObj = JSONObject().apply {
                put("id", esp.id)
                put("name", esp.name)
                put("ipAddress", esp.ipAddress)
                put("port", esp.port)
            }
            jsonArray.put(jsonObj)
        }
        prefs.edit().putString("esp_list", jsonArray.toString()).apply()
    }

    // Load ESP modules
    fun loadESPList(): MutableList<ESPModule> {
        val list = mutableListOf<ESPModule>()
        val jsonString = prefs.getString("esp_list", null) ?: return list

        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    ESPModule(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        ipAddress = obj.getString("ipAddress"),
                        port = obj.getInt("port")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    // Save device map
    fun saveDeviceMap(deviceMap: Map<String, List<Device>>) {
        val jsonObj = JSONObject()
        deviceMap.forEach { (espId, devices) ->
            val devicesArray = JSONArray()
            devices.forEach { device ->
                val deviceObj = JSONObject().apply {
                    put("id", device.id)
                    put("name", device.name)
                    put("type", device.type.name)
                    put("relayId", device.relayId)
                    put("espId", device.espId)
                    put("isOn", device.isOn)
                }
                devicesArray.put(deviceObj)
            }
            jsonObj.put(espId, devicesArray)
        }
        prefs.edit().putString("device_map", jsonObj.toString()).apply()
    }

    // Load device map
    fun loadDeviceMap(): MutableMap<String, MutableList<Device>> {
        val map = mutableMapOf<String, MutableList<Device>>()
        val jsonString = prefs.getString("device_map", null) ?: return map

        try {
            val jsonObj = JSONObject(jsonString)
            jsonObj.keys().forEach { espId ->
                val devices = mutableListOf<Device>()
                val devicesArray = jsonObj.getJSONArray(espId)
                for (i in 0 until devicesArray.length()) {
                    val deviceObj = devicesArray.getJSONObject(i)
                    devices.add(
                        Device(
                            id = deviceObj.getString("id"),
                            name = deviceObj.getString("name"),
                            type = DeviceType.valueOf(deviceObj.getString("type")),
                            relayId = deviceObj.getString("relayId"),
                            espId = deviceObj.getString("espId"),
                            isOn = deviceObj.getBoolean("isOn")
                        )
                    )
                }
                map[espId] = devices
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return map
    }

    // Clear all data
    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
