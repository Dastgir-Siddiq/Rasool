package com.orbitmessenger.backend.routes

import com.orbitmessenger.backend.models.UserResponse
import com.orbitmessenger.backend.models.Users
import com.orbitmessenger.backend.security.JwtService
import com.orbitmessenger.backend.security.PasswordService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

@Serializable
data class RegisterRequest(val username: String, val password: String, val displayName: String)

@Serializable
data class LoginRequest(val username: String, val password: String)

@Serializable
data class AuthResponse(val token: String, val user: UserResponse)

fun Route.authRoutes() {
    route("/api/v1/auth") {
        post("/register") {
            val req = call.receive<RegisterRequest>()
            
            // Validate
            if (req.username.isBlank() || req.password.length < 6) {
                call.respond(HttpStatusCode.BadRequest, "Invalid input")
                return@post
            }

            val result = transaction {
                val exists = Users.select { Users.username eq req.username }.count() > 0
                if (exists) return@transaction null

                val newId = Users.insertAndGetId {
                    it[username] = req.username
                    it[displayName] = req.displayName
                    it[passwordHash] = PasswordService.hash(req.password)
                    it[createdAt] = Instant.now()
                }
                
                UserResponse(
                    id = newId.value.toString(),
                    username = req.username,
                    displayName = req.displayName,
                    avatarUrl = null
                )
            }

            if (result == null) {
                call.respond(HttpStatusCode.Conflict, "Username taken")
            } else {
                val token = JwtService.generateToken(result.id)
                call.respond(HttpStatusCode.Created, AuthResponse(token, result))
            }
        }

        post("/login") {
            val req = call.receive<LoginRequest>()
            val userRow = transaction {
                Users.select { Users.username eq req.username }.singleOrNull()
            }

            if (userRow == null) {
                call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
                return@post
            }

            val isValid = PasswordService.verify(req.password, userRow[Users.passwordHash])
            if (isValid) {
                val userRes = UserResponse(
                    id = userRow[Users.id].value.toString(),
                    username = userRow[Users.username],
                    displayName = userRow[Users.displayName],
                    avatarUrl = userRow[Users.avatarUrl]
                )
                val token = JwtService.generateToken(userRes.id)
                call.respond(HttpStatusCode.OK, AuthResponse(token, userRes))
            } else {
                call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
            }
        }
    }
}
