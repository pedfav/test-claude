package com.taskmanager.service

import com.taskmanager.dto.CreateTaskRequest
import com.taskmanager.dto.MoveTaskRequest
import com.taskmanager.dto.UpdateTaskRequest
import com.taskmanager.model.*
import com.taskmanager.repository.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.persistence.EntityNotFoundException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Optional
import java.util.UUID

class TaskServiceTest {

    private val taskRepository: TaskRepository = mockk()
    private val boardRepository: BoardRepository = mockk()
    private val columnRepository: BoardColumnRepository = mockk()
    private val userRepository: UserRepository = mockk()

    private val service = TaskService(taskRepository, boardRepository, columnRepository, userRepository)

    private val ownerId = UUID.randomUUID()
    private val owner = User(email = "o@o.com", passwordHash = "h", displayName = "Owner").also { it.id = ownerId }
    private val boardId = UUID.randomUUID()
    private val board = Board(name = "B", createdBy = owner).also { it.id = boardId }
    private val columnId = UUID.randomUUID()
    private val column = BoardColumn(board = board, name = "ToDo", position = 0).also { it.id = columnId }
    private val taskId = UUID.randomUUID()
    private val task = Task(title = "T", board = board, column = column, createdBy = owner).also { it.id = taskId }

    @Test
    fun `getTaskById returns task for owner`() {
        every { taskRepository.findById(taskId) } returns Optional.of(task)
        assertEquals(taskId, service.getTaskById(taskId, ownerId).id)
    }

    @Test
    fun `getTaskById throws for non-member`() {
        every { taskRepository.findById(taskId) } returns Optional.of(task)
        assertThrows<SecurityException> { service.getTaskById(taskId, UUID.randomUUID()) }
    }

    @Test
    fun `createTask positions at end of column`() {
        every { boardRepository.findById(boardId) } returns Optional.of(board)
        every { columnRepository.findById(columnId) } returns Optional.of(column)
        every { userRepository.findById(ownerId) } returns Optional.of(owner)
        every { taskRepository.findMaxPositionByColumnId(columnId) } returns 2
        every { taskRepository.save(any()) } answers { (firstArg<Task>()).also { it.id = UUID.randomUUID() } }

        val result = service.createTask(boardId, CreateTaskRequest("New", columnId = columnId), ownerId)

        assertEquals("New", result.title)
        assertEquals(3, result.position)
    }

    @Test
    fun `createTask throws when board not found`() {
        every { boardRepository.findById(any()) } returns Optional.empty()
        assertThrows<EntityNotFoundException> {
            service.createTask(UUID.randomUUID(), CreateTaskRequest("T", columnId = UUID.randomUUID()), ownerId)
        }
    }

    @Test
    fun `updateTask updates priority and title`() {
        every { taskRepository.findById(taskId) } returns Optional.of(task)
        every { taskRepository.save(any()) } answers { firstArg() }

        val result = service.updateTask(taskId, UpdateTaskRequest(title = "Updated", priority = TaskPriority.URGENT), ownerId)

        assertEquals("Updated", result.title)
        assertEquals(TaskPriority.URGENT, result.priority)
    }

    @Test
    fun `deleteTask removes task`() {
        every { taskRepository.findById(taskId) } returns Optional.of(task)
        every { taskRepository.delete(task) } returns Unit

        service.deleteTask(taskId, ownerId)

        verify { taskRepository.delete(task) }
    }

    @Test
    fun `moveTask changes column and position`() {
        val targetColId = UUID.randomUUID()
        val targetCol = BoardColumn(board = board, name = "Done", position = 1).also { it.id = targetColId }
        every { taskRepository.findById(taskId) } returns Optional.of(task)
        every { columnRepository.findById(targetColId) } returns Optional.of(targetCol)
        every { taskRepository.findByColumnIdOrderByPosition(targetColId) } returns emptyList()
        every { taskRepository.save(any()) } answers { firstArg() }

        val result = service.moveTask(taskId, MoveTaskRequest(targetColId, 0), ownerId)

        assertEquals(targetColId, result.columnId)
        assertEquals(0, result.position)
    }
}
