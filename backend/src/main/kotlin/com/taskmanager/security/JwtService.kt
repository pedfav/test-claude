package com.taskmanager.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey

@Service
class JwtService(
    @Value("\${app.jwt.secret}") private val secret: String,
    @Value("\${app.jwt.expiration-ms}") private val expirationMs: Long
) {
    private val signingKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(secret.toByteArray())
    }

    fun generateToken(userId: UUID, email: String): String =
        Jwts.builder()
            .subject(email)
            .claim("userId", userId.toString())
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + expirationMs))
            .signWith(signingKey)
            .compact()

    fun extractEmail(token: String): String = claims(token).subject

    fun extractUserId(token: String): UUID =
        UUID.fromString(claims(token).get("userId", String::class.java))

    fun isValid(token: String): Boolean = runCatching {
        claims(token).expiration.after(Date())
    }.getOrDefault(false)

    private fun claims(token: String) =
        Jwts.parser().verifyWith(signingKey).build()
            .parseSignedClaims(token).payload
}
