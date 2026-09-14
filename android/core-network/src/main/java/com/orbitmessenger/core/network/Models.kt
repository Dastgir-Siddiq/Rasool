package com.orbitmessenger.core.network

data class UserResponse(
    val id: String,
    val username: String,
    val displayName: String,
    val avatarUrl: String?
)

data class AuthResponse(
    val token: String,
    val user: UserResponse
)
