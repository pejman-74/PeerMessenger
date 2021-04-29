package com.peer_messanger.di.module

import com.peer_messanger.bluetoothchat.BluetoothChatServiceInterface
import com.peer_messanger.bluetoothchat.FakeBluetoothChatService
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Singleton

@ExperimentalCoroutinesApi
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [ChatServiceModule::class]
)
object TestChatServiceModule {

    @Singleton
    @Provides
    fun provideChatService(): BluetoothChatServiceInterface = FakeBluetoothChatService()
}