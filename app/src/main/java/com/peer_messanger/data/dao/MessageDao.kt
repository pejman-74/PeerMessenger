package com.peer_messanger.data.dao

import androidx.room.*
import com.peer_messanger.data.model.BluetoothMessage
import org.jetbrains.annotations.TestOnly

@Dao
interface MessageDao {
    /**
     * in real-time apps,in CRUD-functionality maybe occur sometimes conflict
     * for preventing this problem used @Transaction then room run's operations is the same Transaction
     * */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @Transaction
    suspend fun insert(bluetoothMessage: BluetoothMessage)

    @Query("DELETE FROM BluetoothMessage WHERE id=:messageId")
    @Transaction
    suspend fun deleteById(messageId: String)

    @Query("UPDATE  BluetoothMessage SET isDelivered=:isDelivered WHERE id=:messageId")
    @Transaction
    suspend fun setIsDelivered(messageId: String, isDelivered: Boolean)

    @Query("SELECT * FROM BluetoothMessage WHERE receiverDevice=:macAddress AND isDelivered=0")
    suspend fun getUnDeliveredMessages(macAddress: String): List<BluetoothMessage>

    @Query("SELECT * FROM BluetoothMessage WHERE messageOwner=:macAddress AND isDelivered=0")
    suspend fun getUnacknowledgedMessages(macAddress: String): List<BluetoothMessage>

    @Query("Select * from BluetoothMessage")
    @TestOnly
    suspend fun getAllBluetoothMessage(): List<BluetoothMessage>
}