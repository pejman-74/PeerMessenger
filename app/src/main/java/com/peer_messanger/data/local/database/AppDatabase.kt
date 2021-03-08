package com.peer_messanger.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.peer_messanger.data.dao.DeviceDao
import com.peer_messanger.data.dao.MessageDao
import com.peer_messanger.data.model.Device
import com.peer_messanger.data.model.BluetoothMessage

@Database(entities = [BluetoothMessage::class, Device::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun deviceDao(): DeviceDao
    abstract fun messageDao(): MessageDao
}