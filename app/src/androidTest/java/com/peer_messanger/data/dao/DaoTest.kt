package com.peer_messanger.data.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import com.peer_messanger.data.local.database.AppDatabase
import com.peer_messanger.data.model.BluetoothMessage
import com.peer_messanger.data.model.Device
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject
import javax.inject.Named

@HiltAndroidTest
@ExperimentalCoroutinesApi
@SmallTest
class DaoTest {
    @Inject
    @Named("testDB")
    lateinit var database: AppDatabase

    private lateinit var deviceDao: DeviceDao
    private lateinit var messageDao: MessageDao

    private val device = Device("macAddress", "")
    private val bluetoothMessage = BluetoothMessage("id", "body", "macAddress", "0", "create time")

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun before() {

        hiltRule.inject()
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

