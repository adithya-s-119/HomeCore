package com.example.homeassistant

import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.homeassistant.R
import com.example.homeassistant.ESPModule

class AddEditEspDialog(
    private val esp: ESPModule?,
    private val onSave: (name: String, ip: String, port: Int) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = layoutInflater.inflate(R.layout.dialog_add_edit_esp, null)
        val etName = view.findViewById<EditText>(R.id.etEspName)
        val etIp = view.findViewById<EditText>(R.id.etEspIp)
        val etPort = view.findViewById<EditText>(R.id.etEspPort)

        esp?.let {
            etName.setText(it.name)
            etIp.setText(it.ipAddress)
            etPort.setText(it.port.toString())
        }

        return AlertDialog.Builder(requireContext())
            .setTitle(if (esp == null) "Add ESP Module" else "Edit ESP Module")
            .setView(view)
            .setPositiveButton("Save") { _, _ ->
                val name = etName.text.toString().trim()
                val ip = etIp.text.toString().trim()
                val portText = etPort.text.toString().trim()

                if (name.isEmpty() || ip.isEmpty() || portText.isEmpty()) {
                    Toast.makeText(context, "All fields required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val port = portText.toIntOrNull() ?: 80
                onSave(name, ip, port)
            }
            .setNegativeButton("Cancel", null)
            .create()
    }
}