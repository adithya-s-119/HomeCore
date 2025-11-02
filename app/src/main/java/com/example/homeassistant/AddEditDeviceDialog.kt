package com.example.homeassistant

import android.app.Dialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.homeassistant.R
import com.example.homeassistant.Device
import com.example.homeassistant.DeviceType
import com.example.homeassistant.ESPModule

class AddEditDeviceDialog(
    private val device: Device?,
    private val espList: List<ESPModule>,
    private val onSave: (name: String, type: DeviceType, pin: String, espId: String) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = layoutInflater.inflate(R.layout.dialog_add_edit_device, null)
        val etName = view.findViewById<EditText>(R.id.etDeviceName)
        val spinnerType = view.findViewById<Spinner>(R.id.spinnerDeviceType)
        val etPin = view.findViewById<EditText>(R.id.etRelayPin)
        val spinnerEsp = view.findViewById<Spinner>(R.id.spinnerEsp)

        // Device type spinner
        val types = DeviceType.values()
        val typeAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            types.map { "${it.emoji} ${it.displayName}" }
        )
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerType.adapter = typeAdapter

        // ESP spinner
        val espAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            espList.map { it.getDisplayName() }
        )
        espAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerEsp.adapter = espAdapter

        device?.let {
            etName.setText(it.name)
            spinnerType.setSelection(types.indexOf(it.type))
            etPin.setText(it.relayId.toString())
            val espIndex = espList.indexOfFirst { esp -> esp.id == it.espId }
            if (espIndex != -1) spinnerEsp.setSelection(espIndex)
        }

        return AlertDialog.Builder(requireContext())
            .setTitle(if (device == null) "Add Device" else "Edit Device")
            .setView(view)
            .setPositiveButton("Save") { _, _ ->
                val name = etName.text.toString().trim()
                val pinText = etPin.text.toString().trim()

                if (name.isEmpty() || pinText.isEmpty()) {
                    Toast.makeText(context, "All fields required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val type = types[spinnerType.selectedItemPosition]
                val pin: String = pinText
                val selectedEsp = espList[spinnerEsp.selectedItemPosition]

                onSave(name, type, pin, selectedEsp.id)
            }
            .setNegativeButton("Cancel", null)
            .create()
    }
}