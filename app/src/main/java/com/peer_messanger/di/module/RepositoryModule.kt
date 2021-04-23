package com.peer_messanger.di.module

import com.peer_messanger.data.dao.DeviceDao
import com.peer_messanger.data.dao.MessageDao
import com.peer_messanger.data.repository.LocalRepository
import com.peer_messanger.data.repository.LocalRepositoryInterface
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object RepositoryModule {
    @Singleton
    @Provides
    fun provideRepository(
        deviceDao: DeviceDao,
        messageDao: MessageDao,
    ): LocalRepositoryInterface = LocalRepository(deviceDao, messageDao)
}