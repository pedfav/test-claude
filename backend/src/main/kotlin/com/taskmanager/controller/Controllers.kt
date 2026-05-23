package com.taskmanager.controller

import com.taskmanager.dto.*
import com.taskmanager.security.JwtService
import com.taskmanager.service.*
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.UUID

private fun String.bearerUserId(jwt: JwtService) = jwt.extractUserId(removePrefix("Bearer "))

// ---- Auth ----
@RestController
@RequestMapping("/api/auth")
class AuthController(private val authService: AuthService) {
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun register(@Valid @RequestBody req: RegisterRequest) = authService.register(req)

    @PostMapping("/login")
    fun login(@Valid @RequestBody req: LoginRequest) = authService.login(req)
}

// ---- Users ----
@RestController
@RequestMapping("/api/users")
class UserController(private val userService: UserService, private val jwt: JwtService) {
    @GetMapping("/me")
    fun me(@RequestHeader("Authorization") auth: String) = userService.getById(auth.bearerUserId(jwt))

    @GetMapping
    fun all() = userService.getAll()

    @GetMapping("/{id}")
    fun byId(@PathVariable id: UUID) = userService.getById(id)

    @PutMapping("/me")
    fun update(@RequestHeader("Authorization") auth: String, @Valid @RequestBody req: UpdateProfileRequest) =
        userService.updateProfile(auth.bearerUserId(jwt), req)
}

// ---- Boards ----
@RestController
@RequestMapping("/api/boards")
class BoardController(private val boardService: BoardService, private val jwt: JwtService) {
    @GetMapping
    fun list(@RequestHeader("Authorization") auth: String) = boardService.getBoardsForUser(auth.bearerUserId(jwt))

    @GetMapping("/{id}")
    fun get(@PathVariable id: UUID, @RequestHeader("Authorization") auth: String) =
        boardService.getBoardById(id, auth.bearerUserId(jwt))

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody req: CreateBoardRequest, @RequestHeader("Authorization") auth: String) =
        boardService.createBoard(req, auth.bearerUserId(jwt))

    @PutMapping("/{id}")
    fun update(@PathVariable id: UUID, @Valid @RequestBody req: UpdateBoardRequest, @RequestHeader("Authorization") auth: String) =
        boardService.updateBoard(id, req, auth.bearerUserId(jwt))

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: UUID, @RequestHeader("Authorization") auth: String) =
        boardService.deleteBoard(id, auth.bearerUserId(jwt))

    @PostMapping("/{id}/columns")
    @ResponseStatus(HttpStatus.CREATED)
    fun createColumn(@PathVariable id: UUID, @Valid @RequestBody req: CreateColumnRequest, @RequestHeader("Authorization") auth: String) =
        boardService.createColumn(id, req, auth.bearerUserId(jwt))

    @PutMapping("/columns/{columnId}")
    fun updateColumn(@PathVariable columnId: UUID, @Valid @RequestBody req: UpdateColumnRequest, @RequestHeader("Authorization") auth: String) =
        boardService.updateColumn(columnId, req, auth.bearerUserId(jwt))

    @DeleteMapping("/columns/{columnId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteColumn(@PathVariable columnId: UUID, @RequestHeader("Authorization") auth: String) =
        boardService.deleteColumn(columnId, auth.bearerUserId(jwt))
}

// ---- Tasks ----
@RestController
@RequestMapping("/api")
class TaskController(private val taskService: TaskService, private val jwt: JwtService) {
    @GetMapping("/boards/{boardId}/tasks")
    fun list(@PathVariable boardId: UUID, @RequestHeader("Authorization") auth: String) =
        taskService.getTasksForBoard(boardId, auth.bearerUserId(jwt))

    @GetMapping("/tasks/{id}")
    fun get(@PathVariable id: UUID, @RequestHeader("Authorization") auth: String) =
        taskService.getTaskById(id, auth.bearerUserId(jwt))

    @PostMapping("/boards/{boardId}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@PathVariable boardId: UUID, @Valid @RequestBody req: CreateTaskRequest, @RequestHeader("Authorization") auth: String) =
        taskService.createTask(boardId, req, auth.bearerUserId(jwt))

    @PutMapping("/tasks/{id}")
    fun update(@PathVariable id: UUID, @Valid @RequestBody req: UpdateTaskRequest, @RequestHeader("Authorization") auth: String) =
        taskService.updateTask(id, req, auth.bearerUserId(jwt))

    @PatchMapping("/tasks/{id}/move")
    fun move(@PathVariable id: UUID, @Valid @RequestBody req: MoveTaskRequest, @RequestHeader("Authorization") auth: String) =
        taskService.moveTask(id, req, auth.bearerUserId(jwt))

    @DeleteMapping("/tasks/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: UUID, @RequestHeader("Authorization") auth: String) =
        taskService.deleteTask(id, auth.bearerUserId(jwt))
}

// ---- Teams ----
@RestController
@RequestMapping("/api/teams")
class TeamController(private val teamService: TeamService, private val jwt: JwtService) {
    @GetMapping
    fun list(@RequestHeader("Authorization") auth: String) = teamService.getTeamsForUser(auth.bearerUserId(jwt))

    @GetMapping("/{id}")
    fun get(@PathVariable id: UUID, @RequestHeader("Authorization") auth: String) =
        teamService.getTeamById(id, auth.bearerUserId(jwt))

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody req: CreateTeamRequest, @RequestHeader("Authorization") auth: String) =
        teamService.createTeam(req, auth.bearerUserId(jwt))

    @PutMapping("/{id}")
    fun update(@PathVariable id: UUID, @Valid @RequestBody req: UpdateTeamRequest, @RequestHeader("Authorization") auth: String) =
        teamService.updateTeam(id, req, auth.bearerUserId(jwt))

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: UUID, @RequestHeader("Authorization") auth: String) =
        teamService.deleteTeam(id, auth.bearerUserId(jwt))

    @PostMapping("/{id}/members")
    fun addMember(@PathVariable id: UUID, @Valid @RequestBody req: AddMemberRequest, @RequestHeader("Authorization") auth: String) =
        teamService.addMember(id, req, auth.bearerUserId(jwt))

    @DeleteMapping("/{id}/members/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun removeMember(@PathVariable id: UUID, @PathVariable memberId: UUID, @RequestHeader("Authorization") auth: String) =
        teamService.removeMember(id, memberId, auth.bearerUserId(jwt))
}
