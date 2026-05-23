package com.taskmanager.model

import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.time.Instant
import java.util.UUID

enum class UserRole { ADMIN, MEMBER }

@Entity
@Table(name = "users")
class User(
    @Column(nullable = false, unique = true)
    val email: String,

    @Column(name = "password_hash", nullable = false)
    var passwordHash: String,

    @Column(name = "display_name", nullable = false, length = 100)
    var displayName: String,

    @Column(name = "avatar_url")
    var avatarUrl: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: UserRole = UserRole.MEMBER,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
) {
    @Id
    @UuidGenerator
    var id: UUID? = null
}
