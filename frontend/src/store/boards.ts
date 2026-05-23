import { defineStore } from 'pinia'
import { ref } from 'vue'
import { boardsApi } from '@/api/boards'
import { tasksApi } from '@/api/tasks'
import type { Board, Column, Task, CreateTaskRequest, MoveTaskRequest } from '@/api/types'

export const useBoardStore = defineStore('boards', () => {
  const boards = ref<Board[]>([])
  const currentBoard = ref<Board | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  async function fetchBoards() {
    loading.value = true
    error.value = null
    try {
      boards.value = await boardsApi.getAll()
    } catch (e: any) {
      error.value = e.response?.data?.detail || 'Failed to load boards'
    } finally {
      loading.value = false
    }
  }

  async function fetchBoard(id: string) {
    loading.value = true
    error.value = null
    try {
      currentBoard.value = await boardsApi.getById(id)
    } catch (e: any) {
      error.value = e.response?.data?.detail || 'Failed to load board'
    } finally {
      loading.value = false
    }
  }

  async function createBoard(name: string, description?: string) {
    const board = await boardsApi.create({ name, description })
    boards.value.push(board)
    return board
  }

  async function deleteBoard(id: string) {
    await boardsApi.delete(id)
    boards.value = boards.value.filter((b) => b.id !== id)
    if (currentBoard.value?.id === id) currentBoard.value = null
  }

  async function createTask(boardId: string, data: CreateTaskRequest) {
    const task = await tasksApi.create(boardId, data)
    if (currentBoard.value?.id === boardId) {
      const col = currentBoard.value.columns.find((c) => c.id === data.columnId)
      if (col) col.tasks.push(task)
    }
    return task
  }

  async function moveTask(taskId: string, request: MoveTaskRequest, fromColumnId: string) {
    const updatedTask = await tasksApi.move(taskId, request)
    if (!currentBoard.value) return updatedTask

    // Remove from source column
    const fromCol = currentBoard.value.columns.find((c) => c.id === fromColumnId)
    if (fromCol) {
      fromCol.tasks = fromCol.tasks.filter((t) => t.id !== taskId)
    }

    // Insert into target column at correct position
    const toCol = currentBoard.value.columns.find((c) => c.id === request.columnId)
    if (toCol) {
      toCol.tasks = toCol.tasks.filter((t) => t.id !== taskId)
      toCol.tasks.splice(request.position, 0, updatedTask)
    }

    return updatedTask
  }

  async function updateTask(taskId: string, data: Partial<Task>) {
    const updated = await tasksApi.update(taskId, data)
    if (currentBoard.value) {
      for (const col of currentBoard.value.columns) {
        const idx = col.tasks.findIndex((t) => t.id === taskId)
        if (idx !== -1) {
          col.tasks[idx] = updated
          break
        }
      }
    }
    return updated
  }

  async function deleteTask(taskId: string, columnId: string) {
    await tasksApi.delete(taskId)
    if (currentBoard.value) {
      const col = currentBoard.value.columns.find((c) => c.id === columnId)
      if (col) col.tasks = col.tasks.filter((t) => t.id !== taskId)
    }
  }

  async function createColumn(boardId: string, name: string, color?: string) {
    const column = await boardsApi.createColumn(boardId, name, color)
    if (currentBoard.value?.id === boardId) {
      currentBoard.value.columns.push({ ...column, tasks: [] })
    }
    return column
  }

  async function deleteColumn(columnId: string) {
    await boardsApi.deleteColumn(columnId)
    if (currentBoard.value) {
      currentBoard.value.columns = currentBoard.value.columns.filter((c) => c.id !== columnId)
    }
  }

  return {
    boards, currentBoard, loading, error,
    fetchBoards, fetchBoard, createBoard, deleteBoard,
    createTask, moveTask, updateTask, deleteTask,
    createColumn, deleteColumn
  }
})
