package com.orbitmessenger.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String?,
    val isGroup: Boolean,
    val unreadCount: Int = 0,
    val lastMessagePreview: String?,
    val lastMessageTimestamp: Long?,
    val isPinned: Boolean = false
)
