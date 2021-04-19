package com.peer_messanger.di.module

import com.peer_messanger.bluetoothchat.BluetoothChatService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ChatServiceModule {
    @Singleton
    @Provides
    fun provideChatService(ioDispatcher: CoroutineDispatcher): BluetoothChatService =
        BluetoothChatService(ioDispatcher)
}
