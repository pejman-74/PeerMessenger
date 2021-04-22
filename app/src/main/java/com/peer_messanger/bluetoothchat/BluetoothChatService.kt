package com.peer_messanger.bluetoothchat

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import com.peer_messanger.bluetoothchat.blueflow.BlueFlow
import com.peer_messanger.bluetoothchat.blueflow.BlueFlowIO
import com.peer_messanger.data.wrapper.ConnectionEvents
import com.peer_messanger.util.TAG
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

@ExperimentalCoroutinesApi
class BluetoothChatService(private val blueFlow: BlueFlow) {

    companion object {
        // Unique UUID for this application
        private val MY_UUID = UUID.fromString("29621b37-e817-485a-a258-52da5261421a")

        // Name for the SDP record when creating server socket
        private const val SERVER_NAME = "peerMessengerServer"
    }

    private var blueFlowIO: BlueFlowIO? = null

    private val _connectionState = MutableSharedFlow<ConnectionEvents>()

    private val connectionState: Flow<ConnectionEvents> = channelFlow {
        launch {
            //Just for get disconnect Event
            blueFlow.aclEvents().collect {
                offer(it)
                if (bluetoothIsOn())
                    restart()
                else
                    stop()
            }
        }
        launch {
            //Get other Events
            _connectionState.collect {
                offer(it)
            }
        }
    }

    fun connectionState() = connectionState


    fun receivedMessages(): Flow<String> = connectionState.flatMapLatest {

        //read byte arrays just when connected
        if (it is ConnectionEvents.Connected && blueFlowIO != null) {
            blueFlowIO!!.readByteAsString()
        } else
            emptyFlow()
    }

    suspend fun start() {

        //starting a server for listening
        val serverSocket =
            blueFlow.connectAsServerAsync(SERVER_NAME, MY_UUID, true).await()
        setBlueFlowIO(serverSocket)
    }

    fun stop() {
        blueFlowIO?.closeConnections()
        blueFlowIO = null
    }

    private suspend fun restart() {
        stop()
        start()
    }

    private suspend fun setBlueFlowIO(bluetoothSocket: BluetoothSocket) {
        runCatching {
            blueFlowIO = blueFlow.getIO(bluetoothSocket)
            _connectionState.emit(ConnectionEvents.Connected(bluetoothSocket.remoteDevice))
        }.onFailure {
            _connectionState.emit(ConnectionEvents.Disconnect(bluetoothSocket.remoteDevice.name))
        }
    }

    private fun isDeviceEarlyConnected(device: BluetoothDevice): Boolean {
        blueFlowIO?.getBluetoothDevice()?.let {
            if (it.address == device.address) {
                return true
            }
        }
        return false
    }

    suspend fun connect(device: BluetoothDevice) {

        if (isDeviceEarlyConnected(device)) {
            _connectionState.emit(ConnectionEvents.Connected(device))
            return
        }

        stop()

        val clientSocket = runCatching {
            blueFlow.connectAsClientAsync(device, MY_UUID, true).await()
        }.getOrElse {
            restart()
            return
        }

        setBlueFlowIO(clientSocket)
    }

    suspend fun sendMessage(message: String): Boolean {
        blueFlowIO?.let {
            return it.send(message)
        }
        Log.e(TAG, "sendMessage: blueFlowIO is Empty")
        return false
    }

    fun isDeviceSupportBT() = blueFlow.isBluetoothAvailable()

    fun bluetoothIsOn() = blueFlow.isBluetoothEnabled()

    fun enableBT(): Boolean = blueFlow.enable()

    fun startDiscovery() = blueFlow.startDiscovery()

    fun discoveryDevices() = blueFlow.discoverDevices()

    fun pairedDevices(): List<BluetoothDevice> = blueFlow.bondedDevices()?.toList() ?: emptyList()

    fun bluetoothState(): Flow<Int> = blueFlow.bluetoothState()

}