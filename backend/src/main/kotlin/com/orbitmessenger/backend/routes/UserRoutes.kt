package com.orbitmessenger.backend.routes

import com.orbitmessenger.backend.models.UserResponse
import com.orbitmessenger.backend.models.Users
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.UUID

@Serializable
data class UpdateProfileRequest(
    val displayName: String?,
    val avatarUrl: String?
)

fun Route.userRoutes() {
    authenticate("auth-jwt") {
        route("/api/v1/users") {
            
            get("/me") {
                val principal = call.principal<JWTPrincipal>()
                val userIdStr = principal!!.payload.getClaim("userId").asString()
                val userUuid = UUID.fromString(userIdStr)

                val user = transaction {
                    Users.select { Users.id eq userUuid }.singleOrNull()?.let {
                        UserResponse(
                            id = it[Users.id].value.toString(),
                            username = it[Users.username],
                            displayName = it[Users.displayName],
                            avatarUrl = it[Users.avatarUrl]
                        )
                    }
                }

                if (user != null) {
                    call.respond(HttpStatusCode.OK, user)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }

            put("/me") {
                val req = call.receive<UpdateProfileRequest>()
                val principal = call.principal<JWTPrincipal>()
                val userIdStr = principal!!.payload.getClaim("userId").asString()
                val userUuid = UUID.fromString(userIdStr)

                transaction {
                    Users.update({ Users.id eq userUuid }) { statement ->
                        req.displayName?.let { statement[displayName] = it }
                        req.avatarUrl?.let { statement[avatarUrl] = it }
                    }
                }
                
                call.respond(HttpStatusCode.OK, mapOf("status" to "updated"))
            }

            get("/search") {
                val query = call.request.queryParameters["q"] ?: ""
                if (query.isBlank() || query.length < 3) {
                    call.respond(HttpStatusCode.BadRequest, "Query must be at least 3 characters")
                    return@get
                }

                val results = transaction {
                    // Search by exact username or partial display name
                    Users.select { 
                        (Users.username eq query) or (Users.displayName like "%$query%")
                    }.limit(20).map {
                        UserResponse(
                            id = it[Users.id].value.toString(),
                            username = it[Users.username],
                            displayName = it[Users.displayName],
                            avatarUrl = it[Users.avatarUrl]
                        )
                    }
                }

                call.respond(HttpStatusCode.OK, results)
            }
        }
    }
}
