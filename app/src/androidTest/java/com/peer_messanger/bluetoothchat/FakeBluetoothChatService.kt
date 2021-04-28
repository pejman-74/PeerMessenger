package com.peer_messanger.bluetoothchat

import android.bluetooth.BluetoothAdapter
import android.util.Log
import com.peer_messanger.data.model.Device
import com.peer_messanger.data.wrapper.ConnectionEvents
import com.peer_messanger.data.wrapper.ScanResource
import com.peer_messanger.fakeDevices
import com.peer_messanger.util.TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.withContext


@ExperimentalCoroutinesApi
class FakeBluetoothChatService : BluetoothChatServiceInterface {



    private val connectionState = MutableStateFlow<ConnectionEvents?>(null)
    private val fakeReceivedMessageChannel = Channel<String>()
    private val bluetoothState = MutableStateFlow(BluetoothAdapter.STATE_ON)
    private var isStarted = false
    private var deviceHasBluetooth = true

    fun setDeviceHasBluetooth(has: Boolean) {
        bluetoothState.value = BluetoothAdapter.STATE_DISCONNECTED
        deviceHasBluetooth = has
    }

    suspend fun sendFakeMessage() {
        fakeReceivedMessageChannel.send("Message")
    }


    override fun connectionState(): Flow<ConnectionEvents> = connectionState.filterNotNull()

    override fun receivedMessages(): Flow<String> = fakeReceivedMessageChannel.receiveAsFlow()


    override suspend fun start() {
        isStarted = true
    }

    override fun stop() {
        isStarted = false
    }

    override suspend fun connect(macAddress: String){
        fakeDevices.forEach {
            if (it.macAddress == macAddress) {
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
        return true
    }

    override fun discoveryDevices(): Flow<ScanResource> = channelFlow {
        offer(ScanResource.DiscoveryStarted)
        fakeDevices.forEach {
            offer(ScanResource.DeviceFound(it))
        }
        offer(ScanResource.DiscoveryFinished)
    }

    override fun pairedDevices(): List<Device> = fakeDevices

    override fun bluetoothState(): Flow<Int> = bluetoothState


}