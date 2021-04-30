package com.peer_messanger.data.relationship

import androidx.room.Embedded
import androidx.room.Relation
import com.peer_messanger.data.model.BluetoothMessage
import com.peer_messanger.data.model.Device

data class DeviceWithMessages(
    @Embedded
    val device: Device,

    @Relation(parentColumn = "macAddress", entityColumn = "messageOwner")
    val receivedBluetoothMessages: List<BluetoothMessage>,

    @Relation(parentColumn = "macAddress", entityColumn = "receiverDevice")
    val sentBluetoothMessages: List<BluetoothMessage>

) {
    val lastMessage
        get() = receivedBluetoothMessages.plus(sentBluetoothMessages).maxByOrNull { it.createdTime }
}


