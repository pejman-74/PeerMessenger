package com.peer_messanger.di.module

import android.content.Context
import com.peer_messanger.bluetoothchat.BluetoothChatService
import com.peer_messanger.bluetoothchat.BluetoothChatServiceInterface
import com.peer_messanger.bluetoothchat.blueflow.BlueFlow
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Singleton

@ExperimentalCoroutinesApi
@Module
@InstallIn(SingletonComponent::class)
object ChatServiceModule {

    @Singleton
    @Provides
    fun provideBlueFlow(@ApplicationContext context: Context): BlueFlow = BlueFlow(context)

    @Singleton
    @Provides
    fun provideChatService(blueFlow: BlueFlow): BluetoothChatServiceInterface =
        BluetoothChatService(blueFlow)

}
