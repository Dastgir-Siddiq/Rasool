package com.orbitmessenger.backend.models

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestamp
import kotlinx.serialization.Serializable
import java.util.UUID

object Conversations : UUIDTable("conversations") {
    val isGroup = bool("is_group").default(false)
    val name = varchar("name", 100).nullable()
    val createdAt = timestamp("created_at")
}

object ConversationMembers : UUIDTable("conversation_members") {
    val conversationId = reference("conversation_id", Conversations)
    val userId = reference("user_id", Users)
    val role = varchar("role", 20).default("MEMBER")
    
    init {
        uniqueIndex(conversationId, userId)
    }
}

object Messages : UUIDTable("messages") {
    val conversationId = reference("conversation_id", Conversations)
    val senderId = reference("sender_id", Users)
    val type = varchar("type", 20).default("TEXT")
    val content = text("content")
    val status = varchar("status", 20).default("SENT")
    val createdAt = timestamp("created_at")
    val replyToId = reference("reply_to_id", Messages).nullable()
}

@Serializable
data class ConversationResponse(
    val id: String,
    val isGroup: Boolean,
    val name: String?,
    val participants: List<UserResponse>
)

@Serializable
data class MessageResponse(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val type: String,
    val content: String,
    val status: String,
    val createdAt: Long,
    val replyToId: String?
)

@Serializable
data class CreateConversationRequest(
    val participantIds: List<String>,
    val isGroup: Boolean,
    val name: String? = null
)
