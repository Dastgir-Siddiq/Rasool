package com.orbitmessenger.backend.routes

import com.orbitmessenger.backend.models.ConversationMembers
import com.orbitmessenger.backend.models.MessageResponse
import com.orbitmessenger.backend.models.Messages
import com.orbitmessenger.backend.sockets.ConnectionManager
import com.orbitmessenger.backend.sockets.WsAction
import com.orbitmessenger.backend.sockets.WsEvent
import com.orbitmessenger.backend.security.JwtService
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.util.UUID

fun Route.webSocketRoutes() {
    webSocket("/ws/chat") {
        val token = call.request.queryParameters["token"]
        if (token == null) {
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Token missing"))
            return@webSocket
        }

        val decoded = try {
            JwtService.verifier.verify(token)
        } catch (e: Exception) {
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Invalid token"))
            return@webSocket
        }

        val userIdStr = decoded.getClaim("userId").asString()
        val userUuid = UUID.fromString(userIdStr)

        ConnectionManager.addConnection(userIdStr, this)

        try {
            incoming.consumeEach { frame ->
                if (frame is Frame.Text) {
                    val text = frame.readText()
                    val action = try {
                        Json.decodeFromString<WsAction>(text)
                    } catch (e: Exception) {
                        null
                    }

                    when (action) {
                        is WsAction.SendMessage -> {
                            val convId = UUID.fromString(action.conversationId)
                            val newMsg = transaction {
                                // Verify membership
                                val isMember = ConversationMembers.select {
                                    (ConversationMembers.conversationId eq convId) and 
                                    (ConversationMembers.userId eq userUuid)
                                }.count() > 0
                                
                                if (!isMember) return@transaction null

                                val msgId = Messages.insertAndGetId {
                                    it[conversationId] = convId
                                    it[senderId] = userUuid
                                    it[type] = action.type
                                    it[content] = action.content
                                    it[status] = "SENT"
                                    it[createdAt] = Instant.now()
                                    if (action.replyToId != null) {
                                        it[replyToId] = UUID.fromString(action.replyToId)
                                    }
                                }

                                val row = Messages.select { Messages.id eq msgId }.single()
                                MessageResponse(
                                    id = msgId.value.toString(),
                                    conversationId = row[Messages.conversationId].value.toString(),
                                    senderId = row[Messages.senderId].value.toString(),
                                    type = row[Messages.type],
                                    content = row[Messages.content],
                                    status = row[Messages.status],
                                    createdAt = row[Messages.createdAt].toEpochMilli(),
                                    replyToId = row[Messages.replyToId]?.value?.toString()
                                )
                            }

                            if (newMsg != null) {
                                // Broadcast to all members
                                val memberIds = transaction {
                                    ConversationMembers.select { ConversationMembers.conversationId eq convId }
                                        .map { it[ConversationMembers.userId].value.toString() }
                                }
                                
                                val event = WsEvent.NewMessage(newMsg)
                                memberIds.forEach { mId ->
                                    ConnectionManager.sendMessageToUser(mId, event)
                                }
                            }
                        }
                        is WsAction.SendTyping -> {
                            val convId = UUID.fromString(action.conversationId)
                            val memberIds = transaction {
                                ConversationMembers.select { ConversationMembers.conversationId eq convId }
                                    .map { it[ConversationMembers.userId].value.toString() }
                            }
                            val event = WsEvent.TypingIndicator(action.conversationId, userIdStr, action.isTyping)
                            memberIds.filter { it != userIdStr }.forEach { mId ->
                                ConnectionManager.sendMessageToUser(mId, event)
                            }
                        }
                        is WsAction.SendWebRtcOffer -> {
                            val event = WsEvent.WebRtcOffer(userIdStr, action.sdp)
                            ConnectionManager.sendMessageToUser(action.targetUserId, event)
                        }
                        is WsAction.SendWebRtcAnswer -> {
                            val event = WsEvent.WebRtcAnswer(userIdStr, action.sdp)
                            ConnectionManager.sendMessageToUser(action.targetUserId, event)
                        }
                        is WsAction.SendWebRtcIceCandidate -> {
                            val event = WsEvent.WebRtcIceCandidate(userIdStr, action.candidate, action.sdpMid, action.sdpMLineIndex)
                            ConnectionManager.sendMessageToUser(action.targetUserId, event)
                        }
                        is WsAction.SendEndCall -> {
                            val event = WsEvent.EndCall(userIdStr)
                            ConnectionManager.sendMessageToUser(action.targetUserId, event)
                        }
                        else -> {
                            // other actions like updating status to read/delivered
                        }
                    }
                }
            }
        } finally {
            ConnectionManager.removeConnection(userIdStr, this)
        }
    }
}
