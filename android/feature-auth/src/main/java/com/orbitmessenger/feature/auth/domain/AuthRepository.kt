package com.orbitmessenger.feature.auth.domain

import com.orbitmessenger.core.network.UserResponse

sealed class AuthResult {
    data class Success(val user: UserResponse) : AuthResult()
    data class Error(val message: String) : AuthResult()
    object Loading : AuthResult()
}

interface AuthRepository {
    suspend fun login(username: String, password: String): AuthResult
    suspend fun register(username: String, password: String, displayName: String): AuthResult
    suspend fun logout()
    fun getToken(): String?
}
