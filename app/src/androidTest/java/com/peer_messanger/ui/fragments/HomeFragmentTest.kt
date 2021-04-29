package com.peer_messanger.ui.fragments

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import com.peer_messanger.R
import com.peer_messanger.bluetoothchat.BluetoothChatServiceInterface
import com.peer_messanger.bluetoothchat.FakeBluetoothChatService
import com.peer_messanger.data.repository.LocalRepositoryInterface
import com.peer_messanger.fakeDevices
import com.peer_messanger.launchFragmentInHiltContainer
import com.peer_messanger.sentBtMessage
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltAndroidTest
class HomeFragmentTest {

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
    fun checkShowingPreviousChatsInRecyclerView() = runBlockingTest {
        localRepository.saveDevice(fakeDevices.first())
        localRepository.saveMessage(sentBtMessage)
        launchFragmentInHiltContainer<HomeFragment>()

        onView(withText(fakeDevices.first().name)).check(matches(isDisplayed()))
        onView(withText(sentBtMessage.body)).check(matches(isDisplayed()))

    }

    @Test
    fun navigateToChatFragmentWhenClickOnPreviousChat() = runBlockingTest {
        val navController = mock(NavController::class.java)
        localRepository.saveDevice(fakeDevices.first())
        localRepository.saveMessage(sentBtMessage)
        launchFragmentInHiltContainer<HomeFragment> {
            Navigation.setViewNavController(requireView(), navController)
        }
        onView(withText(sentBtMessage.body)).perform(click())
        verify(navController).navigate(HomeFragmentDirections.actionGlobalChatFragment(fakeDevices.first()))
    }

    @Test
    fun navigateToFindingFragment() = runBlockingTest {
        val navController = mock(NavController::class.java)

        launchFragmentInHiltContainer<HomeFragment> {
            Navigation.setViewNavController(requireView(), navController)
        }
        onView(withId(R.id.fab_add_new)).perform(click())
        verify(navController).navigate(HomeFragmentDirections.actionHomeFragmentToFindingFragment())
    }
}