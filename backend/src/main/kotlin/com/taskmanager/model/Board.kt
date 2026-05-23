package com.taskmanager.model

import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "boards")
class Board(
    @Column(nullable = false, length = 100)
    var name: String,

    @Column
    var description: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    var team: Team? = null,

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

    @OneToMany(mappedBy = "board", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("position ASC")
    val columns: MutableList<BoardColumn> = mutableListOf()
}

@Entity
@Table(name = "columns")
class BoardColumn(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    val board: Board,

    @Column(nullable = false, length = 100)
    var name: String,

    @Column(nullable = false)
    var position: Int = 0,

    @Column(length = 7)
    var color: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()
) {
    @Id
    @UuidGenerator
    var id: UUID? = null

    @OneToMany(mappedBy = "column", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("position ASC")
    val tasks: MutableList<Task> = mutableListOf()
}
