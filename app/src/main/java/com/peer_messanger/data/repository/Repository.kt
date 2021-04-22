package com.peer_messanger.data.repository

import android.bluetooth.BluetoothDevice
import com.peer_messanger.data.wrapper.ConnectionEvents
import com.peer_messanger.data.model.BluetoothMessage
import com.peer_messanger.data.model.Device
import com.peer_messanger.data.relationship.DeviceWithMessages
import com.peer_messanger.data.wrapper.ScanResource
import kotlinx.coroutines.flow.Flow


interface Repository {

    suspend fun saveMessage(bluetoothMessage: BluetoothMessage)

    suspend fun saveDevice(device: Device)

    suspend fun setBluetoothMessageIsDelivered(messageId: String, isDelivered: Boolean)

    fun getAllDevicesWithMessages(): Flow<List<DeviceWithMessages>>

    suspend fun getUnDeliveredMessages(macAddress: String): List<BluetoothMessage>

    suspend fun getUnacknowledgedMessages(macAddress: String): List<BluetoothMessage>

    suspend fun startChatService()
    fun stopChatService()
    suspend fun connectToDevice(device: BluetoothDevice, isSecure: Boolean)
    fun pairedDevices(): List<BluetoothDevice>
    fun bluetoothState():Flow<Int>
    fun isDeviceSupportBluetooth(): Boolean
    fun enableBluetooth(): Boolean
    fun bluetoothIsOn():Boolean
    suspend fun sendMessage(message: String): Boolean
    fun startDiscovery(): Boolean
    fun discoveryDevices(): Flow<ScanResource>
    fun connectionState(): Flow<ConnectionEvents>
    fun receivedMessages(): Flow<String>
}

