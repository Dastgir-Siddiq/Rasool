package com.orbitmessenger.backend

import com.orbitmessenger.backend.database.DatabaseFactory
import com.orbitmessenger.backend.models.ConversationMembers
import com.orbitmessenger.backend.models.Conversations
import com.orbitmessenger.backend.models.Messages
import com.orbitmessenger.backend.routes.authRoutes
import com.orbitmessenger.backend.routes.conversationRoutes
import com.orbitmessenger.backend.routes.mediaRoutes
import com.orbitmessenger.backend.routes.userRoutes
import com.orbitmessenger.backend.routes.webSocketRoutes
import com.orbitmessenger.backend.security.JwtService
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.server.http.content.staticFiles
import java.io.File
import java.time.Duration
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    try {
        DatabaseFactory.init()
        transaction {
            SchemaUtils.create(Conversations, ConversationMembers, Messages)
        }
    } catch (e: Exception) {
        println("Warning: Database connection failed. Please ensure PostgreSQL is running.")
    }

    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    install(ContentNegotiation) {
        json()
    }

    install(CORS) {
        anyHost()
    }

    install(Authentication) {
        jwt("auth-jwt") {
            verifier(JwtService.verifier)
            validate { credential ->
                if (credential.payload.getClaim("userId").asString() != "") {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }

    routing {
        authRoutes()
        userRoutes()
        conversationRoutes()
        webSocketRoutes()
        mediaRoutes()
        
        staticFiles("/uploads", File("uploads"))
    }
}
