package com.peer_messanger.di.module

import com.peer_messanger.data.local.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Singleton


@ExperimentalCoroutinesApi
@InstallIn(SingletonComponent::class)
@Module
object DaoModule {

    @Singleton
    @Provides
    fun provideDeviceDao(db: AppDatabase) = db.deviceDao()

    @Singleton
    @Provides
    fun provideMessageDao(db: AppDatabase) = db.messageDao()

}