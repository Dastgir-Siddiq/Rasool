package com.orbitmessenger.backend.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

object JwtService {
    private val secret = System.getenv("JWT_SECRET") ?: "super_secret_jwt_key"
    private val issuer = System.getenv("JWT_ISSUER") ?: "orbit_messenger"
    private val audience = System.getenv("JWT_AUDIENCE") ?: "orbit_messenger_users"
    private val algorithm = Algorithm.HMAC256(secret)

    val verifier = JWT.require(algorithm)
        .withIssuer(issuer)
        .withAudience(audience)
        .build()

    fun generateToken(userId: String): String {
        return JWT.create()
        .withAudience(audience)
        .withIssuer(issuer)
        .withClaim("userId", userId)
        .withExpiresAt(Date(System.currentTimeMillis() + 86400000)) // 24 hours
        .sign(algorithm)
    }
}
