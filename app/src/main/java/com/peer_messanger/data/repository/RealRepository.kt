package com.peer_messanger.data.repository

import androidx.room.withTransaction
import com.peer_messanger.data.local.database.AppDatabase
import com.peer_messanger.data.model.BluetoothMessage
import com.peer_messanger.data.model.Device
import javax.inject.Inject

class RealRepository @Inject constructor(private val db: AppDatabase) : Repository {
    override suspend fun saveMessage(bluetoothMessage: BluetoothMessage) {
        db.messageDao().insert(bluetoothMessage)
    }

    override suspend fun saveDevice(device: Device) {
        db.deviceDao().insert(device)
    }

    override suspend fun setBluetoothMessageIsDelivered(messageId: String, isDelivered: Boolean) =
        db.messageDao().setIsDelivered(messageId, isDelivered)

    override fun getAllDevicesWithMessages() = db.deviceDao().getAllDevicesWithMessages()

    override suspend fun getUnDeliveredMessages(macAddress: String) =
        db.messageDao().getUnDeliveredMessages(macAddress)

    override suspend fun getUnacknowledgedMessages(macAddress: String) =
        db.messageDao().getUnacknowledgedMessages(macAddress)




}