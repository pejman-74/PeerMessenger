package com.peer_messanger.bluetoothchat

import android.bluetooth.BluetoothDevice
import com.peer_messanger.data.wrapper.ConnectionEvents
import com.peer_messanger.data.wrapper.ScanResource
import kotlinx.coroutines.flow.Flow


class FakeBluetoothChatService:BluetoothChatServiceInterface{
    override fun connectionState(): Flow<ConnectionEvents> {
        TODO("Not yet implemented")
    }

    override fun receivedMessages(): Flow<String> {
        TODO("Not yet implemented")
    }

    override suspend fun start() {
        TODO("Not yet implemented")
    }

    override fun stop() {
        TODO("Not yet implemented")
    }

    override suspend fun connect(device: BluetoothDevice) {
        TODO("Not yet implemented")
    }

    override suspend fun sendMessage(message: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun isDeviceSupportBT(): Boolean {
        TODO("Not yet implemented")
    }

    override fun bluetoothIsOn(): Boolean {
        TODO("Not yet implemented")
    }

    override fun enableBT(): Boolean {
        TODO("Not yet implemented")
    }

    override fun startDiscovery(): Boolean {
        TODO("Not yet implemented")
    }

    override fun discoveryDevices(): Flow<ScanResource> {
        TODO("Not yet implemented")
    }

    override fun pairedDevices(): List<BluetoothDevice> {
        TODO("Not yet implemented")
    }

    override fun bluetoothState(): Flow<Int> {
        TODO("Not yet implemented")
    }


}