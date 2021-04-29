package com.peer_messanger.ui.fragments

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import com.google.common.truth.Truth.assertThat
import com.peer_messanger.R
import com.peer_messanger.bluetoothchat.BluetoothChatServiceInterface
import com.peer_messanger.bluetoothchat.FakeBluetoothChatService
import com.peer_messanger.data.wrapper.ConnectionEvents
import com.peer_messanger.fakeDevices
import com.peer_messanger.launchFragmentInHiltContainer
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltAndroidTest
class FindingFragmentTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Inject
    lateinit var bluetoothChatServiceInterface: BluetoothChatServiceInterface

    private val fakeChatService get() = bluetoothChatServiceInterface as FakeBluetoothChatService


    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun checkShowingPairedDevices() {

        fakeChatService.setHasPairedDevice(true)

        launchFragmentInHiltContainer<FindingFragment>()

        onView(withText(fakeDevices.first().name)).check(matches(isDisplayed()))
    }

    @Test
    fun checkShowingFinedDevice() {

        launchFragmentInHiltContainer<FindingFragment>()

        onView(withId(R.id.pfab_search)).perform(click())

        onView(withText(fakeDevices.first().name)).check(matches(isDisplayed()))
    }


    @Test
    fun checkConnectingToDevice() = runBlockingTest {
        launchFragmentInHiltContainer<FindingFragment>()

        fakeChatService.start()

        onView(withId(R.id.pfab_search)).perform(click())

        onView(withText(fakeDevices.first().name)).perform(click())

        assertThat(fakeChatService.connectionState().first() is ConnectionEvents.Connected).isTrue()

    }

    @Test
    fun showDialogWhenTryConnectToExceptSmartPhoneDevice() = runBlockingTest {
        launchFragmentInHiltContainer<FindingFragment>()

        onView(withId(R.id.pfab_search)).perform(click())

        onView(withText(fakeDevices[1].name)).perform(click())

        onView(withText(R.string.inappropriate_device_select)).check(matches(isDisplayed()))

    }
}