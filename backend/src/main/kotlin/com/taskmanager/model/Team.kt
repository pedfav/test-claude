package com.taskmanager.model

import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.io.Serializable
import java.time.Instant
import java.util.UUID

enum class TeamRole { OWNER, ADMIN, MEMBER }

@Entity
@Table(name = "teams")
class Team(
    @Column(nullable = false, length = 100)
    var name: String,

    @Column
    var description: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    val createdBy: User,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
) {
    @Id
    @UuidGenerator
    var id: UUID? = null

    @OneToMany(mappedBy = "team", cascade = [CascadeType.ALL], orphanRemoval = true)
    val members: MutableList<TeamMember> = mutableListOf()
}

@Embeddable
data class TeamMemberId(
    @Column(name = "team_id") val teamId: UUID,
    @Column(name = "user_id") val userId: UUID
) : Serializable

@Entity
@Table(name = "team_members")
class TeamMember(
    @EmbeddedId val id: TeamMemberId,

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("teamId") @JoinColumn(name = "team_id")
    val team: Team,

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId") @JoinColumn(name = "user_id")
    val user: User,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: TeamRole = TeamRole.MEMBER,

    @Column(name = "joined_at", nullable = false, updatable = false)
    val joinedAt: Instant = Instant.now()
)
