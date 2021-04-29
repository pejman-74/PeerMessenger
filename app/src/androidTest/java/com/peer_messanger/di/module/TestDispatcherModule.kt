package com.peer_messanger.di.module

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import javax.inject.Singleton

@ExperimentalCoroutinesApi
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DispatcherModule::class]
)
@Module
object TestDispatcherModule {

    @Singleton
    @Provides
    fun provideTestDispatcher(): CoroutineDispatcher = TestCoroutineDispatcher()


}