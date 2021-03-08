package com.peer_messanger.ui.listener

import android.bluetooth.BluetoothDevice

interface BluetoothDeviceItemListener {
    fun onClick(bluetoothDevice: BluetoothDevice)
}