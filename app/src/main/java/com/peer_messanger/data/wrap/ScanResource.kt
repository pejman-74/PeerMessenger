package com.peer_messanger.data.wrap

import android.bluetooth.BluetoothDevice

sealed class ScanResource {
    class ItemFound(val value: BluetoothDevice) : ScanResource()
    object DiscoveryStarted : ScanResource()
    object DiscoveryFinished : ScanResource()
}