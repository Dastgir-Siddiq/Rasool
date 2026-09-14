package com.orbitmessenger.backend.sockets

import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object ConnectionManager {
    // Map of User UUID to their active WebSocket Sessions (a user might have multiple devices)
    private val activeConnections = ConcurrentHashMap<String, CopyOnWriteArraySet<DefaultWebSocketServerSession>>()

    fun addConnection(userId: String, session: DefaultWebSocketServerSession) {
        val sessions = activeConnections.computeIfAbsent(userId) { CopyOnWriteArraySet() }
        sessions.add(session)
    }

    fun removeConnection(userId: String, session: DefaultWebSocketServerSession) {
        val sessions = activeConnections[userId]
        sessions?.remove(session)
        if (sessions?.isEmpty() == true) {
            activeConnections.remove(userId)
        }
    }

    suspend fun sendMessageToUser(userId: String, messageEvent: WsEvent) {
        val sessions = activeConnections[userId] ?: return
        val jsonPayload = Json.encodeToString(messageEvent)
        sessions.forEach { session ->
            try {
                session.send(Frame.Text(jsonPayload))
            } catch (e: Exception) {
                // Connection might be closed, handled gracefully
            }
        }
    }
}
