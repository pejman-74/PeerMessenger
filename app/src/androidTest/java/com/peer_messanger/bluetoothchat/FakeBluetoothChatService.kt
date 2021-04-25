package com.peer_messanger.bluetoothchat

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import com.peer_messanger.data.wrapper.ConnectionEvents
import com.peer_messanger.data.wrapper.ScanResource

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*


@ExperimentalCoroutinesApi
class FakeBluetoothChatService : BluetoothChatServiceInterface {

    private val connectionState = MutableSharedFlow<ConnectionEvents>()
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


    override fun connectionState(): Flow<ConnectionEvents> = connectionState

    override fun receivedMessages(): Flow<String> = fakeReceivedMessageChannel.receiveAsFlow()


    override suspend fun start() {
        isStarted = true
    }

    override fun stop() {
        isStarted = false
    }

    override suspend fun connect(device: BluetoothDevice) {
        connectionState.emit(ConnectionEvents.Connected(device))
    }

    override suspend fun sendMessage(message: String): Boolean {
        return isStarted
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
       // val fakeBluetoothDevice = mockk<BluetoothDevice>()
        //offer(ScanResource.DeviceFound(fakeBluetoothDevice))
        offer(ScanResource.DiscoveryFinished)
    }

    override fun pairedDevices(): List<BluetoothDevice> {
       // val fakeBluetoothDevice = mockk<BluetoothDevice>()

        return emptyList()
    }


    override fun bluetoothState(): Flow<Int> = bluetoothState


}