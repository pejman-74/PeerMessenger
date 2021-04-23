package com.peer_messanger.bluetoothchat

import android.bluetooth.BluetoothDevice
import com.peer_messanger.data.wrapper.ConnectionEvents
import com.peer_messanger.data.wrapper.ScanResource
import kotlinx.coroutines.flow.Flow

interface BluetoothChatServiceInterface {
    fun connectionState(): Flow<ConnectionEvents>

    fun receivedMessages(): Flow<String>

    suspend fun start()

    fun stop()

    suspend fun connect(device: BluetoothDevice)

    suspend fun sendMessage(message: String): Boolean

    fun isDeviceSupportBT(): Boolean

    fun bluetoothIsOn():Boolean

    fun enableBT(): Boolean

    fun startDiscovery():Boolean

    fun discoveryDevices():Flow<ScanResource>

    fun pairedDevices(): List<BluetoothDevice>

    fun bluetoothState(): Flow<Int>

}