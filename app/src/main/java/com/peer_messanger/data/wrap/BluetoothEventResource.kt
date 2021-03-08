package com.peer_messanger.data.wrap


sealed class BluetoothEventResource {
    object DeviceConnected : BluetoothEventResource()
    object DeviceConnecting: BluetoothEventResource()
    object DeviceDisconnected : BluetoothEventResource()
}