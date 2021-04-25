package com.peer_messanger.bluetoothchat

import com.peer_messanger.data.model.Device
import com.peer_messanger.data.wrapper.ConnectionEvents
import com.peer_messanger.data.wrapper.ScanResource
import kotlinx.coroutines.flow.Flow

interface BluetoothChatServiceInterface {
    fun connectionState(): Flow<ConnectionEvents>

    fun receivedMessages(): Flow<String>

    suspend fun start()

    fun stop()

    suspend fun connect(macAddress: String)

    suspend fun sendMessage(message: String): Boolean

    fun isDeviceSupportBT(): Boolean

    fun bluetoothIsOn():Boolean

    fun enableBT(): Boolean

    fun startDiscovery():Boolean

    fun discoveryDevices():Flow<ScanResource>

    fun pairedDevices(): List<Device>

    fun bluetoothState(): Flow<Int>

}