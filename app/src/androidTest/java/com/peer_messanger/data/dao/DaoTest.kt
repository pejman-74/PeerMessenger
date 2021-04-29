package com.peer_messanger.data.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.peer_messanger.fakeDevices
import com.peer_messanger.receivedBtMessage
import com.peer_messanger.sentBtMessage
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
@ExperimentalCoroutinesApi
class DaoTest {

    @Inject
    lateinit var deviceDao: DeviceDao

    @Inject
    lateinit var messageDao: MessageDao

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
        deviceDao.insert(fakeDevices.first())
        assertThat(deviceDao.getAllDevice()).contains(fakeDevices.first())
    }

    @Test
    fun checkInsertBluetoothMessage() = runBlockingTest {
        messageDao.insert(sentBtMessage)
        assertThat(messageDao.getAllBluetoothMessage()).contains(sentBtMessage)
    }

    @Test
    fun checkRelationShip() = runBlockingTest {
        deviceDao.insert(fakeDevices.first())
        messageDao.insert(sentBtMessage)
        messageDao.insert(receivedBtMessage)

        val allDeicesWithMessages = deviceDao.getAllDevicesWithMessages().first().first()
        assertThat(allDeicesWithMessages.device).isEqualTo(fakeDevices.first())
        assertThat(allDeicesWithMessages.sentBluetoothMessages.first()).isEqualTo(
            sentBtMessage
        )
        assertThat(allDeicesWithMessages.receivedBluetoothMessages.first()).isEqualTo(
            receivedBtMessage
        )
    }

}

