package com.example.homeassistant

enum class DeviceType(val emoji: String, val displayName: String) {
    PRINTER("🖨️", "Printer"),
    LIGHT("💡", "Light"),
    MONITOR("🖥️", "Monitor"),
    LAPTOP("💻", "Laptop"),
    WIFI("📡", "WiFi Repeater"),
    HARD_DISK("💾", "Hard Disk"),
    CHARGER("🔌", "Charger"),
    FAN("🌀", "Fan"),
    AC("❄️", "Air Conditioner"),
    TV("📺", "TV"),
    SPEAKER("🔊", "Speaker"),
    CAMERA("📷", "Camera"),
    ROUTER("🌐", "Router"),
    OTHER("⚙️", "Other")
}