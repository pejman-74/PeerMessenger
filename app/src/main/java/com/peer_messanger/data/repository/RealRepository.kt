package com.peer_messanger.data.repository

import android.bluetooth.BluetoothDevice
import com.peer_messanger.bluetoothchat.BluetoothChatService
import com.peer_messanger.data.dao.DeviceDao
import com.peer_messanger.data.dao.MessageDao
import com.peer_messanger.data.model.BluetoothMessage
import com.peer_messanger.data.model.Device
import com.peer_messanger.data.wrapper.ConnectionEvents
import com.peer_messanger.data.wrapper.ScanResource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow

@ExperimentalCoroutinesApi
class RealRepository(
    private val deviceDao: DeviceDao,
    private val messageDao: MessageDao,
    private val chatService: BluetoothChatService,
) : Repository {
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


    override suspend fun startChatService() = chatService.start()

    override fun stopChatService() = chatService.stop()

    override suspend fun connectToDevice(device: BluetoothDevice, isSecure: Boolean) =
        chatService.connect(device)

    override fun pairedDevices(): List<BluetoothDevice> = chatService.pairedDevices()

    override fun bluetoothState(): Flow<Int> = chatService.bluetoothState()

    override fun isDeviceSupportBluetooth(): Boolean = chatService.isDeviceSupportBT()

    override fun enableBluetooth(): Boolean = chatService.enableBT()

    override fun bluetoothIsOn(): Boolean = chatService.bluetoothIsOn()

    override suspend fun sendMessage(message: String) = chatService.sendMessage(message)

    override fun startDiscovery() = chatService.startDiscovery()

    override fun discoveryDevices(): Flow<ScanResource> = chatService.discoveryDevices()

    override fun connectionState(): Flow<ConnectionEvents> = chatService.connectionState()

    override fun receivedMessages(): Flow<String> = chatService.receivedMessages()


}