package com.taskmanager.service

import com.taskmanager.dto.CreateBoardRequest
import com.taskmanager.dto.CreateColumnRequest
import com.taskmanager.dto.UpdateBoardRequest
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

class BoardServiceTest {

    private val boardRepository: BoardRepository = mockk()
    private val columnRepository: BoardColumnRepository = mockk()
    private val teamRepository: TeamRepository = mockk()
    private val userRepository: UserRepository = mockk()

    private val service = BoardService(boardRepository, columnRepository, teamRepository, userRepository)

    private val userId = UUID.randomUUID()
    private val user = User(email = "x@x.com", passwordHash = "h", displayName = "X").also { it.id = userId }
    private val boardId = UUID.randomUUID()
    private val board = Board(name = "B", createdBy = user).also { it.id = boardId }

    @Test
    fun `getBoardsForUser returns list`() {
        every { boardRepository.findAccessibleByUserId(userId) } returns listOf(board)
        val result = service.getBoardsForUser(userId)
        assertEquals(1, result.size)
    }

    @Test
    fun `getBoardById throws for unauthorized user`() {
        every { boardRepository.findById(boardId) } returns Optional.of(board)
        assertThrows<SecurityException> { service.getBoardById(boardId, UUID.randomUUID()) }
    }

    @Test
    fun `createBoard saves board and three default columns`() {
        val savedBoardId = UUID.randomUUID()
        val savedBoard = Board(name = "B", createdBy = user).also { it.id = savedBoardId }
        every { userRepository.findById(userId) } returns Optional.of(user)
        every { boardRepository.save(any()) } returns savedBoard
        every { columnRepository.save(any()) } answers { (firstArg<BoardColumn>()).also { it.id = UUID.randomUUID() } }

        service.createBoard(CreateBoardRequest("B"), userId)

        verify(exactly = 3) { columnRepository.save(any()) }
    }

    @Test
    fun `updateBoard modifies name`() {
        every { boardRepository.findById(boardId) } returns Optional.of(board)
        every { boardRepository.save(any()) } answers { firstArg() }

        val result = service.updateBoard(boardId, UpdateBoardRequest(name = "New"), userId)

        assertEquals("New", result.name)
    }

    @Test
    fun `deleteBoard throws for non-owner`() {
        every { boardRepository.findById(boardId) } returns Optional.of(board)
        assertThrows<SecurityException> { service.deleteBoard(boardId, UUID.randomUUID()) }
    }

    @Test
    fun `createColumn appends to board`() {
        every { boardRepository.findById(boardId) } returns Optional.of(board)
        every { columnRepository.save(any()) } answers { (firstArg<BoardColumn>()).also { it.id = UUID.randomUUID() } }

        val result = service.createColumn(boardId, CreateColumnRequest("Review", "#aabbcc"), userId)

        assertEquals("Review", result.name)
        assertEquals("#aabbcc", result.color)
        assertEquals(0, result.position)
    }

    @Test
    fun `getBoardById throws EntityNotFoundException for unknown board`() {
        every { boardRepository.findById(any()) } returns Optional.empty()
        assertThrows<EntityNotFoundException> { service.getBoardById(UUID.randomUUID(), userId) }
    }
}
