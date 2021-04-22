package com.peer_messanger.ui.vm

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.peer_messanger.data.model.BluetoothMessage
import com.peer_messanger.data.model.Device
import com.peer_messanger.data.relationship.DeviceWithMessages
import com.peer_messanger.data.repository.Repository
import com.peer_messanger.data.wrapper.ConnectionEvents
import com.peer_messanger.util.getCurrentUTCDateTime
import com.peer_messanger.util.selfUserDatabaseId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val repository: Repository) :
    ViewModel() {


    /**
     * get specific device with messages from home-feed flow 'allDevicesWithMessages'
     * because on app lunched, all devices and messages got for show into homeFragment
     * */
    private val _deviceWithMessages = MutableLiveData<DeviceWithMessages>()
    val deviceWithMessages: LiveData<DeviceWithMessages> get() = _deviceWithMessages

    fun getDeviceWithMessages(macAddress: String) = viewModelScope.launch {

        allDevicesWithMessages.collect { devicesWithMessages ->
            devicesWithMessages.find { it.device.macAddress == macAddress }?.let {
                _deviceWithMessages.postValue(it)
            }
        }
    }


    /**
     * load all device with messages from db for showing into homeFragment
     * */
    val allDevicesWithMessages =
        repository.getAllDevicesWithMessages()


    /**
     *  @param messageBody ->raw json string message
     * @param sederDeviceAddress ->sender mac address for save as messageOwner
     * After successfully saved, adding to readyToAcknowledgmentMessages for send ack message to sender
     * */
    fun saveReceivedMessage(messageBody: String, sederDeviceAddress: String?) =
        viewModelScope.launch {
            if (messageBody.isBlank() || sederDeviceAddress.isNullOrBlank())
                return@launch
            try {
                val bluetoothMessage = Gson().fromJson(messageBody, BluetoothMessage::class.java)
                val receivedBluetoothMessage = bluetoothMessage.copy(
                    messageOwner = sederDeviceAddress,
                    receiverDevice = selfUserDatabaseId,
                    createdTime = getCurrentUTCDateTime()
                )

                repository.saveMessage(receivedBluetoothMessage)

                //send ack messages to connected device
                sendAckMessage(receivedBluetoothMessage.id)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


    //save user sent-message
    fun saveSendMessage(bluetoothMessage: BluetoothMessage) = viewModelScope.launch {
        if (bluetoothMessage.id.isBlank() || bluetoothMessage.body.isBlank() || bluetoothMessage.receiverDevice.isBlank())
            return@launch
        repository.saveMessage(bluetoothMessage)
    }


    //Save connected devices
    fun saveDevice(macAddress: String, name: String) = viewModelScope.launch {
        if (macAddress.isBlank() || name.isBlank())
            return@launch
        val device = Device(macAddress, name)

        repository.saveDevice(device)
    }


    //when ack-message received,used this function to set message isDelivered property in db
    fun setBluetoothMessageIsDelivered(messageId: String, isDelivered: Boolean) =
        viewModelScope.launch {
            if (messageId.isBlank())
                return@launch
            repository.setBluetoothMessageIsDelivered(messageId, isDelivered)

        }


    fun getUnDeliveredMessages(macAddress: String) = viewModelScope.launch {
        val messages = repository.getUnDeliveredMessages(macAddress)
        //send to connected device undelivered messages
        messages.forEach { message ->
            sendUnDeliverMessage(message)
        }

    }

    private fun sendUnDeliverMessage(bluetoothMessage: BluetoothMessage) {
        //convert to gson for make serializable easier
        val gsonStringMessage = Gson().toJson(bluetoothMessage)
        viewModelScope.launch {
            val isSent = sendMessageBt(gsonStringMessage)
            if (isSent)
                setBluetoothMessageIsDelivered(bluetoothMessage.id, true)
        }
    }

    //send to connected device unacknowledged-messages
    fun getUnacknowledgedMessages(macAddress: String) = viewModelScope.launch {

        val messages = repository.getUnacknowledgedMessages(macAddress)
        messages.forEach { message ->
            val isSent = sendAckMessage(message.id)
            //set unacknowledged-messages status to acknowledged
            if (isSent)
                setBluetoothMessageIsDelivered(message.id, true)
        }

    }

    private suspend fun sendAckMessage(messageId: String): Boolean {
        //create ack message
        val ackMessage = "ack=$messageId"

        return sendMessageBt(ackMessage)
    }

    fun sendMessage(messageBody: String, macAddress: String) {

        //create a BluetoothMessage
        val message = BluetoothMessage(
            getCurrentUTCDateTime(), messageBody, selfUserDatabaseId, macAddress,
            getCurrentUTCDateTime(), false
        )
        //save message to db
        saveSendMessage(message)
        //convert to gson for make serializable easier
        val gsonStringMessage = Gson().toJson(message)
        viewModelScope.launch {
            sendMessageBt(gsonStringMessage)
        }

    }


    fun startChatService() = viewModelScope.launch { repository.startChatService() }

    fun stopChatService() = viewModelScope.launch { repository.stopChatService() }

    fun connectToDevice(device: BluetoothDevice, isSecure: Boolean) = viewModelScope.launch {
        repository.connectToDevice(device, isSecure)
    }

    private suspend fun sendMessageBt(message: String) = repository.sendMessage(message)

    fun getPairedDevices(): List<BluetoothDevice> = repository.pairedDevices()

    fun isDeviceSupportBT(): Boolean = repository.isDeviceSupportBluetooth()

    fun enableBT(): Boolean = repository.enableBluetooth()

    fun btIsOn(): Boolean = repository.bluetoothIsOn()

    lateinit var lastConnectedDevice: BluetoothDevice

    val connectionState = repository.connectionState().map {
        if (it is ConnectionEvents.Connected)
            lastConnectedDevice = it.device
        it
    }

    private val receivedMessages = repository.receivedMessages()
    fun startScan() = repository.startDiscovery()

    val scanFlow = repository.discoveryDevices()

    init {
        viewModelScope.launch {
            launch {
                receivedMessages.collect {
                    if (it.startsWith("ack=")) {
                        val messageId = it.substringAfter("=")
                        //set message is IsDelivered true
                        if (messageId.isNotBlank())
                            setBluetoothMessageIsDelivered(messageId, true)
                    } else {
                        //save received message to db
                        saveReceivedMessage(it, lastConnectedDevice.address)
                    }
                }

            }

            launch {
                connectionState.collect {
                    when (it) {
                        is ConnectionEvents.Connected -> {
                            //save connected user to database
                            lastConnectedDevice.also { device ->
                                if (device.address.isNotBlank() && !device.name.isNullOrBlank())
                                    saveDevice(device.address, device.name)
                            }

                            getUnDeliveredMessages(lastConnectedDevice.address)

                            getUnacknowledgedMessages(lastConnectedDevice.address)
                        }
                        is ConnectionEvents.Disconnect -> {
                        }
                    }

                }
            }

        }

    }

}
