package com.taskmanager.dto

import com.taskmanager.model.*
import jakarta.validation.constraints.*
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

// ---- Auth ----
data class LoginRequest(
    @field:Email @field:NotBlank val email: String,
    @field:NotBlank val password: String
)

data class RegisterRequest(
    @field:Email @field:NotBlank val email: String,
    @field:NotBlank @field:Size(min = 8) val password: String,
    @field:NotBlank @field:Size(max = 100) val displayName: String
)

data class AuthResponse(val token: String, val user: UserDto)

// ---- User ----
data class UserDto(
    val id: UUID, val email: String, val displayName: String,
    val avatarUrl: String?, val role: UserRole, val createdAt: Instant
)

data class UpdateProfileRequest(
    @field:NotBlank @field:Size(max = 100) val displayName: String,
    val avatarUrl: String? = null
)

fun User.toDto() = UserDto(id!!, email, displayName, avatarUrl, role, createdAt)

// ---- Team ----
data class TeamDto(
    val id: UUID, val name: String, val description: String?,
    val createdBy: UserDto, val members: List<TeamMemberDto>, val createdAt: Instant
)

data class TeamMemberDto(val user: UserDto, val role: TeamRole, val joinedAt: Instant)

data class CreateTeamRequest(
    @field:NotBlank @field:Size(max = 100) val name: String,
    val description: String? = null
)

data class UpdateTeamRequest(
    @field:Size(max = 100) val name: String? = null,
    val description: String? = null
)

data class AddMemberRequest(
    @field:NotNull val userId: UUID,
    val role: TeamRole = TeamRole.MEMBER
)

fun TeamMember.toDto() = TeamMemberDto(user.toDto(), role, joinedAt)
fun Team.toDto() = TeamDto(id!!, name, description, createdBy.toDto(), members.map { it.toDto() }, createdAt)

// ---- Board ----
data class BoardDto(
    val id: UUID, val name: String, val description: String?,
    val teamId: UUID?, val createdBy: UserDto, val columns: List<ColumnDto>,
    val createdAt: Instant, val updatedAt: Instant
)

data class ColumnDto(
    val id: UUID, val boardId: UUID, val name: String,
    val position: Int, val color: String?, val tasks: List<TaskDto>
)

data class CreateBoardRequest(
    @field:NotBlank @field:Size(max = 100) val name: String,
    val description: String? = null,
    val teamId: UUID? = null
)

data class UpdateBoardRequest(
    @field:Size(max = 100) val name: String? = null,
    val description: String? = null
)

data class CreateColumnRequest(
    @field:NotBlank @field:Size(max = 100) val name: String,
    val color: String? = null
)

data class UpdateColumnRequest(
    @field:Size(max = 100) val name: String? = null,
    val color: String? = null,
    val position: Int? = null
)

fun BoardColumn.toDto() = ColumnDto(id!!, board.id!!, name, position, color, tasks.map { it.toDto() })
fun Board.toDto() = BoardDto(id!!, name, description, team?.id, createdBy.toDto(), columns.map { it.toDto() }, createdAt, updatedAt)

// ---- Task ----
data class ChecklistItemDto(val id: String, val text: String, val done: Boolean)

data class TaskDto(
    val id: UUID, val title: String, val description: String?,
    val boardId: UUID, val columnId: UUID, val assignee: UserDto?,
    val createdBy: UserDto, val priority: TaskPriority, val position: Int,
    val dueDate: LocalDate?, val labels: List<String>,
    val checklist: List<ChecklistItemDto>,
    val createdAt: Instant, val updatedAt: Instant
)

data class CreateTaskRequest(
    @field:NotBlank @field:Size(max = 255) val title: String,
    val description: String? = null,
    @field:NotNull val columnId: UUID,
    val assigneeId: UUID? = null,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val dueDate: LocalDate? = null,
    val labels: List<String> = emptyList(),
    val checklist: List<ChecklistItemDto> = emptyList()
)

data class UpdateTaskRequest(
    @field:Size(max = 255) val title: String? = null,
    val description: String? = null,
    val columnId: UUID? = null,
    val assigneeId: UUID? = null,
    val priority: TaskPriority? = null,
    val position: Int? = null,
    val dueDate: LocalDate? = null,
    val labels: List<String>? = null,
    val checklist: List<ChecklistItemDto>? = null
)

data class MoveTaskRequest(
    @field:NotNull val columnId: UUID,
    @field:NotNull val position: Int
)

fun Task.toDto() = TaskDto(
    id!!, title, description, board.id!!, column.id!!,
    assignee?.toDto(), createdBy.toDto(), priority, position,
    dueDate, labels,
    checklist.map { ChecklistItemDto(it.id, it.text, it.done) },
    createdAt, updatedAt
)
