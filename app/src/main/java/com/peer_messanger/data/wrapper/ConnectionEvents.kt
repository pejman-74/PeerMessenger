package com.peer_messanger.data.wrapper

import android.bluetooth.BluetoothDevice

sealed class ConnectionEvents {
    class Connected(val device: BluetoothDevice) : ConnectionEvents()
    class Disconnect(val deviceName: String?) : ConnectionEvents()
}