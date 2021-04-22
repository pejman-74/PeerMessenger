package com.peer_messanger.data.wrapper

import android.bluetooth.BluetoothDevice

sealed class ScanResource {
    class DeviceFound(val device: BluetoothDevice) : ScanResource()
    object DiscoveryStarted : ScanResource()
    object DiscoveryFinished : ScanResource()
}