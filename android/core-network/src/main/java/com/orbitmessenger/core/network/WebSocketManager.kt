package com.orbitmessenger.core.network

import kotlinx.coroutines.flow.Flow

interface WebSocketManager {
    fun connect(token: String)
    fun disconnect()
    
    // Commands to send over WS
    suspend fun sendMessage(conversationId: String, content: String, type: String = "TEXT", replyToId: String? = null)
    suspend fun sendTypingIndicator(conversationId: String, isTyping: Boolean)
    suspend fun updateMessageStatus(messageId: String, status: String)

    // Flow for observing incoming WS events
    val incomingEvents: Flow<WsEvent>
}

// Map to Backend WsEvent
sealed class WsEvent {
    data class NewMessage(
        val id: String,
        val conversationId: String,
        val senderId: String,
        val content: String,
        val type: String
    ) : WsEvent()
    
    data class TypingIndicator(val conversationId: String, val userId: String, val isTyping: Boolean) : WsEvent()
    data class MessageStatusUpdate(val messageId: String, val status: String) : WsEvent()
}
