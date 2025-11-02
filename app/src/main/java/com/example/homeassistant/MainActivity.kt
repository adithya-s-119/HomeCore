package com.example.homeassistant

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.homeassistant.SectionedAdapter
import com.example.homeassistant.AddEditDeviceDialog
import com.example.homeassistant.AddEditEspDialog
import com.example.homeassistant.Device
import com.example.homeassistant.ESPModule
import com.example.homeassistant.RelayController
import com.example.homeassistant.DataStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.recyclerview.widget.GridLayoutManager
import android.os.Handler
import android.os.Looper
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAddEsp: FloatingActionButton
    private lateinit var fabAddDevice: FloatingActionButton

    private val espList = mutableListOf<ESPModule>()
    private val deviceMap = mutableMapOf<String, MutableList<Device>>()
    private val relayController = RelayController()
    private lateinit var dataStorage: DataStorage  // Added storage

    private val pollHandler = Handler(Looper.getMainLooper())
    private val pollInterval = 5000L // Poll every 5 seconds
    private var isPolling = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize storage
        dataStorage = DataStorage(this)

        recyclerView = findViewById(R.id.recyclerView)
        fabAddEsp = findViewById(R.id.fabAddEsp)
        fabAddDevice = findViewById(R.id.fabAddDevice)

        recyclerView.layoutManager = GridLayoutManager(this, 2).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    val adapter = recyclerView.adapter as? SectionedAdapter
                    return when (adapter?.getItemViewType(position)) {
                        0 -> 2  // ESP header spans 2 columns (full width)
                        else -> 1  // Device tiles span 1 column (half width)
                    }
                }
            }
        }

        // Load saved data before refreshing adapter
        loadData()
        refreshAdapter()

        fabAddEsp.setOnClickListener { showAddEspDialog() }
        fabAddDevice.setOnClickListener { showAddDeviceDialog() }
    }

    private fun loadData() {
        espList.clear()
        espList.addAll(dataStorage.loadESPList())

        deviceMap.clear()
        deviceMap.putAll(dataStorage.loadDeviceMap())
    }

    private fun saveData() {
        dataStorage.saveESPList(espList)
        dataStorage.saveDeviceMap(deviceMap)
    }

    private fun refreshAdapter() {
        val adapter = SectionedAdapter(
            espList = espList,
            deviceMap = deviceMap,
            onDeviceClick = { device -> toggleDevice(device) },
            onDeviceLongClick = { device -> showEditDeviceDialog(device) },
            onEspClick = { esp -> Toast.makeText(this, "ESP: ${esp.name}", Toast.LENGTH_SHORT).show() },
            onEspLongClick = { esp -> showEditEspDialog(esp) }
        )
        recyclerView.adapter = adapter
    }

    private fun toggleDevice(device: Device) {
        val esp = espList.find { it.id == device.espId } ?: return
        val newState = !device.isOn

        CoroutineScope(Dispatchers.IO).launch {
            try {
                relayController.toggleRelay(esp.getBaseUrl(), device.relayId, newState)
                withContext(Dispatchers.Main) {
                    device.isOn = newState
                    saveData()
                    refreshAdapter()
                    // Removed Toast for successful toggle
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // Only show Toast on error
                    Toast.makeText(
                        this@MainActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private val pollRunnable = object : Runnable {
        override fun run() {
            if (isPolling) {
                pollDeviceStates()
                pollHandler.postDelayed(this, pollInterval)
            }
        }
    }

    private fun pollDeviceStates() {
        CoroutineScope(Dispatchers.IO).launch {
            espList.forEach { esp ->
                try {
                    val devices = deviceMap[esp.id] ?: return@forEach
                    val relayIds = devices.map { it.relayId }
                    val states = relayController.getAllRelayStates(esp.getBaseUrl(), relayIds)

                    withContext(Dispatchers.Main) {
                        var hasChanges = false
                        devices.forEach { device ->
                            val newState = states[device.relayId] ?: false
                            if (device.isOn != newState) {
                                device.isOn = newState
                                hasChanges = true
                            }
                        }
                        if (hasChanges) {
                            saveData()
                            refreshAdapter()
                        }
                    }
                } catch (e: Exception) {
                    // Silently fail - don't show error for polling failures
                }
            }
        }
    }

    private fun startPolling() {
        isPolling = true
        pollHandler.post(pollRunnable)
    }

    private fun stopPolling() {
        isPolling = false
        pollHandler.removeCallbacks(pollRunnable)
    }

    private fun showAddEspDialog() {
        AddEditEspDialog(
            esp = null,
            onSave = { name, ip, port ->
                val newEsp = ESPModule(name = name, ipAddress = ip, port = port)
                espList.add(newEsp)
                deviceMap[newEsp.id] = mutableListOf()
                saveData()  // Save after adding ESP
                refreshAdapter()
            }
        ).show(supportFragmentManager, "AddEspDialog")
    }

    private fun showEditEspDialog(esp: ESPModule) {
        AlertDialog.Builder(this)
            .setTitle(esp.name)
            .setItems(arrayOf("Edit", "Delete")) { _, which ->
                when (which) {
                    0 -> {
                        AddEditEspDialog(
                            esp = esp,
                            onSave = { name, ip, port ->
                                esp.name = name
                                esp.ipAddress = ip
                                esp.port = port
                                saveData()  // Save after editing ESP
                                refreshAdapter()
                            }
                        ).show(supportFragmentManager, "EditEspDialog")
                    }
                    1 -> {
                        espList.remove(esp)
                        deviceMap.remove(esp.id)
                        saveData()  // Save after deleting ESP
                        refreshAdapter()
                        Toast.makeText(this, "ESP deleted", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .show()
    }

    private fun showAddDeviceDialog() {
        if (espList.isEmpty()) {
            Toast.makeText(this, "Add an ESP module first", Toast.LENGTH_SHORT).show()
            return
        }
        AddEditDeviceDialog(
            device = null,
            espList = espList,
            onSave = { name, type, pin, espId ->
                val newDevice = Device(name = name, type = type, relayId = pin, espId = espId)
                deviceMap[espId]?.add(newDevice)
                saveData()  // Save after adding device
                refreshAdapter()
            }
        ).show(supportFragmentManager, "AddDeviceDialog")
    }

    private fun showEditDeviceDialog(device: Device) {
        AlertDialog.Builder(this)
            .setTitle(device.name)
            .setItems(arrayOf("Edit", "Delete")) { _, which ->
                when (which) {
                    0 -> {
                        AddEditDeviceDialog(
                            device = device,
                            espList = espList,
                            onSave = { name, type, pin, espId ->
                                device.name = name
                                device.type = type
                                device.relayId = pin
                                saveData()  // Save after editing device
                                refreshAdapter()
                            }
                        ).show(supportFragmentManager, "EditDeviceDialog")
                    }
                    1 -> {
                        deviceMap[device.espId]?.remove(device)
                        saveData()  // Save after deleting device
                        refreshAdapter()
                        Toast.makeText(this, "Device deleted", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .show()
    }

    override fun onResume() {
        super.onResume()
        startPolling()
    }

    override fun onPause() {
        super.onPause()
        stopPolling()
    }
}
