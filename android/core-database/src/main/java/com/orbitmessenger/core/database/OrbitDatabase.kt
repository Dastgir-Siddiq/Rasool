package com.orbitmessenger.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.orbitmessenger.core.database.dao.ConversationDao
import com.orbitmessenger.core.database.dao.MessageDao
import com.orbitmessenger.core.database.entity.ConversationEntity
import com.orbitmessenger.core.database.entity.MessageEntity
import com.orbitmessenger.core.database.entity.UserEntity

@Database(
    entities = [UserEntity::class, ConversationEntity::class, MessageEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class OrbitDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
}
