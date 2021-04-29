package com.peer_messanger.ui.activity

import android.bluetooth.BluetoothAdapter
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.navigation.Navigation.findNavController
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import com.google.common.truth.Truth
import com.peer_messanger.R
import com.peer_messanger.bluetoothchat.BluetoothChatServiceInterface
import com.peer_messanger.bluetoothchat.FakeBluetoothChatService
import com.peer_messanger.data.repository.LocalRepositoryInterface
import com.peer_messanger.fakeDevices
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltAndroidTest
class MainActivityTest {

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


    private fun launchActivity(): ActivityScenario<MainActivity> {
        return ActivityScenario.launch(MainActivity::class.java)
    }

    @Test
    fun finishAppIfDeviceNotSupportBT()  {
        fakeChatService.setDeviceHasBluetooth(false)
        val activityScenario = launchActivity()
        Truth.assertThat(activityScenario.state == Lifecycle.State.DESTROYED).isTrue()
    }

    @Test
    fun whenConnectedShouldNavigateToChatFragment() = runBlockingTest {
        launchActivity()
        fakeChatService.connect(fakeDevices.first().macAddress)
        onView(withId(R.id.rv_chat)).check(matches(isDisplayed()))
    }

    @Test
    fun whenBtIsOffShowDialogToEnable() = runBlockingTest {
        fakeChatService.setBluetoothStatus(BluetoothAdapter.STATE_OFF)
        launchActivity()
        onView(withText(R.string.turn_on_the_bluetooth)).check(matches(isDisplayed()))

        onView(withText(R.string.turn_on)).perform(click())

        Truth.assertThat(fakeChatService.bluetoothIsOn()).isTrue()
    }
}