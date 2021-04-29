package com.peer_messanger.bluetoothchat

import android.bluetooth.BluetoothAdapter
import com.peer_messanger.data.model.Device
import com.peer_messanger.data.wrapper.ConnectionEvents
import com.peer_messanger.data.wrapper.ScanResource
import com.peer_messanger.fakeDevices
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*


@ExperimentalCoroutinesApi
class FakeBluetoothChatService : BluetoothChatServiceInterface {


    private val connectionState = MutableStateFlow<ConnectionEvents>(ConnectionEvents.Disconnect())
    private val fakeReceivedMessageChannel = Channel<String>()
    private val fakeFinedDevice = MutableStateFlow<ScanResource?>(null)
    private val bluetoothState = MutableStateFlow(BluetoothAdapter.STATE_ON)
    private var isStarted = false
    private var deviceHasBluetooth = true
    private var hasPairedDevice = false


    fun setHasPairedDevice(has: Boolean) {
        hasPairedDevice = has
    }

    fun setDeviceHasBluetooth(has: Boolean) {
        bluetoothState.value = BluetoothAdapter.STATE_DISCONNECTED
        deviceHasBluetooth = has
    }

    suspend fun sendFakeMessage() {
        fakeReceivedMessageChannel.send("{ id:'id',body:'hello' }")
    }


    override fun connectionState(): Flow<ConnectionEvents> = connectionState

    override fun receivedMessages(): Flow<String> = fakeReceivedMessageChannel.receiveAsFlow()


    override suspend fun start() {
        isStarted = true
    }

    override fun stop() {
        connectionState.value = ConnectionEvents.ConnectionFailed
        isStarted = false
    }

    override suspend fun connect(macAddress: String){
        fakeDevices.forEach {
            if (it.macAddress == macAddress && isStarted) {
                connectionState.emit(ConnectionEvents.Connected(it))
                return
            }
        }
        connectionState.emit(ConnectionEvents.ConnectionFailed)
    }

    override suspend fun sendMessage(message: String): Boolean {
        return connectionState.first() is ConnectionEvents.Connected
    }

    override fun isDeviceSupportBT(): Boolean = deviceHasBluetooth

    override fun bluetoothIsOn(): Boolean {
        return bluetoothState.value == BluetoothAdapter.STATE_ON
    }

    override fun enableBT(): Boolean {
        bluetoothState.value = BluetoothAdapter.STATE_ON
        return true
    }

    override fun startDiscovery(): Boolean {
        fakeFinedDevice.value = ScanResource.DiscoveryStarted
        fakeDevices.forEach {
            fakeFinedDevice.value = ScanResource.DeviceFound(it)
        }
        fakeFinedDevice.value = ScanResource.DiscoveryFinished
        return true
    }

    override fun discoveryDevices(): Flow<ScanResource> = fakeFinedDevice.filterNotNull()

    override fun pairedDevices(): List<Device> =
        if (hasPairedDevice) fakeDevices else emptyList()

    override fun bluetoothState(): Flow<Int> = bluetoothState


}