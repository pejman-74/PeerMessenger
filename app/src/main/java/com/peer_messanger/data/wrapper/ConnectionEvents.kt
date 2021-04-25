package com.peer_messanger.data.wrapper

import com.peer_messanger.data.model.Device

sealed class ConnectionEvents {
    class Connected(val device: Device) : ConnectionEvents()
    object ConnectionFailed : ConnectionEvents()
    class Disconnect(val deviceName: String? = null) : ConnectionEvents()
}