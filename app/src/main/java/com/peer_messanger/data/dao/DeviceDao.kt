package com.peer_messanger.data.dao

import androidx.room.*
import com.peer_messanger.data.model.Device
import com.peer_messanger.data.relationship.DeviceWithMessages
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceDao {
    /**
     * in real-time apps,in CRUD-functionality maybe occur sometimes conflict
     * for preventing this problem used @Transaction then room run's operations is the same Transaction
     * */
    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(device: Device)

    @Transaction
    @Query("DELETE FROM device WHERE macAddress=:deviceMacAddress")
    suspend fun deleteById(deviceMacAddress: String)

    @Query("Select * from device where name!='self'")
    fun getAllDevicesWithMessages(): Flow<List<DeviceWithMessages>>

    @Query("Select * from device")
    suspend fun getAllDevice():List<Device>
}