package com.peer_messanger.bluetoothchat

import android.bluetooth.BluetoothDevice

sealed class BTChatEvents {

    class Connected(val device: BluetoothDevice?):BTChatEvents()
    class Connecting(val device: BluetoothDevice?):BTChatEvents()
    class ConnectionFailed(val deviceName: String?):BTChatEvents()
    class Disconnect(val deviceName: String?):BTChatEvents()

    class ReceivedMessage(val message: String):BTChatEvents()
    class SendingMessage(val message: String):BTChatEvents()
}