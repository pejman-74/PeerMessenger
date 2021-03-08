package com.peer_messanger.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.peer_messanger.data.model.BluetoothMessage
import com.peer_messanger.data.model.Device
import com.peer_messanger.data.relationship.DeviceWithMessages
import com.peer_messanger.data.repository.Repository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeRepository : Repository {

    private val bluetoothMessageTable = ArrayList<BluetoothMessage>()
    private val _bluetoothMessageTableLive =
        MutableLiveData<List<BluetoothMessage>>(emptyList())
    val bluetoothMessageTableLive: LiveData<List<BluetoothMessage>> get() = _bluetoothMessageTableLive

    private val deviceTable = ArrayList<Device>()
    private val _deviceTableLive = MutableLiveData<List<Device>>(emptyList())
    val deviceTableLive: LiveData<List<Device>> get() = _deviceTableLive

    override suspend fun saveMessage(bluetoothMessage: BluetoothMessage) {
        bluetoothMessageTable.add(bluetoothMessage)
        _bluetoothMessageTableLive.postValue(bluetoothMessageTable)
    }

    override suspend fun saveDevice(device: Device) {
        deviceTable.add(device)
        _deviceTableLive.postValue(deviceTable)
    }

    override suspend fun setBluetoothMessageIsDelivered(messageId: String, isDelivered: Boolean) {

        val deliverType =
            bluetoothMessageTable.find { it.id == messageId }?.copy(isDelivered = isDelivered)
        if (deliverType != null) {
            bluetoothMessageTable.removeAll { it.id == messageId }
            bluetoothMessageTable.add(deliverType)
            _bluetoothMessageTableLive.postValue(bluetoothMessageTable)
        }
    }

    override fun getAllDevicesWithMessages(): Flow<List<DeviceWithMessages>> =
        flow {
            emit(
                listOf(
                    DeviceWithMessages(
                        deviceTable.first(),
                        bluetoothMessageTable,
                        bluetoothMessageTable,
                    )
                )
            )
        }


    override suspend fun getUnDeliveredMessages(macAddress: String): List<BluetoothMessage> {
        return bluetoothMessageTable.filter { it.isDelivered == null || it.isDelivered == false }
    }

    override suspend fun getUnacknowledgedMessages(macAddress: String): List<BluetoothMessage> {
        return bluetoothMessageTable.filter { it.isDelivered == null || it.isDelivered == false }
    }


}