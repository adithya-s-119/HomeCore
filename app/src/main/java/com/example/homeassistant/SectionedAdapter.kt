package com.example.homeassistant

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.homeassistant.R
import com.example.homeassistant.Device
import com.example.homeassistant.ESPModule
import androidx.recyclerview.widget.GridLayoutManager

class SectionedAdapter(
    private val espList: List<ESPModule>,
    private val deviceMap: Map<String, List<Device>>,
    private val onDeviceClick: (Device) -> Unit,
    private val onDeviceLongClick: (Device) -> Unit,
    private val onEspClick: (ESPModule) -> Unit,
    private val onEspLongClick: (ESPModule) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_ESP_HEADER = 0
        private const val VIEW_TYPE_DEVICE_TILE = 1
    }

    private data class ListItem(
        val type: Int,
        val esp: ESPModule? = null,
        val device: Device? = null
    )

    private val flatList = mutableListOf<ListItem>()

    init {
        buildFlatList()
    }

    private fun buildFlatList() {
        flatList.clear()
        espList.forEach { esp ->
            flatList.add(ListItem(VIEW_TYPE_ESP_HEADER, esp = esp))
            deviceMap[esp.id]?.forEach { device ->
                flatList.add(ListItem(VIEW_TYPE_DEVICE_TILE, device = device))
            }
        }
    }

    override fun getItemViewType(position: Int): Int = flatList[position].type

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_ESP_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_esp_header, parent, false)
                ESPHeaderViewHolder(view)
            }

            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_device_tile, parent, false)
                DeviceTileViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ESPHeaderViewHolder -> {
                flatList[position].esp?.let { holder.bind(it, onEspClick, onEspLongClick) }
            }

            is DeviceTileViewHolder -> {
                flatList[position].device?.let { holder.bind(it, onDeviceClick, onDeviceLongClick) }
            }
        }
    }

    override fun getItemCount(): Int = flatList.size

    class ESPHeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvEspName: TextView = view.findViewById(R.id.tvEspName)
        private val tvEspIp: TextView = view.findViewById(R.id.tvEspIp)

        fun bind(
            esp: ESPModule,
            onClick: (ESPModule) -> Unit,
            onLongClick: (ESPModule) -> Unit
        ) {
            tvEspName.text = esp.getDisplayName()
            tvEspIp.text = esp.ipAddress
            itemView.setOnClickListener { onClick(esp) }
            itemView.setOnLongClickListener {
                onLongClick(esp)
                true
            }
        }
    }

    class DeviceTileViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvDeviceEmoji: TextView = view.findViewById(R.id.tvDeviceEmoji)
        private val tvDeviceName: TextView = view.findViewById(R.id.tvDeviceName)
        private val tvDeviceStatus: TextView = view.findViewById(R.id.tvDeviceStatus)

        fun bind(
            device: Device,
            onClick: (Device) -> Unit,
            onLongClick: (Device) -> Unit
        ) {
            tvDeviceEmoji.text = device.getEmoji()
            tvDeviceName.text = device.getDisplayName()
            tvDeviceStatus.text = if (device.isOn) "ON" else "OFF"

            val context = itemView.context
            val isDarkMode = context.resources.configuration.uiMode and
                    android.content.res.Configuration.UI_MODE_NIGHT_MASK ==
                    android.content.res.Configuration.UI_MODE_NIGHT_YES

            if (device.isOn) {
                // ON state - White (light) / Light surface (dark)
                tvDeviceStatus.setTextColor(0xFF21808D.toInt())
                (itemView as? androidx.cardview.widget.CardView)?.setCardBackgroundColor(
                    if (isDarkMode) 0xFFFFFFFF.toInt() else 0xFFFFFFFF.toInt()
                )
            } else {
                // OFF state - Grey (light) / Darker surface (dark)
                tvDeviceStatus.setTextColor(0xFF626C71.toInt())
                (itemView as? androidx.cardview.widget.CardView)?.setCardBackgroundColor(
                    if (isDarkMode) 0xFF262828.toInt() else 0xFFE0E0E0.toInt()
                )
            }

            itemView.setOnClickListener { onClick(device) }
            itemView.setOnLongClickListener {
                onLongClick(device)
                true
            }
        }
    }
}