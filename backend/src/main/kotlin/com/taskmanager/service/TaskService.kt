package com.taskmanager.service

import com.taskmanager.dto.*
import com.taskmanager.model.Task
import com.taskmanager.repository.*
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class TaskService(
    private val taskRepository: TaskRepository,
    private val boardRepository: BoardRepository,
    private val columnRepository: BoardColumnRepository,
    private val userRepository: UserRepository
) {
    fun getTasksForBoard(boardId: UUID, userId: UUID): List<TaskDto> {
        val board = findBoard(boardId).also { checkAccess(it, userId) }
        return taskRepository.findByBoardIdOrdered(board.id!!).map { it.toDto() }
    }

    fun getTaskById(taskId: UUID, userId: UUID): TaskDto {
        val task = findTask(taskId).also { checkAccess(it.board, userId) }
        return task.toDto()
    }

    @Transactional
    fun createTask(boardId: UUID, req: CreateTaskRequest, userId: UUID): TaskDto {
        val board = findBoard(boardId).also { checkAccess(it, userId) }
        val column = columnRepository.findById(req.columnId).orElseThrow { EntityNotFoundException("Column not found") }
        require(column.board.id?.equals(boardId) == true) { "Column does not belong to this board" }
        val creator = userRepository.findById(userId).orElseThrow { EntityNotFoundException("User not found") }
        val assignee = req.assigneeId?.let { userRepository.findById(it).orElseThrow { EntityNotFoundException("Assignee not found") } }
        val pos = (taskRepository.findMaxPositionByColumnId(column.id!!) ?: -1) + 1
        return taskRepository.save(Task(
            title = req.title, description = req.description, board = board, column = column,
            assignee = assignee, createdBy = creator, priority = req.priority,
            position = pos, dueDate = req.dueDate, labels = req.labels,
            checklist = req.checklist.map { com.taskmanager.model.ChecklistItem(it.id, it.text, it.done) }
        )).toDto()
    }

    @Transactional
    fun updateTask(taskId: UUID, req: UpdateTaskRequest, userId: UUID): TaskDto {
        val task = findTask(taskId).also { checkAccess(it.board, userId) }
        req.title?.let { task.title = it }
        req.description?.let { task.description = it }
        req.priority?.let { task.priority = it }
        req.position?.let { task.position = it }
        req.dueDate?.let { task.dueDate = it }
        req.labels?.let { task.labels = it }
        req.checklist?.let { task.checklist = it.map { item -> com.taskmanager.model.ChecklistItem(item.id, item.text, item.done) } }
        req.columnId?.let { colId ->
            val col = columnRepository.findById(colId).orElseThrow { EntityNotFoundException("Column not found") }
            require(col.board.id?.equals(task.board.id) == true) { "Column does not belong to this board" }
            task.column = col
        }
        req.assigneeId?.let { task.assignee = userRepository.findById(it).orElseThrow { EntityNotFoundException("Assignee not found") } }
        task.updatedAt = Instant.now()
        return taskRepository.save(task).toDto()
    }

    @Transactional
    fun moveTask(taskId: UUID, req: MoveTaskRequest, userId: UUID): TaskDto {
        val task = findTask(taskId).also { checkAccess(it.board, userId) }
        val targetCol = columnRepository.findById(req.columnId).orElseThrow { EntityNotFoundException("Column not found") }
        require(targetCol.board.id?.equals(task.board.id) == true) { "Column does not belong to this board" }
        val others = taskRepository.findByColumnIdOrderByPosition(req.columnId).filter { it.id != taskId }
        task.column = targetCol
        task.position = req.position
        task.updatedAt = Instant.now()
        others.forEachIndexed { i, t ->
            val newPos = if (i < req.position) i else i + 1
            if (t.position != newPos) { t.position = newPos; taskRepository.save(t) }
        }
        return taskRepository.save(task).toDto()
    }

    @Transactional
    fun deleteTask(taskId: UUID, userId: UUID) {
        val task = findTask(taskId).also { checkAccess(it.board, userId) }
        taskRepository.delete(task)
    }

    private fun findTask(id: UUID) = taskRepository.findById(id).orElseThrow { EntityNotFoundException("Task not found") }
    private fun findBoard(id: UUID) = boardRepository.findById(id).orElseThrow { EntityNotFoundException("Board not found") }

    private fun checkAccess(board: com.taskmanager.model.Board, userId: UUID) {
        val ok = board.createdBy.id?.equals(userId) == true || board.team?.members?.any { it.user.id?.equals(userId) == true } == true
        if (!ok) throw SecurityException("Access denied")
    }
}
