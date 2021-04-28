package com.peer_messanger.ui.vm

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import com.peer_messanger.bluetoothchat.BluetoothChatServiceInterface
import com.peer_messanger.bluetoothchat.FakeBluetoothChatService
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


    private val receivedBtMessage =
        BluetoothMessage("id", "body", "macAddress", "0", "create time", false)
    private val sentBtMessage =
        BluetoothMessage("id", "body", "0", "macAddress", "create time", false)


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


    private val fakeBluetoothChatService get() = chatServiceInterface as FakeBluetoothChatService

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

        val firstDeviceWithMessages = mainViewModel.allDevicesWithMessages.first().first()

        assertThat(firstDeviceWithMessages.device).isNotNull()

    }

    @Test
    fun whenConnectedToDevice_shouldSendUndeliveredMessages() =
        coroutineDispatcher.runBlockingTest {
            //save undelivered message

            mainViewModel.saveSendMessage(sentBtMessage)

            val pairedDevice = mainViewModel.pairedDevices().first()

            mainViewModel.connectToDevice(pairedDevice.macAddress)

            val firstDeviceWithMessages = mainViewModel.allDevicesWithMessages.first().first()

            assertThat(firstDeviceWithMessages.sentBluetoothMessages.first().isDelivered).isTrue()

        }

    @Test
    fun whenConnectedToDevice_shouldSendUnacknowledgedMessages() =
        coroutineDispatcher.runBlockingTest {
            //save Unacknowledged message
            mainViewModel.saveSendMessage(receivedBtMessage)

            val pairedDevice = mainViewModel.pairedDevices().first()

            mainViewModel.connectToDevice(pairedDevice.macAddress)

            val firstDeviceWithMessages = mainViewModel.allDevicesWithMessages.first().first()

            assertThat(firstDeviceWithMessages.receivedBluetoothMessages.first().isDelivered).isEqualTo(
                true
            )

        }

    @Test
    fun sendMessageToConnectedDevice() = coroutineDispatcher.runBlockingTest {
        val pairedDevice = mainViewModel.pairedDevices().first()

        mainViewModel.connectToDevice(pairedDevice.macAddress)

        mainViewModel.sendMessage("messageBody", pairedDevice.macAddress)

        val firstDeviceWithMessages = mainViewModel.allDevicesWithMessages.first().first()

        assertThat(firstDeviceWithMessages.sentBluetoothMessages.first().body).isEqualTo("messageBody")

    }

    @Test
    fun saveReceivedMessageInToDb() = coroutineDispatcher.runBlockingTest {

        val pairedDevice = mainViewModel.pairedDevices().first()

        mainViewModel.connectToDevice(pairedDevice.macAddress)

        fakeBluetoothChatService.sendFakeMessage()

        val firstDeviceWithMessages = mainViewModel.allDevicesWithMessages.first().first()

        assertThat(firstDeviceWithMessages.receivedBluetoothMessages.first().body).isEqualTo("hello")
    }

    @Test
    fun sendAckMessageToReceivedMessage() = coroutineDispatcher.runBlockingTest {

        val pairedDevice = mainViewModel.pairedDevices().first()

        mainViewModel.connectToDevice(pairedDevice.macAddress)

        fakeBluetoothChatService.sendFakeMessage()

        val firstDeviceWithMessages = mainViewModel.allDevicesWithMessages.first().first()

        assertThat(firstDeviceWithMessages.receivedBluetoothMessages.first().isDelivered).isTrue()
    }
}