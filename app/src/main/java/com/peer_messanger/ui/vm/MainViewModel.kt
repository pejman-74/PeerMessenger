package com.peer_messanger.ui.vm

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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@ExperimentalCoroutinesApi
class MainViewModel @Inject constructor(
    private val localRepository: LocalRepositoryInterface,
    private val chatService: BluetoothChatServiceInterface,
    private val mainCoroutineDispatcher: CoroutineDispatcher
) : ViewModel() {


    /**
     * get specific device with messages from home-feed flow [allDevicesWithMessages]
     * because on app lunched, all devices and messages got for show into homeFragment
     * */
    private val deviceWithMessagesChanel = Channel<String>()
    private val deviceWithMessagesFlow = deviceWithMessagesChanel.receiveAsFlow()

    val deviceWithMessages: Flow<DeviceWithMessages?> =
        deviceWithMessagesFlow.flatMapLatest { macAddress ->
            allDevicesWithMessages.map { devWithMessage ->
                devWithMessage.find { it.device.macAddress == macAddress }
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    fun getDeviceWithMessages(macAddress: String) = viewModelScope.launch(mainCoroutineDispatcher) {
        deviceWithMessagesChanel.send(macAddress)
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
    private fun saveReceivedMessage(messageBody: String, sederDeviceAddress: String?) =
        viewModelScope.launch(mainCoroutineDispatcher) {
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
    fun saveSendMessage(bluetoothMessage: BluetoothMessage) =
        viewModelScope.launch(mainCoroutineDispatcher) {
            localRepository.saveMessage(bluetoothMessage)
        }


    //Save connected devices
    fun saveDevice(device: Device) = viewModelScope.launch(mainCoroutineDispatcher) {
        localRepository.saveDevice(device)
    }


    //when ack-message received,used this function to set message isDelivered property in db
    private fun setBluetoothMessageIsDelivered(messageId: String, isDelivered: Boolean) =
        viewModelScope.launch(mainCoroutineDispatcher) {
            localRepository.setBluetoothMessageIsDelivered(messageId, isDelivered)
        }


    private fun processUnDeliveredMessages(macAddress: String) =
        viewModelScope.launch(mainCoroutineDispatcher) {
            val messages = localRepository.getUnDeliveredMessages(macAddress)
            //send to connected device undelivered messages
            messages.forEach { message ->
                sendUnDeliverMessage(message)
            }

        }

    private fun sendUnDeliverMessage(bluetoothMessage: BluetoothMessage) {
        //convert to gson for make serializable easier
        val gsonStringMessage = Gson().toJson(bluetoothMessage)
        viewModelScope.launch(mainCoroutineDispatcher) {
            val isSent = sendMessageBt(gsonStringMessage)
            if (isSent)
                setBluetoothMessageIsDelivered(bluetoothMessage.id, true)
        }
    }

    //send to connected device unacknowledged-messages
    private fun precessUnacknowledgedMessages(macAddress: String) =
        viewModelScope.launch(mainCoroutineDispatcher) {

            val messages = localRepository.getUnacknowledgedMessages(macAddress)
            messages.forEach { message ->
                sendAckMessage(message.id)
            }

        }

    private suspend fun sendAckMessage(messageId: String) {
        //create ack message
        val ackMessage = "ack=$messageId"

        //send ack message
        val isSent = sendMessageBt(ackMessage)

        //set message status
        if (isSent)
            setBluetoothMessageIsDelivered(messageId, true)
    }

    fun sendMessage(messageBody: String, macAddress: String) =
        viewModelScope.launch(mainCoroutineDispatcher) {

            //create a BluetoothMessage
            val message = BluetoothMessage(
                getCurrentUTCDateTime(), messageBody, selfUserDatabaseId, macAddress,
                getCurrentUTCDateTime(), false
            )
            //save message to db
            saveSendMessage(message)
            //convert to gson for make serializable easier
            val gsonStringMessage = Gson().toJson(message)

            sendMessageBt(gsonStringMessage)


        }


    fun startChatService() = viewModelScope.launch(mainCoroutineDispatcher) { chatService.start() }

    fun stopChatService() = viewModelScope.launch(mainCoroutineDispatcher) { chatService.stop() }

    fun connectToDevice(macAddress: String) = viewModelScope.launch(mainCoroutineDispatcher) {
        chatService.connect(macAddress)
    }

    private suspend fun sendMessageBt(message: String) = chatService.sendMessage(message)

    fun pairedDevices(): List<Device> = chatService.pairedDevices()

    fun isDeviceSupportBluetooth(): Boolean = chatService.isDeviceSupportBT()

    fun enableBT(): Boolean = chatService.enableBT()

    fun btIsOn(): Boolean = chatService.bluetoothIsOn()

    private lateinit var lastConnectedDevice: Device

    val connectionState = chatService.connectionState().map {
        if (it is ConnectionEvents.Connected)
            lastConnectedDevice = it.device
        it
    }

    private val receivedMessages = chatService.receivedMessages()

    fun startScan() = chatService.startDiscovery()

    val scanFlow = chatService.discoveryDevices()


    init {

        viewModelScope.launch(mainCoroutineDispatcher) {
            receivedMessages.collect {
                if (it.startsWith("ack=")) {
                    val messageId = it.substringAfter("=")
                    //set message is IsDelivered true
                    if (messageId.isNotBlank())
                        setBluetoothMessageIsDelivered(messageId, true)
                } else {
                    //save received message to db
                    saveReceivedMessage(it, lastConnectedDevice.macAddress)
                }
            }

        }

        viewModelScope.launch(mainCoroutineDispatcher) {
            connectionState.collect {
                if (it is ConnectionEvents.Connected) {
                    //save connected user to database
                    saveDevice(it.device)

                    processUnDeliveredMessages(it.device.macAddress)
                    precessUnacknowledgedMessages(it.device.macAddress)
                }
            }
        }


    }


}
