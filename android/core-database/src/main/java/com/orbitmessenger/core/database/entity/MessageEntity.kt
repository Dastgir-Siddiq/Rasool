package com.orbitmessenger.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

enum class MessageType { TEXT, IMAGE, VIDEO, AUDIO, FILE, SYSTEM }
enum class MessageStatus { SENDING, SENT, DELIVERED, READ, FAILED }

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["conversationId"]), Index(value = ["createdAt"])]
)
data class MessageEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val conversationId: String,
    val senderId: String,
    val type: MessageType = MessageType.TEXT,
    val content: String, // Encrypted content or plaintext depending on state
    val status: MessageStatus = MessageStatus.SENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val replyToId: String?,
    val attachmentUrl: String? = null,
    val attachmentType: String? = null
)
