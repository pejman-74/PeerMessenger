package com.peer_messanger

import com.peer_messanger.data.model.BluetoothMessage
import com.peer_messanger.data.model.Device

val fakeDevices = listOf(
    Device("macAddress", "android device"),
    Device("macAddress", "tv device", false)
)


val receivedBtMessage =
    BluetoothMessage("rId", "rBody", "macAddress", "0", "create time", false)
val sentBtMessage =
    BluetoothMessage("sId", "SBody", "0", "macAddress", "create time", false)