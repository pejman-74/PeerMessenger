package com.peer_messanger.bluetoothchat.blueflow

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.text.TextUtils
import android.util.Log
import com.peer_messanger.data.wrapper.ConnectionEvents
import com.peer_messanger.data.wrapper.ScanResource
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import java.io.IOException
import java.util.*

class BlueFlow constructor(val context: Context) {


    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    private var blueFlowIO: BlueFlowIO? = null

    /**
     * Return true if Bluetooth is available.
     *
     * @return true if bluetoothAdapter is not null or it's address is empty, otherwise Bluetooth is
     * not supported on this hardware platform
     */
    @SuppressLint("HardwareIds")
    fun isBluetoothAvailable() =
        !(bluetoothAdapter == null || TextUtils.isEmpty(bluetoothAdapter.address))

    /**
     * get bluetooth device by mac address
     * */
    fun getBluetoothDevice(macAddress: String) = bluetoothAdapter?.getRemoteDevice(macAddress)

    /**
     * Return true if Bluetooth is currently enabled and ready for use.
     * <p>Equivalent to:
     * <code>getBluetoothState() == STATE_ON</code>
     * <p>Requires [android.Manifest.permission.BLUETOOTH]
     *
     * @return true if the local adapter is turned on
     */
    fun isBluetoothEnabled() = bluetoothAdapter?.isEnabled == true

    /**
     * Get [BlueFlowIO] Helper class for simplifying read and write operations from/to {@link BluetoothSocket}.
     *
     * @param bluetoothSocket bluetooth socket
     * @returns BlueFlowIO
     */
    fun getIO(bluetoothSocket: BluetoothSocket): BlueFlowIO {

        if (blueFlowIO?.bluetoothSocket === bluetoothSocket) {
            return blueFlowIO as BlueFlowIO
        }
        blueFlowIO = BlueFlowIO(bluetoothSocket)
        return blueFlowIO as BlueFlowIO
    }

    /**
     * Turn on the local Bluetooth adapter — do not use without explicit user action to turn on
     * Bluetooth.
     *
     * @return true to indicate adapter startup has begun, or false on
     * immediate error
     * @see BluetoothAdapter.enable
     */
    fun enable(): Boolean {
        bluetoothAdapter?.let { return it.enable() }
        return false
    }


    /**
     * Return the set of [BluetoothDevice] objects that are bonded
     * (paired) to the local adapter.
     *
     * If Bluetooth state is not [BluetoothAdapter.STATE_ON], this API
     * will return an empty set. After turning on Bluetooth,
     * wait for [BluetoothAdapter.ACTION_STATE_CHANGED] with [BluetoothAdapter.STATE_ON]
     * to get the updated value.
     *
     * Requires [android.Manifest.permission.BLUETOOTH].
     *
     * @return unmodifiable set of [BluetoothDevice], or null on error
     */
    fun bondedDevices(): Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices

    /**
     * Start the remote device discovery process.
     *
     * @return true on success, false on error
     */
    fun startDiscovery() = bluetoothAdapter?.startDiscovery() == true

    /**
     * Cancel the remote device discovery process.
     *
     * @return true on success, false on error
     */
    fun cancelDiscovery() = bluetoothAdapter?.cancelDiscovery() == true

