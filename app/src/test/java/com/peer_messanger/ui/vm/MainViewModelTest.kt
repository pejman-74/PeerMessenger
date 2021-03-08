package com.peer_messanger.ui.vm

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.peer_messanger.MainCoroutineRule
import com.peer_messanger.data.FakeRepository
import com.peer_messanger.data.model.BluetoothMessage
import com.peer_messanger.data.wrap.BluetoothEventResource
import com.peer_messanger.getOrAwaitValueTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.concurrent.TimeoutException

@ExperimentalCoroutinesApi
@RunWith(JUnit4::class)
class MainViewModelTest {

    private lateinit var mainViewModel: MainViewModel
    private lateinit var fakeRepository: FakeRepository
    private val macAddress = "macAddress"
    private val deviceName = "android device"
    private val gsonMessageBody = "{ id:'id',body:'hello' }"
    private val bluetMessage = BluetoothMessage("id", "body", macAddress, "0", "create time")

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()


    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUp() {
        fakeRepository = FakeRepository()
        mainViewModel = MainViewModel(fakeRepository)
    }


    @Test
    fun `bluetooth live status, should be on`() {
        mainViewModel.postBluetoothStatus(true)
        assertThat(mainViewModel.bluetoothLiveStatus.getOrAwaitValueTest().peekContent()).isTrue()
    }

    @Test
    fun `bluetooth live status, should be off`() {
        mainViewModel.postBluetoothStatus(false)
        assertThat(mainViewModel.bluetoothLiveStatus.getOrAwaitValueTest().peekContent()).isFalse()
    }

    @Test
    fun `scan devices, should send device connected`() {
        mainViewModel.postBluetoothEventsResource(BluetoothEventResource.DeviceConnected)
        assertThat(mainViewModel.bluetoothEventsEventResource.getOrAwaitValueTest()).isEqualTo(
            BluetoothEventResource.DeviceConnected
        )
    }


    @Test
    fun `get all devices with messages`() {

        mainViewModel.saveDevice(macAddress, deviceName)

        mainViewModel.saveReceivedMessage(gsonMessageBody, macAddress)

        mainViewModel.saveSendMessage(bluetMessage)

        mainViewModel.getDeviceWithMessages(macAddress)

        val result = mainViewModel.deviceWithMessages.getOrAwaitValueTest()

        assertThat(result.device.macAddress).isEqualTo(macAddress)

    }

    @Test
    fun `on save received-message successfully, should be add to readyToAcknowledgmentMessages`() {
        mainViewModel.saveReceivedMessage(gsonMessageBody, macAddress)
        assertThat(mainViewModel.readyToAcknowledgmentMessages.getOrAwaitValueTest().id).isEqualTo("id")
    }

    @Test(expected = TimeoutException::class)
    fun `on save received-message with empty message body, then should not be added to readyToAcknowledgmentMessages`() {
        mainViewModel.saveReceivedMessage("", macAddress)

        assertThat(mainViewModel.readyToAcknowledgmentMessages.getOrAwaitValueTest())
    }

    @Test
    fun `save send-message successfully, then should be add to db`() {
        mainViewModel.saveSendMessage(bluetMessage)
        assertThat(
            fakeRepository.bluetoothMessageTableLive.getOrAwaitValueTest().first().id
        ).isEqualTo("id")
    }

    @Test
    fun `save send-message with empty body, then should not be added to db`() {
        mainViewModel.saveSendMessage(bluetMessage.copy(body = ""))
        assertThat(
            fakeRepository.bluetoothMessageTableLive.getOrAwaitValueTest()
        ).isEmpty()
    }

    @Test
    fun `save device successfully`() {
        mainViewModel.saveDevice(macAddress, deviceName)
        assertThat(
            fakeRepository.deviceTableLive.getOrAwaitValueTest().first().macAddress
        ).isEqualTo(macAddress)
    }

    @Test
    fun `save device failure with empty macAddress, then should not be added to db`() {
        mainViewModel.saveDevice("", deviceName)
        assertThat(
            fakeRepository.deviceTableLive.getOrAwaitValueTest()
        ).isEmpty()
    }

    @Test
    fun `set bluetooth-message delivered,then should be updated in db`() {
        mainViewModel.saveSendMessage(bluetMessage)
        mainViewModel.setBluetoothMessageIsDelivered(bluetMessage.id, true)
        assertThat(
            fakeRepository.bluetoothMessageTableLive.getOrAwaitValueTest().first().isDelivered
        ).isTrue()
    }

    @Test
    fun `get undelivered messages`() {
        mainViewModel.saveSendMessage(bluetMessage.copy(isDelivered = true))

        mainViewModel.saveSendMessage(bluetMessage.copy(isDelivered = false))
        mainViewModel.saveSendMessage(bluetMessage.copy(isDelivered = false))

        mainViewModel.getUnDeliveredMessages(macAddress)
        assertThat(
           mainViewModel.unDeliveredMessages.getOrAwaitValueTest().size
        ).isEqualTo(2)
    }

    @Test
    fun `get unacknowledged-messages`() {
        mainViewModel.saveSendMessage(bluetMessage.copy(isDelivered = true))

        mainViewModel.saveSendMessage(bluetMessage.copy(isDelivered = false))
        mainViewModel.saveSendMessage(bluetMessage.copy(isDelivered = false))

        mainViewModel.getUnacknowledgedMessages(macAddress)
        assertThat(
            mainViewModel.unDeliveredMessages.getOrAwaitValueTest().size
        ).isEqualTo(2)
    }
}