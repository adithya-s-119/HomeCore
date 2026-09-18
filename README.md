[README.md](https://github.com/user-attachments/files/32394413/README.md)
# Home Assistant

A lightweight Android home-control application for managing ESP-based relay modules and the devices connected to them.

The app provides a simple interface for registering ESP modules, assigning devices to each module, controlling relay states, and periodically synchronising the displayed device state with the ESP module.

## Features

- Add, edit, and delete ESP modules.
- Configure an ESP module with:
  - Name
  - IP address
  - Port
- Add, edit, and delete devices.
- Assign each device to a specific ESP module.
- Configure a relay ID for each device.
- Device categories with built-in emoji icons.
- Two-column device grid with ESP module headers.
- Tap a device to toggle its relay.
- Long-press an ESP module or device to edit/delete it.
- Persist ESP and device configuration locally.
- Poll relay states every 5 seconds while the activity is active.
- Automatically update the UI when the remote relay state changes.
- Uses HTTP requests to communicate with the ESP modules.

## Supported Device Types

The current `DeviceType` enum includes:

| Type | Display Name |
|---|---|
| 🖨️ | Printer |
| 💡 | Light |
| 🖥️ | Monitor |
| 💻 | Laptop |
| 📡 | WiFi Repeater |
| 💾 | Hard Disk |
| 🔌 | Charger |
| 🌀 | Fan |
| ❄️ | Air Conditioner |
| 📺 | TV |
| 🔊 | Speaker |
| 📷 | Camera |
| 🌐 | Router |
| ⚙️ | Other |

## Architecture

The application is organised around a few simple components:

```text
Android App
│
├── MainActivity
│   ├── UI / RecyclerView
│   ├── Device & ESP management
│   ├── State polling
│   └── Local data coordination
│
├── SectionedAdapter
│   ├── ESP module headers
│   └── Device tiles
│
├── RelayController
│   └── HTTP communication with ESP modules
│
├── DataStorage
│   └── SharedPreferences + JSON persistence
│
├── ESPModule
│   └── ESP module model
│
├── Device
│   └── Device model
│
├── DeviceType
│   └── Device categories
│
├── AddEditEspDialog
│   └── ESP configuration UI
│
└── AddEditDeviceDialog
    └── Device configuration UI
```

## ESP Module Configuration

Each ESP module stores:

- A unique ID
- A user-defined name
- An IP address
- A network port

The base URL is generated in the form:

```text
http://<IP_ADDRESS>:<PORT>
```

For example:

```text
http://192.168.1.50:80
```

## Device Configuration

Each device stores:

- A unique ID
- Device name
- Device type
- Relay ID
- ESP module ID
- Current ON/OFF state

Devices are grouped under their assigned ESP module.

## Relay Communication

The Android application communicates with the ESP module through HTTP.

### Turn Relay On/Off

The relay controller constructs requests using:

```text
/switch/relay_<relayId>/turn_on
/switch/relay_<relayId>/turn_off
```

The request is sent as an HTTP `POST`.

Example:

```text
POST http://192.168.1.50:80/switch/relay_1/turn_on
```

### Read Relay State

The current relay state is requested with:

```text
GET /switch/relay_<relayId>
```

The application expects a JSON response containing a boolean `value` field.

Example:

```json
{
  "value": true
}
```

## Automatic State Synchronisation

The application polls the configured ESP modules every **5 seconds** while `MainActivity` is in the foreground.

The flow is:

```text
ESP Module
    │
    │ HTTP GET
    ▼
RelayController
    │
    ▼
MainActivity
    │
    ├── Compare remote state
    ├── Update Device.isOn
    ├── Save changed state
    └── Refresh RecyclerView
```

Polling starts in `onResume()` and stops in `onPause()`.

## Local Data Storage

Application configuration is stored locally using Android `SharedPreferences`.

The stored data includes:

### ESP modules

```text
id
name
ipAddress
port
```

### Devices

```text
id
name
type
relayId
espId
isOn
```

The data is serialised as JSON before being stored.

No external database is required for the current implementation.

## UI

The main screen uses a `RecyclerView` with a `GridLayoutManager`.

- ESP module headers span the full width.
- Device tiles occupy one column each.
- Device tiles display:
  - Emoji
  - Device name
  - ON/OFF status

The UI also adapts the device tile background for light and dark mode.

## Interaction

### ESP Modules

**Add**

Use the floating action button to add an ESP module.

**Edit/Delete**

Long-press an ESP module to open the edit/delete menu.

### Devices

**Add**

Use the device floating action button.

An ESP module must exist before a device can be added.

**Toggle**

Tap a device tile to switch its relay between ON and OFF.

**Edit/Delete**

Long-press a device to open the edit/delete menu.

## Error Handling

Relay control requests use a 10-second connection timeout and 10-second read timeout.

If a manual relay toggle fails, the application displays an error message.

Polling failures are intentionally handled silently so temporary network problems do not continuously interrupt the user interface.

## Project Status

This repository contains the current Android application implementation for controlling ESP-based relay devices over a local HTTP network.

The supplied implementation focuses on:

- Device and ESP management
- Local persistence
- Relay control
- Relay state synchronisation
- A simple home-control UI

## ESP Firmware Interface

The Android application expects the ESP-side HTTP interface to provide endpoints equivalent to:

```text
POST /switch/relay_<relayId>/turn_on
POST /switch/relay_<relayId>/turn_off
GET  /switch/relay_<relayId>
```

For state queries, the response should contain:

```json
{
  "value": true
}
```

or:

```json
{
  "value": false
}
```

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
---
