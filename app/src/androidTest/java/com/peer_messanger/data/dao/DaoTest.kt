package com.peer_messanger.data.dao

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import com.peer_messanger.data.local.database.AppDatabase
import com.peer_messanger.data.model.BluetoothMessage
import com.peer_messanger.data.model.Device
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@SmallTest
class DaoTest {

    private lateinit var database: AppDatabase
    private lateinit var deviceDao: DeviceDao
    private lateinit var messageDao: MessageDao

    private val device = Device("macAddress", "")
    private val bluetoothMessage = BluetoothMessage("id", "body", "macAddress", "0", "create time")

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun before() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database =
            Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries()
                .build()

        deviceDao = database.deviceDao()
        messageDao = database.messageDao()
    }

    @After
    fun after() {
        database.close()
    }

    @Test
    fun checkInsertDevice() = runBlockingTest {
        deviceDao.insert(device)
        assertThat(deviceDao.getAllDevice()).contains(device)
    }

    @Test
    fun checkInsertBluetoothMessage() = runBlockingTest {
        messageDao.insert(bluetoothMessage)
        assertThat(messageDao.getAllBluetoothMessage()).contains(bluetoothMessage)
    }

    @Test
    fun checkRelationShip() = runBlockingTest {
        deviceDao.insert(device)
        messageDao.insert(bluetoothMessage)
        val allDeicesWithMessages = deviceDao.getAllDevicesWithMessages().first()
        assertThat(allDeicesWithMessages.size).isEqualTo(1)
    }

}

