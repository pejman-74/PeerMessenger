package com.peer_messanger.data.repository

import com.peer_messanger.data.dao.DeviceDao
import com.peer_messanger.data.dao.MessageDao
import com.peer_messanger.data.model.BluetoothMessage
import com.peer_messanger.data.model.Device
import kotlinx.coroutines.ExperimentalCoroutinesApi

class LocalRepository(
    private val deviceDao: DeviceDao,
    private val messageDao: MessageDao
) : LocalRepositoryInterface {
    override suspend fun saveMessage(bluetoothMessage: BluetoothMessage) {
        messageDao.insert(bluetoothMessage)
    }

    override suspend fun saveDevice(device: Device) {
        deviceDao.insert(device)
    }

    override suspend fun setBluetoothMessageIsDelivered(messageId: String, isDelivered: Boolean) =
        messageDao.setIsDelivered(messageId, isDelivered)

    override fun getAllDevicesWithMessages() = deviceDao.getAllDevicesWithMessages()

    override suspend fun getUnDeliveredMessages(macAddress: String) =
        messageDao.getUnDeliveredMessages(macAddress)

    override suspend fun getUnacknowledgedMessages(macAddress: String) =
        messageDao.getUnacknowledgedMessages(macAddress)


}