    /**
     * Observes Bluetooth devices found while discovering.
     */
    @ExperimentalCoroutinesApi
    fun discoverDevices(): Flow<ScanResource> = callbackFlow<ScanResource> {
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND).apply {
            addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    BluetoothDevice.ACTION_FOUND -> {
                        Log.i("BlueFlowLib", "FOUND DEVICE")
                        val device =
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE) as BluetoothDevice?
                        device?.let { offer(ScanResource.DeviceFound(device)) }
                    }
                    BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                        offer(ScanResource.DiscoveryStarted)
                        Log.i("BlueFlowLib", "DISCOVERY STARTED")
                    }
                    BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                        offer(ScanResource.DiscoveryFinished)
                        Log.i("BlueFlowLib", "DISCOVERY FINISHED")
                    }
                }
            }
        }
        context.registerReceiver(receiver, filter)

        awaitClose {
            context.unregisterReceiver(receiver)
        }
    }.flowOn(Dispatchers.IO)


    /**
     * Opens {@link BluetoothServerSocket}, listens for a single connection request, releases socket
     * and returns a connected {@link BluetoothSocket} on successful connection. Notifies observers
     * with {@link IOException} {@code onError()}.
     *
     * @param name service name for SDP record
     * @param uuid uuid for SDP record
     * @param secure connection security status
     * @return Single with connected {@link BluetoothSocket} on successful connection
     * @throws IOException when socket might closed or timeout, read ret: -1
     */

    suspend fun connectAsServerAsync(
        name: String,
        uuid: UUID,
        secure: Boolean = true
    ): Deferred<BluetoothSocket> =
        coroutineScope {
            return@coroutineScope async(Dispatchers.IO) {
                if (bluetoothAdapter == null)
                    throw IOException("Bluetooth adapter is null")
                val bluetoothServerSocket: BluetoothServerSocket = if (secure)
                    bluetoothAdapter.listenUsingRfcommWithServiceRecord(name, uuid)
                else
                    bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(name, uuid)
                bluetoothServerSocket.accept()
            }
        }

    /**
     * Create connection to {@link BluetoothDevice} and returns a connected {@link BluetoothSocket}
     * on successful connection. Notifies observers with {@link IOException} via {@code onError()}.
     *
     * @param bluetoothDevice bluetooth device to connect
     * @param uuid uuid for SDP record
     * @param secure connection security status
     * @return Deferred with connected {@link BluetoothSocket} on successful connection
     * @throws IOException when socket might closed or timeout, read ret: -1
     */

    suspend fun connectAsClientAsync(
        bluetoothDevice: BluetoothDevice,
        uuid: UUID,
        secure: Boolean = true
    ): Deferred<BluetoothSocket> =
        coroutineScope {
            return@coroutineScope async(Dispatchers.IO) {
                val bluetoothSocket =
                    if (secure) bluetoothDevice.createRfcommSocketToServiceRecord(uuid)
                    else bluetoothDevice.createInsecureRfcommSocketToServiceRecord(uuid)
                bluetoothSocket.apply {
                    connect()
                }
            }
        }

    /**
     * Observes BluetoothState. Possible values are:
     *
     * [BluetoothAdapter.STATE_OFF],
     * [BluetoothAdapter.STATE_TURNING_ON],
     * [BluetoothAdapter.STATE_ON],
     * [BluetoothAdapter.STATE_TURNING_OFF],
     *
     * @return Flow Observable with BluetoothState
     */
    @ExperimentalCoroutinesApi
    fun bluetoothState(): Flow<Int> = callbackFlow<Int> {
        val filter = IntentFilter().apply {
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                bluetoothAdapter ?: return
                offer(bluetoothAdapter.state)
            }
        }
        context.registerReceiver(receiver, filter)
        awaitClose {
            context.unregisterReceiver(receiver)
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Observes ACL broadcast actions from {@link BluetoothDevice}. Possible broadcast ACL action
     * values are:
     * [BluetoothDevice.ACTION_ACL_CONNECTED],
     * [BluetoothDevice.ACTION_ACL_DISCONNECTED]
     *
     * @return Flow Observable with {@link AclEvent}
     */
    @ExperimentalCoroutinesApi
    fun aclEvents(): Flow<ConnectionEvents> = callbackFlow<ConnectionEvents> {
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val action = intent?.action
                val device =
                    intent?.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE) as? BluetoothDevice
                if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
                    offer(ConnectionEvents.Disconnect(device?.name))
                }
            }

        }
        context.registerReceiver(receiver, filter)
        awaitClose {
            context.unregisterReceiver(receiver)
        }
    }.flowOn(Dispatchers.IO)

}