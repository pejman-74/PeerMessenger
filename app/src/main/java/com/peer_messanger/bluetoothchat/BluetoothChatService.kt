package com.peer_messanger.bluetoothchat


import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import kotlin.coroutines.CoroutineContext


class BluetoothChatService(private val ioDispatcher: CoroutineDispatcher) {

    // Member fields
    private var mAdapter: BluetoothAdapter? = null

    private var secureAcceptThread: AcceptThread? = null
    private var insecureAcceptThread: AcceptThread? = null
    private var connectThread: ConnectThread? = null
    private var connectedThread: ConnectedThread? = null
    private var mState: Int = 0
    private val eventFlow = MutableSharedFlow<BTChatEvents>()


    companion object {
        private const val TAG: String = "BluetoothChatService"

        // Unique UUID for this application
        private val MY_UUID_SECURE = UUID.fromString("29621b37-e817-485a-a258-52da5261421a")
        private val MY_UUID_INSECURE = UUID.fromString("d620cd2b-e0a4-435b-b02e-40324d57195b")


        // Name for the SDP record when creating server socket
        private const val NAME_SECURE = "BluetoothChatSecure"
        private const val NAME_INSECURE = "BluetoothChatInsecure"

        // Constants that indicate the current connection state

        const val STATE_NONE = 0       // we're doing nothing
        const val STATE_LISTEN = 1     // now listening for incoming connections
        const val STATE_CONNECTING = 2 // now initiating an outgoing connection
        const val STATE_CONNECTED = 3  // now connected to a remote device
    }

    init {
        mAdapter = BluetoothAdapter.getDefaultAdapter()
        mState = STATE_NONE
    }


    fun getEventFlow(): SharedFlow<BTChatEvents> =
        eventFlow.asSharedFlow()


