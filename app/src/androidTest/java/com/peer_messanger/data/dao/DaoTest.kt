package com.peer_messanger.data.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.peer_messanger.data.model.BluetoothMessage
import com.peer_messanger.data.model.Device
import com.peer_messanger.di.module.ChatServiceModule
import com.peer_messanger.di.module.DatabaseModule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
@ExperimentalCoroutinesApi
@UninstallModules(DatabaseModule::class, ChatServiceModule::class)
class DaoTest {

    @Inject
    lateinit var deviceDao: DeviceDao

    @Inject
    lateinit var messageDao: MessageDao

    private val device = Device("macAddress", "")
    private val bluetoothMessage = BluetoothMessage("id", "body", "macAddress", "", "create time")

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun before() {
        hiltRule.inject()
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
        assertThat(messageDao.getAllBluetoothMessage().contains(bluetoothMessage)).isTrue()
        assertThat(deviceDao.getAllDevice().contains(device)).isTrue()
        val allDeicesWithMessages = deviceDao.getAllDevicesWithMessages().first().first()
        assertThat(allDeicesWithMessages.device).isEqualTo(device)
        assertThat(allDeicesWithMessages.receivedBluetoothMessages.first()).isEqualTo(
            bluetoothMessage
        )
    }

}

