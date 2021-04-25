package com.peer_messanger.data.wrapper

import com.peer_messanger.data.model.Device

sealed class ScanResource {
    class DeviceFound(val device: Device) : ScanResource()
    object DiscoveryStarted : ScanResource()
    object DiscoveryFinished : ScanResource()
}