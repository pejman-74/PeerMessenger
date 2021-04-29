package com.peer_messanger.ui.fragments

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.os.bundleOf
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import com.peer_messanger.*
import com.peer_messanger.bluetoothchat.BluetoothChatServiceInterface
import com.peer_messanger.bluetoothchat.FakeBluetoothChatService
import com.peer_messanger.data.repository.LocalRepositoryInterface
import com.peer_messanger.util.getCurrentUTCDateTime
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.Matchers.equalTo
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltAndroidTest
class ChatFragmentTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Inject
    lateinit var bluetoothChatServiceInterface: BluetoothChatServiceInterface

    private val fakeChatService get() = bluetoothChatServiceInterface as FakeBluetoothChatService

    @Inject
    lateinit var localRepository: LocalRepositoryInterface


    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun checkShowingPreviousChats() = runBlockingTest {

        localRepository.saveDevice(fakeDevices.first())
        localRepository.saveMessage(sentBtMessage.copy(createdTime = getCurrentUTCDateTime()))
        localRepository.saveMessage(receivedBtMessage.copy(createdTime = getCurrentUTCDateTime()))

        val bundle = bundleOf("device" to fakeDevices.first())
        launchFragmentInHiltContainer<ChatFragment>(bundle)

        onView(withText(fakeDevices.first().name)).check(matches(isDisplayed()))

        onView(withText(sentBtMessage.body)).check(matches(isDisplayed()))

        onView(withText(receivedBtMessage.body)).check(matches(isDisplayed()))

    }

    @Test
    fun checkSendMessage() = runBlockingTest {
        localRepository.saveDevice(fakeDevices.first())
        val bundle = bundleOf("device" to fakeDevices.first())

        launchFragmentInHiltContainer<ChatFragment>(bundle)

        onView(withId(R.id.ti_message)).perform(typeText("hi"))

        onView(withId(R.id.btn_send_message)).perform(click())

        onView(withText("hi")).check(matches(isDisplayed()))
    }

    @Test
    fun checkShowingWatchWhenDisconnectThenCheckMarkAfterConnected() = runBlockingTest {
        localRepository.saveDevice(fakeDevices.first())
        val bundle = bundleOf("device" to fakeDevices.first())
        //disconnect
        fakeChatService.stop()

        launchFragmentInHiltContainer<ChatFragment>(bundle)

        onView(withId(R.id.ti_message)).perform(typeText("hi"))

        onView(withId(R.id.btn_send_message)).perform(click())

        onView(withTagValue(equalTo(R.raw.message_watch))).check(matches(isDisplayed()))

        //start to can connect again
        fakeChatService.start()

        onView(withId(R.id.btn_chat_disconnectBar_connect)).perform(click())

        onView(withTagValue(equalTo(R.drawable.ic_check))).check(matches(isDisplayed()))
    }
}