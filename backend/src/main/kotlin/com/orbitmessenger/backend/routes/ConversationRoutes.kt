package com.orbitmessenger.backend.routes

import com.orbitmessenger.backend.models.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.util.UUID

fun Route.conversationRoutes() {
    authenticate("auth-jwt") {
        route("/api/v1/conversations") {
            
            // Get all conversations for the user
            get {
                val principal = call.principal<JWTPrincipal>()
                val userIdStr = principal!!.payload.getClaim("userId").asString()
                val userUuid = UUID.fromString(userIdStr)

                val result = transaction {
                    // Find conversation IDs where the user is a member
                    val convIds = ConversationMembers
                        .select { ConversationMembers.userId eq userUuid }
                        .map { it[ConversationMembers.conversationId].value }

                    // Fetch those conversations
                    convIds.map { cId ->
                        val convRow = Conversations.select { Conversations.id eq cId }.single()
                        
                        // Get members of this conversation
                        val members = (ConversationMembers innerJoin Users)
                            .select { ConversationMembers.conversationId eq cId }
                            .map { 
                                UserResponse(
                                    id = it[Users.id].value.toString(),
                                    username = it[Users.username],
                                    displayName = it[Users.displayName],
                                    avatarUrl = it[Users.avatarUrl]
                                )
                            }
                            
                        ConversationResponse(
                            id = cId.toString(),
                            isGroup = convRow[Conversations.isGroup],
                            name = convRow[Conversations.name],
                            participants = members
                        )
                    }
                }
                call.respond(HttpStatusCode.OK, result)
            }

            // Create a new conversation
            post {
                val req = call.receive<CreateConversationRequest>()
                val principal = call.principal<JWTPrincipal>()
                val userIdStr = principal!!.payload.getClaim("userId").asString()
                val userUuid = UUID.fromString(userIdStr)
                
                val allParticipantIds = req.participantIds.map { UUID.fromString(it) }.toMutableSet()
                allParticipantIds.add(userUuid) // Ensure creator is included

                if (!req.isGroup && allParticipantIds.size != 2) {
                    call.respond(HttpStatusCode.BadRequest, "Direct messages must have exactly 2 participants")
                    return@post
                }

                val newConvId = transaction {
                    val cId = Conversations.insertAndGetId {
                        it[isGroup] = req.isGroup
                        it[name] = req.name
                        it[createdAt] = Instant.now()
                    }
                    
                    allParticipantIds.forEach { pId ->
                        ConversationMembers.insert {
                            it[conversationId] = cId
                            it[userId] = pId
                            it[role] = if (pId == userUuid) "ADMIN" else "MEMBER"
                        }
                    }
                    cId
                }
                
                call.respond(HttpStatusCode.Created, mapOf("id" to newConvId.value.toString()))
            }
            
            // Get messages for a conversation
            get("/{id}/messages") {
                val convIdStr = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                val convId = UUID.fromString(convIdStr)
                val principal = call.principal<JWTPrincipal>()
                val userIdStr = principal!!.payload.getClaim("userId").asString()
                val userUuid = UUID.fromString(userIdStr)
                
                val messages = transaction {
                    // Verify membership
                    val isMember = ConversationMembers.select {
                        (ConversationMembers.conversationId eq convId) and (ConversationMembers.userId eq userUuid)
                    }.count() > 0
                    
                    if (!isMember) {
                        return@transaction null
                    }
                    
                    Messages.select { Messages.conversationId eq convId }
                        .orderBy(Messages.createdAt to SortOrder.ASC)
                        .limit(50) // Pagination logic would go here
                        .map {
                            MessageResponse(
                                id = it[Messages.id].value.toString(),
                                conversationId = it[Messages.conversationId].value.toString(),
                                senderId = it[Messages.senderId].value.toString(),
                                type = it[Messages.type],
                                content = it[Messages.content],
                                status = it[Messages.status],
                                createdAt = it[Messages.createdAt].toEpochMilli(),
                                replyToId = it[Messages.replyToId]?.value?.toString()
                            )
                        }
                }
                
                if (messages == null) {
                    call.respond(HttpStatusCode.Forbidden, "Not a member of this conversation")
                } else {
                    call.respond(HttpStatusCode.OK, messages)
                }
            }
        }
    }
}
