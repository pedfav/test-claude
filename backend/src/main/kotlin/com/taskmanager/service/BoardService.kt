package com.taskmanager.service

import com.taskmanager.dto.*
import com.taskmanager.model.Board
import com.taskmanager.model.BoardColumn
import com.taskmanager.repository.*
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class BoardService(
    private val boardRepository: BoardRepository,
    private val columnRepository: BoardColumnRepository,
    private val teamRepository: TeamRepository,
    private val userRepository: UserRepository
) {
    fun getBoardsForUser(userId: UUID): List<BoardDto> =
        boardRepository.findAccessibleByUserId(userId).map { it.toDto() }

    fun getBoardById(boardId: UUID, userId: UUID): BoardDto {
        val board = findBoard(boardId)
        checkAccess(board, userId)
        return board.toDto()
    }

    @Transactional
    fun createBoard(req: CreateBoardRequest, userId: UUID): BoardDto {
        val creator = userRepository.findById(userId).orElseThrow { EntityNotFoundException("User not found") }
        val team = req.teamId?.let { teamRepository.findById(it).orElseThrow { EntityNotFoundException("Team not found") } }
        val board = boardRepository.save(Board(name = req.name, description = req.description, team = team, createdBy = creator))
        listOf("To Do" to "#6366f1", "In Progress" to "#f59e0b", "Done" to "#10b981").forEachIndexed { i, (name, color) ->
            board.columns.add(columnRepository.save(BoardColumn(board = board, name = name, position = i, color = color)))
        }
        return board.toDto()
    }

    @Transactional
    fun updateBoard(boardId: UUID, req: UpdateBoardRequest, userId: UUID): BoardDto {
        val board = findBoard(boardId).also { checkAccess(it, userId) }
        req.name?.let { board.name = it }
        req.description?.let { board.description = it }
        return boardRepository.save(board).toDto()
    }

    @Transactional
    fun deleteBoard(boardId: UUID, userId: UUID) {
        val board = findBoard(boardId).also { checkOwner(it, userId) }
        boardRepository.delete(board)
    }

    @Transactional
    fun createColumn(boardId: UUID, req: CreateColumnRequest, userId: UUID): ColumnDto {
        val board = findBoard(boardId).also { checkAccess(it, userId) }
        val pos = (board.columns.maxOfOrNull { it.position } ?: -1) + 1
        return columnRepository.save(BoardColumn(board = board, name = req.name, position = pos, color = req.color)).toDto()
    }

    @Transactional
    fun updateColumn(columnId: UUID, req: UpdateColumnRequest, userId: UUID): ColumnDto {
        val col = findColumn(columnId).also { checkAccess(it.board, userId) }
        req.name?.let { col.name = it }
        req.color?.let { col.color = it }
        req.position?.let { col.position = it }
        return columnRepository.save(col).toDto()
    }

    @Transactional
    fun deleteColumn(columnId: UUID, userId: UUID) {
        val col = findColumn(columnId).also { checkAccess(it.board, userId) }
        columnRepository.delete(col)
    }

    private fun findBoard(id: UUID) = boardRepository.findById(id).orElseThrow { EntityNotFoundException("Board not found") }
    private fun findColumn(id: UUID) = columnRepository.findById(id).orElseThrow { EntityNotFoundException("Column not found") }

    private fun checkAccess(board: Board, userId: UUID) {
        val ok = board.createdBy.id?.equals(userId) == true || board.team?.members?.any { it.user.id?.equals(userId) == true } == true
        if (!ok) throw SecurityException("Access denied")
    }

    private fun checkOwner(board: Board, userId: UUID) {
        if (board.createdBy.id?.equals(userId) != true) throw SecurityException("Owner access required")
    }
}
