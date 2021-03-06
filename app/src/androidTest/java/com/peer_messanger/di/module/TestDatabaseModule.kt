package com.peer_messanger.di.module

import android.content.Context
import androidx.room.Room
import com.peer_messanger.data.local.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DatabaseModule::class]
)
@Module
object TestDatabaseModule {

    @Provides
    @Singleton
    fun provideInMemoryRoomDB(@ApplicationContext context: Context): AppDatabase =
        Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
}