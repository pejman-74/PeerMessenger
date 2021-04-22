package com.peer_messanger.bluetoothchat.blueflow

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import com.peer_messanger.util.TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class BlueFlowIO(val bluetoothSocket: BluetoothSocket) {


    private val inputStream: InputStream =
        try {
            bluetoothSocket.inputStream
        } catch (e: IOException) {
            throw e
        }

    private val outputStream: OutputStream =
        try {
            bluetoothSocket.outputStream
        } catch (e: IOException) {
            throw e
        }


    fun getBluetoothDevice(): BluetoothDevice? = bluetoothSocket.remoteDevice

    /**
     * Send array of bytes to bluetooth output stream.
     *
     * @param bytes data to send
     * @return true if success, false if there was error occurred or disconnected
     */
    private suspend fun send(bytes: ByteArray): Boolean = withContext(Dispatchers.IO) {
        //isConnected not work properly in some devices
        //if (!bluetoothSocket.isConnected) return false
        runCatching {
            outputStream.write(bytes)
            outputStream.flush()
            true
        }.onFailure {
            Log.e(TAG, "send: error", it)
        }.isSuccess
    }

    /**
     * Send string of text to bluetooth output stream.
     *
     * @param text text to send
     * @return true if success, false if there was error occurred or disconnected
     */
    suspend fun send(text: String) = send(text.encodeToByteArray())

    @ExperimentalCoroutinesApi
    fun readByteAsString(
        bufferCapacity: Int = 1024
    ): Flow<String> = channelFlow {

        val buffer = ByteArray(bufferCapacity)
        while (isActive) {
            runCatching {
                val numBytes = inputStream.read(buffer)
                val message = String(buffer, 0, numBytes)
                offer(message)
            }.onFailure {
                Log.e(TAG, "readByteArrayStream: error", it)
                closeConnections()
            }
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Close the streams and socket connection.
     */
    fun closeConnections() {
        try {
            inputStream.close()
            outputStream.close()
            bluetoothSocket.close()
        } catch (e: Exception) {
            Log.e(TAG, "closeConnections: error", e)
        }
    }

}