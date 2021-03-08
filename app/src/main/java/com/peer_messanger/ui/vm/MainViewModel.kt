package com.peer_messanger.ui.vm

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.peer_messanger.data.model.BluetoothMessage
import com.peer_messanger.data.model.Device
import com.peer_messanger.data.relationship.DeviceWithMessages
import com.peer_messanger.data.repository.Repository
import com.peer_messanger.data.wrap.BluetoothEventResource
import com.peer_messanger.data.wrap.Event
import com.peer_messanger.data.wrap.ScanResource
import com.peer_messanger.ui.base.BaseViewModel
import com.peer_messanger.util.getCurrentUTCDateTime
import com.peer_messanger.util.selfUserDatabaseId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val repository: Repository) :
    BaseViewModel() {

    /**
     *   Bluetooth connectivity(on/off) status:
     *   true->on
     *   false->off
     * */
    private val _bluetoothLiveStatus = MutableLiveData<Event<Boolean>>()
    val bluetoothLiveStatus: LiveData<Event<Boolean>> get() = _bluetoothLiveStatus

    fun postBluetoothStatus(isOn: Boolean) {
        _bluetoothLiveStatus.postValue(Event(isOn))
    }


    // Founded bluetooth devices or other events..
    private val _scanDeviceResource = MutableLiveData<ScanResource>()
    val scanDeviceResource: LiveData<ScanResource> get() = _scanDeviceResource

    fun postScanDeviceResource(scanResource: ScanResource) {
        _scanDeviceResource.postValue(scanResource)
    }


    //Paired devices list
    private val _pairedDevices = MutableLiveData<List<BluetoothDevice>>()
    val pairedDevices: LiveData<List<BluetoothDevice>> get() = _pairedDevices

    fun postPairedDevices(pairedDevices: List<BluetoothDevice>) {
        _pairedDevices.postValue(pairedDevices)
    }


    // bluetooth connection status
    private val _bluetoothEventsResource = MutableLiveData<BluetoothEventResource>()
    val bluetoothEventsEventResource: LiveData<BluetoothEventResource> get() = _bluetoothEventsResource

    fun postBluetoothEventsResource(bluetoothEventResource: BluetoothEventResource) {
        _bluetoothEventsResource.postValue(bluetoothEventResource)
    }


    /**
     * get specific device with messages from home-feed flow 'allDevicesWithMessages'
     * because on app lunched, all devices and messages got for show into homeFragment
     * */
    private val _deviceWithMessages = MutableLiveData<DeviceWithMessages>()
    val deviceWithMessages: LiveData<DeviceWithMessages> get() = _deviceWithMessages

    fun getDeviceWithMessages(macAddress: String) = work {

        allDevicesWithMessages.collect { devicesWithMessages ->
            devicesWithMessages.find { it.device.macAddress == macAddress }?.let {
                _deviceWithMessages.postValue(it)
            }
        }
    }


    /**
     * load all device with messages from db for showing into homeFragment
     * TODO : in real messengers app this is not a good practice, Because maybe memory leak will occur.
     * */
    val allDevicesWithMessages =
        repository.getAllDevicesWithMessages()


    private val _readyToAcknowledgmentMessages = MutableLiveData<BluetoothMessage>()
    val readyToAcknowledgmentMessages: LiveData<BluetoothMessage> get() = _readyToAcknowledgmentMessages

    /**
     *  @param messageBody ->raw json string message
     * @param sederDeviceAddress ->sender mac address for save as messageOwner
     * After successfully saved, adding to readyToAcknowledgmentMessages for send ack message to sender
     * */
    fun saveReceivedMessage(messageBody: String, sederDeviceAddress: String?) = work {
        if (messageBody.isBlank() || sederDeviceAddress.isNullOrBlank())
            return@work
        try {
            val bluetoothMessage = Gson().fromJson(messageBody, BluetoothMessage::class.java)
            val receivedBluetoothMessage = bluetoothMessage.copy(
                messageOwner = sederDeviceAddress,
                receiverDevice = selfUserDatabaseId,
                createdTime = getCurrentUTCDateTime()
            )

            repository.saveMessage(receivedBluetoothMessage)
            _readyToAcknowledgmentMessages.postValue(receivedBluetoothMessage)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    //save user sent-message
    fun saveSendMessage(bluetoothMessage: BluetoothMessage) = work {
        if (bluetoothMessage.id.isBlank() || bluetoothMessage.body.isBlank() || bluetoothMessage.receiverDevice.isBlank())
            return@work

        repository.saveMessage(bluetoothMessage)

    }


    //Save connected devices
    fun saveDevice(macAddress: String, name: String) = work {
        if (macAddress.isBlank() || name.isBlank())
            return@work
        val device = Device(macAddress, name)

        repository.saveDevice(device)

    }


    //when ack-message received,used this function to set message isDelivered property in db
    fun setBluetoothMessageIsDelivered(messageId: String, isDelivered: Boolean) = work {
        if (messageId.isBlank())
            return@work

        repository.setBluetoothMessageIsDelivered(messageId, isDelivered)

    }


    //get undelivered-messages of specific user
    private val _unDeliveredMessages = MutableLiveData<List<BluetoothMessage>>()
    val unDeliveredMessages: LiveData<List<BluetoothMessage>> get() = _unDeliveredMessages

    fun getUnDeliveredMessages(macAddress: String) = work {
        val messages = repository.getUnDeliveredMessages(macAddress)
        _unDeliveredMessages.postValue(messages)
    }


    //get unacknowledged-messages of specific user
    private val _unacknowledgedMessages = MutableLiveData<List<BluetoothMessage>>()
    val unacknowledgedMessages: LiveData<List<BluetoothMessage>> get() = _unacknowledgedMessages

    fun getUnacknowledgedMessages(macAddress: String) = work {

        val messages = repository.getUnacknowledgedMessages(macAddress)
        _unacknowledgedMessages.postValue(messages)
    }


}
