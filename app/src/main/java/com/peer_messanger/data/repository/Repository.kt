package com.peer_messanger.data.repository

import com.peer_messanger.data.model.BluetoothMessage
import com.peer_messanger.data.model.Device
import com.peer_messanger.data.relationship.DeviceWithMessages
import kotlinx.coroutines.flow.Flow


interface Repository {

    suspend fun saveMessage(bluetoothMessage: BluetoothMessage)

    suspend fun saveDevice(device: Device)

    suspend fun setBluetoothMessageIsDelivered(messageId: String, isDelivered: Boolean)

    fun getAllDevicesWithMessages(): Flow<List<DeviceWithMessages>>

    suspend fun getUnDeliveredMessages(macAddress: String): List<BluetoothMessage>

    suspend fun getUnacknowledgedMessages(macAddress: String): List<BluetoothMessage>


}

