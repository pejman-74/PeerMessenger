package com.peer_messanger.ui.vm

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.peer_messanger.bluetoothchat.BluetoothChatServiceInterface
import com.peer_messanger.data.model.BluetoothMessage
import com.peer_messanger.data.model.Device
import com.peer_messanger.data.relationship.DeviceWithMessages
import com.peer_messanger.data.repository.LocalRepositoryInterface
import com.peer_messanger.data.wrapper.ConnectionEvents
import com.peer_messanger.util.getCurrentUTCDateTime
import com.peer_messanger.util.selfUserDatabaseId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val localRepository: LocalRepositoryInterface,
    private val chatService: BluetoothChatServiceInterface
) : ViewModel() {


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
     * load all device with messages from db
     * */
    val allDevicesWithMessages =
        localRepository.getAllDevicesWithMessages()


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

                localRepository.saveMessage(receivedBluetoothMessage)

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
        localRepository.saveMessage(bluetoothMessage)
    }


    //Save connected devices
    fun saveDevice(macAddress: String, name: String) = viewModelScope.launch {
        if (macAddress.isBlank() || name.isBlank())
            return@launch
        val device = Device(macAddress, name)

        localRepository.saveDevice(device)
    }


    //when ack-message received,used this function to set message isDelivered property in db
    fun setBluetoothMessageIsDelivered(messageId: String, isDelivered: Boolean) =
        viewModelScope.launch {
            if (messageId.isBlank())
                return@launch
            localRepository.setBluetoothMessageIsDelivered(messageId, isDelivered)

        }


    fun processUnDeliveredMessages(macAddress: String) = viewModelScope.launch {
        val messages = localRepository.getUnDeliveredMessages(macAddress)
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
    fun precessUnacknowledgedMessages(macAddress: String) = viewModelScope.launch {

        val messages = localRepository.getUnacknowledgedMessages(macAddress)
        messages.forEach { message ->
            val isSent = sendAckMessage(message.id)
            //set message status
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


    fun startChatService() = viewModelScope.launch { chatService.start() }

    fun stopChatService() = viewModelScope.launch { chatService.stop() }

    fun connectToDevice(device: BluetoothDevice) = viewModelScope.launch {
        chatService.connect(device)
    }

    private suspend fun sendMessageBt(message: String) = chatService.sendMessage(message)

    fun pairedDevices(): List<BluetoothDevice> = chatService.pairedDevices()

    fun isDeviceSupportBluetooth(): Boolean = chatService.isDeviceSupportBT()

    fun enableBT(): Boolean = chatService.enableBT()

    fun btIsOn(): Boolean = chatService.bluetoothIsOn()

    lateinit var lastConnectedDevice: BluetoothDevice

    val connectionState = chatService.connectionState().map {
        if (it is ConnectionEvents.Connected)
            lastConnectedDevice = it.device
        it
    }

    private val receivedMessages = chatService.receivedMessages()

    fun startScan() = chatService.startDiscovery()

    val scanFlow = chatService.discoveryDevices()

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
                            it.device.also { device ->
                                if (device.address.isNotBlank() && !device.name.isNullOrBlank())
                                    saveDevice(device.address, device.name)
                            }

                            processUnDeliveredMessages(it.device.address)

                            precessUnacknowledgedMessages(it.device.address)
                        }
                        is ConnectionEvents.Disconnect -> {
                        }
                    }

                }
            }

        }

    }

}
