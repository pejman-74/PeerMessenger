package com.peer_messanger.ui.vm

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import com.peer_messanger.bluetoothchat.BluetoothChatServiceInterface
import com.peer_messanger.data.model.BluetoothMessage
import com.peer_messanger.data.repository.LocalRepositoryInterface
import com.peer_messanger.di.module.ChatServiceModule
import com.peer_messanger.di.module.DatabaseModule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@ExperimentalCoroutinesApi
@UninstallModules(DatabaseModule::class, ChatServiceModule::class)
@HiltAndroidTest
@SmallTest
class MainViewModelTest {


    private val macAddress = "macAddress"
    private val deviceName = "android device"
    private val gsonMessageBody = "{ id:'id',body:'hello' }"
    private val receivedBtMessage =
        BluetoothMessage("id", "body", macAddress, "0", "create time", false)
    private val sentBtMessage =
        BluetoothMessage("id", "body", "0", macAddress, "create time", false)


    private lateinit var mainViewModel: MainViewModel

    @Inject
    lateinit var localRepository: LocalRepositoryInterface

    @Inject
    lateinit var chatServiceInterface: BluetoothChatServiceInterface


    @Inject
    lateinit var coroutineDispatcher: TestCoroutineDispatcher

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()


    @Before
    fun setUp() {
        hiltRule.inject()
        mainViewModel = MainViewModel(localRepository, chatServiceInterface, coroutineDispatcher)
    }

    @After
    fun tearDown() {
        coroutineDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun connectToPairedDevice_shouldSaveInDB() = coroutineDispatcher.runBlockingTest {

        val pairedDevice = mainViewModel.pairedDevices().first()

        mainViewModel.connectToDevice(pairedDevice.macAddress)

        assertThat(mainViewModel.allDevicesWithMessages.first().first().device).isNotNull()

    }

    @Test
    fun whenConnectedToDevice_shouldSendUndeliveredMessages() =
        coroutineDispatcher.runBlockingTest {
            //save undelivered message

            mainViewModel.saveSendMessage(sentBtMessage)

            val pairedDevice = mainViewModel.pairedDevices().first()

            mainViewModel.connectToDevice(pairedDevice.macAddress)


            mainViewModel.allDevicesWithMessages.first().let {
                assertThat(it.first().sentBluetoothMessages.first().isDelivered).isTrue()
            }

    }

    @Test
    fun whenConnectedToDevice_shouldSendUnacknowledgedMessages() =
        coroutineDispatcher.runBlockingTest {
            //save undelivered message
            mainViewModel.saveSendMessage(receivedBtMessage)

            val pairedDevice = mainViewModel.pairedDevices().first()

            mainViewModel.connectToDevice(pairedDevice.macAddress)

            assertThat(
                mainViewModel.allDevicesWithMessages.first()
                    .first().receivedBluetoothMessages.first().isDelivered
            ).isEqualTo(true)

    }
}