package com.orbitmessenger.core.database

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideOrbitDatabase(@ApplicationContext context: Context): OrbitDatabase {
        return Room.databaseBuilder(
            context,
            OrbitDatabase::class.java,
            "orbit_messenger.db"
        ).build()
    }

    @Provides
    fun provideConversationDao(database: OrbitDatabase) = database.conversationDao()

    @Provides
    fun provideMessageDao(database: OrbitDatabase) = database.messageDao()
}
