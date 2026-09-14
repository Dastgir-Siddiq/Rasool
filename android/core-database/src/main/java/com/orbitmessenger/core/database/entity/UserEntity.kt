package com.orbitmessenger.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val username: String,
    val displayName: String,
    val avatarUrl: String?,
    val publicKey: String?
)
