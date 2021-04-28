package com.peer_messanger.di.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import javax.inject.Singleton

@ExperimentalCoroutinesApi
@InstallIn(SingletonComponent::class)
@Module
object TestDispatcherModule {

    @Singleton
    @Provides
    fun provideTestDispatcher(): TestCoroutineDispatcher = TestCoroutineDispatcher()


}