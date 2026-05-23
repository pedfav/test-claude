package com.taskmanager.repository

import com.taskmanager.model.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface UserRepository : JpaRepository<User, UUID> {
    fun findByEmail(email: String): User?
    fun existsByEmail(email: String): Boolean
}

@Repository
interface TeamRepository : JpaRepository<Team, UUID> {
    @Query("SELECT t FROM Team t JOIN t.members m WHERE m.user.id = :userId")
    fun findByMemberUserId(@Param("userId") userId: UUID): List<Team>
}

@Repository
interface TeamMemberRepository : JpaRepository<TeamMember, TeamMemberId> {
    fun findByTeamIdAndUserId(teamId: UUID, userId: UUID): TeamMember?
    fun existsByTeamIdAndUserId(teamId: UUID, userId: UUID): Boolean
    fun findAllByTeamId(teamId: UUID): List<TeamMember>
}

@Repository
interface BoardRepository : JpaRepository<Board, UUID> {
    fun findByTeamId(teamId: UUID): List<Board>

    @Query("""
        SELECT DISTINCT b FROM Board b
        LEFT JOIN b.team t
        LEFT JOIN t.members m
        WHERE b.createdBy.id = :userId OR m.user.id = :userId
    """)
    fun findAccessibleByUserId(@Param("userId") userId: UUID): List<Board>
}

@Repository
interface BoardColumnRepository : JpaRepository<BoardColumn, UUID> {
    fun findByBoardIdOrderByPosition(boardId: UUID): List<BoardColumn>
}

@Repository
interface TaskRepository : JpaRepository<Task, UUID> {
    fun findByColumnIdOrderByPosition(columnId: UUID): List<Task>

    @Query("SELECT MAX(t.position) FROM Task t WHERE t.column.id = :columnId")
    fun findMaxPositionByColumnId(@Param("columnId") columnId: UUID): Int?

    @Query("SELECT t FROM Task t WHERE t.board.id = :boardId ORDER BY t.column.position, t.position")
    fun findByBoardIdOrdered(@Param("boardId") boardId: UUID): List<Task>
}
