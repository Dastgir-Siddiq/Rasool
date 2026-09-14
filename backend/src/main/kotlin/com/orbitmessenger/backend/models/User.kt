package com.orbitmessenger.backend.models

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestamp
import kotlinx.serialization.Serializable

object Users : UUIDTable("users") {
    val username = varchar("username", 50).uniqueIndex()
    val displayName = varchar("display_name", 100)
    val passwordHash = varchar("password_hash", 255)
    val avatarUrl = varchar("avatar_url", 255).nullable()
    val publicKey = text("public_key").nullable()
    val createdAt = timestamp("created_at")
}

@Serializable
data class UserResponse(
    val id: String,
    val username: String,
    val displayName: String,
    val avatarUrl: String?
)