    /**
     * Return the current connection state.
     */
    @Synchronized
    fun getState(): Int {
        return mState
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    @Synchronized
    fun start() {
        Log.d(TAG, "start")

        // Cancel any thread attempting to make a connection
        if (connectThread != null) {
            connectThread?.done()
            connectThread = null
        }

        // Cancel any thread currently running a connection
        if (connectedThread != null) {
            connectedThread?.done()
            connectedThread = null
        }

        // Start the thread to listen on a BluetoothServerSocket

        if (secureAcceptThread == null) {
            secureAcceptThread = AcceptThread(true)
            secureAcceptThread?.start()
        } else {
            secureAcceptThread?.start()
        }

        if (insecureAcceptThread == null) {
            insecureAcceptThread = AcceptThread(false)
            insecureAcceptThread?.start()
        } else {
            insecureAcceptThread?.start()
        }

    }


    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * @param device The BluetoothDevice to connect
     * *
     * @param isSecure Socket Security type - Secure (true) , Insecure (false)
     */
    @Synchronized
    fun connect(device: BluetoothDevice, isSecure: Boolean) {

        connectThread?.let {
            //Check the device is now connected
            if (it.mmDevice.address == device.address && it.secure == isSecure) {
                eventFlow.tryEmit(BTChatEvents.Connected(device))
                return
            }

            // Cancel any thread attempting to make a connection
            it.done()
            connectThread = null
        }


        // Cancel any thread currently running a connection
        if (connectedThread != null) {
            connectedThread?.done()
            connectedThread = null
        }

        // Start the thread to connect with the given device
        connectThread = ConnectThread(device, isSecure)
        connectThread?.connect()

    }


    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     * @param socket The BluetoothSocket on which the connection was made
     * *
     * @param device The BluetoothDevice that has been connected
     */
    @Synchronized
    private suspend fun connected(socket: BluetoothSocket, device: BluetoothDevice?) {

        // Cancel the thread that completed the connection
        if (connectThread != null) {
            connectThread?.done()
            connectThread = null
        }

        // Cancel any thread currently running a connection
        if (connectedThread != null) {
            connectedThread?.done()
            connectedThread = null
        }

        insecureAcceptThread = null
        secureAcceptThread = null

        // Start the thread to manage the connection and perform transmissions
        connectedThread = ConnectedThread(socket)
        connectedThread?.startListening()

        // Send the name of the connected device
        eventFlow.emit(BTChatEvents.Connected(device))
    }

    /**
     * Stop all threads
     */
    @Synchronized
    fun stop() {

        if (connectThread != null) {
            connectThread?.done()
            connectThread = null
        }

        if (connectedThread != null) {
            connectedThread?.done()
            connectedThread = null
        }

        if (secureAcceptThread != null) {
            secureAcceptThread?.done()
            secureAcceptThread = null
        }

        if (insecureAcceptThread != null) {
            insecureAcceptThread?.done()
            insecureAcceptThread = null
        }
        mState = STATE_NONE

    }

    // Write to the ConnectedThread
    @Synchronized
    fun sendMessage(message: String) {
        connectedThread?.write(message)
    }


    /**
     * Indicate that the connection attempt failed
     */
    private suspend fun connectionFailed(deviceName: String?) {

        eventFlow.emit(BTChatEvents.ConnectionFailed(deviceName))

        mState = STATE_NONE

        // Start the service over to restart listening mode
        this@BluetoothChatService.start()
    }

    /**
     * Indicate that the connection was lost
     */
    private suspend fun connectionLost(deviceName: String?) {
        eventFlow.emit(BTChatEvents.Disconnect(deviceName))

        mState = STATE_NONE

        // Start the service over to restart listening mode
        this@BluetoothChatService.start()
    }


    /**
     * This class runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private inner class AcceptThread(secure: Boolean) : CoroutineScope {

        private val job = Job()
        override val coroutineContext: CoroutineContext
            get() = job + ioDispatcher


        // The local server socket
        private val mmServerSocket: BluetoothServerSocket? = try {
            mState = STATE_LISTEN

            // Create a new listening server socket
            if (secure) {
                mAdapter?.listenUsingRfcommWithServiceRecord(NAME_SECURE, MY_UUID_SECURE)
            } else {
                mAdapter?.listenUsingInsecureRfcommWithServiceRecord(
                    NAME_INSECURE, MY_UUID_INSECURE
                )
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }

        fun start() = launch {

            var socket: BluetoothSocket?

            // Listen to the server socket if we're not connected
            while (mState != STATE_CONNECTED) {
                socket = runCatching {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    mmServerSocket?.accept()
                }.onFailure {
                    it.printStackTrace()
                }.getOrNull()

                // If a connection was accepted
                if (socket != null) {
                    synchronized(this@BluetoothChatService) {
                        when (mState) {
                            STATE_LISTEN, STATE_CONNECTING -> {
                                // Situation normal. Start the connected thread.
                                launch {
                                    connected(socket, socket.remoteDevice)
                                    done()
                                }
                            }
                            STATE_NONE, STATE_CONNECTED ->
                                // Either not ready or already connected. Terminate new socket.
                                runCatching {
                                    socket.close()
                                    done()
                                }.onFailure {
                                    it.printStackTrace()
                                }

                            else -> Unit
                        }
                    }
                }
            }

        }

        fun done() {
            runCatching {
                mmServerSocket?.close()
                job.cancel()
            }.onFailure {
                it.printStackTrace()
            }
        }
    }


    /**
     * This class runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private inner class ConnectThread(val mmDevice: BluetoothDevice, val secure: Boolean) :
        CoroutineScope {

        private val job = Job()
        override val coroutineContext: CoroutineContext
            get() = job + ioDispatcher

        private var mmSocket: BluetoothSocket? = try {

            if (secure) {
                mmDevice.createRfcommSocketToServiceRecord(MY_UUID_SECURE)
            } else {
                mmDevice.createInsecureRfcommSocketToServiceRecord(MY_UUID_INSECURE)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }


        fun connect() = launch {

            mState = STATE_CONNECTING

            eventFlow.emit(BTChatEvents.Connecting(mmDevice))

            // Always cancel discovery because it will slow down a connection
            mAdapter?.cancelDiscovery()

            // Make a connection to the BluetoothSocket
            runCatching {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket?.connect()
            }.onFailure {

                runCatching {
                    mmSocket?.close()
                }.onFailure {
                    it.printStackTrace()
                }
                connectionFailed(mmDevice.name)

            }

            // Reset the ConnectThread because we're done
            synchronized(this@BluetoothChatService) {
                connectThread = null
            }

            // Start the connected thread
            mmSocket?.let { connected(it, mmDevice) }
        }

        fun done() {

            runCatching {
                mmSocket?.close()
                job.cancel()
            }.onFailure {
                it.printStackTrace()
            }

        }
    }

    /**
     * It handles all incoming and outgoing transmissions.
     */
    private inner class ConnectedThread(private val mmSocket: BluetoothSocket) :
        CoroutineScope {

        private val job = Job()
        override val coroutineContext: CoroutineContext
            get() = job + ioDispatcher

        var inStream: InputStream? = try {
            mmSocket.inputStream
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }

        var outStream: OutputStream? = try {
            mmSocket.outputStream
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }

        init {
            mState = STATE_CONNECTED
        }

        //Listening to received data
        fun startListening() = launch {
            runCatching {
                val buffer = ByteArray(1024)
                var bytes: Int
                // Keep listening to the InputStream while connected
                while (mState == STATE_CONNECTED) {
                    // Read from the InputStream
                    bytes = inStream?.read(buffer)!!
                    val message = String(buffer, 0, bytes)
                    eventFlow.emit(BTChatEvents.ReceivedMessage(message))
                }
            }.onFailure {
                it.printStackTrace()
                connectionLost(mmSocket.remoteDevice?.name)
            }
        }


        //Write to the connected OutStream.
        fun write(stringMessage: String) = launch {
            runCatching {
                outStream?.write(stringMessage.encodeToByteArray())
                eventFlow.emit(BTChatEvents.SendingMessage(stringMessage))
            }.onFailure {
                it.printStackTrace()
            }
        }

        fun done() {
            runCatching {
                mmSocket.close()
                outStream?.close()
                inStream?.close()

                job.cancel()
            }.onFailure {
                it.printStackTrace()
            }
        }


    }

}