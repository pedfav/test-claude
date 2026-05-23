import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useBoardStore } from '@/store/boards'
import * as boardsApi from '@/api/boards'
import * as tasksApi from '@/api/tasks'

vi.mock('@/api/boards')
vi.mock('@/api/tasks')

const mockUser = {
  id: 'u1', email: 'test@test.com', displayName: 'Test', role: 'MEMBER' as const,
  createdAt: new Date().toISOString()
}

const mockColumn = {
  id: 'col1', boardId: 'b1', name: 'To Do', position: 0, tasks: []
}

const mockBoard = {
  id: 'b1', name: 'Test Board', createdBy: mockUser,
  columns: [mockColumn],
  createdAt: new Date().toISOString(),
  updatedAt: new Date().toISOString()
}

const mockTask = {
  id: 't1', title: 'Test Task', boardId: 'b1', columnId: 'col1',
  createdBy: mockUser, priority: 'MEDIUM' as const, position: 0,
  labels: [], checklist: [], createdAt: new Date().toISOString(), updatedAt: new Date().toISOString()
}

describe('boards store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('fetchBoards loads boards', async () => {
    vi.mocked(boardsApi.boardsApi.getAll).mockResolvedValue([mockBoard])
    const store = useBoardStore()
    await store.fetchBoards()
    expect(store.boards).toHaveLength(1)
    expect(store.boards[0].name).toBe('Test Board')
  })

  it('fetchBoards handles errors', async () => {
    vi.mocked(boardsApi.boardsApi.getAll).mockRejectedValue({ response: { data: { detail: 'Server error' } } })
    const store = useBoardStore()
    await store.fetchBoards()
    expect(store.error).toBe('Server error')
    expect(store.boards).toHaveLength(0)
  })

  it('createBoard adds to list', async () => {
    vi.mocked(boardsApi.boardsApi.create).mockResolvedValue(mockBoard)
    const store = useBoardStore()
    await store.createBoard('Test Board')
    expect(store.boards).toHaveLength(1)
  })

  it('createTask adds to column', async () => {
    vi.mocked(boardsApi.boardsApi.getById).mockResolvedValue({ ...mockBoard, columns: [{ ...mockColumn }] })
    vi.mocked(tasksApi.tasksApi.create).mockResolvedValue(mockTask)
    const store = useBoardStore()
    await store.fetchBoard('b1')
    await store.createTask('b1', { title: 'Test Task', columnId: 'col1' })
    expect(store.currentBoard?.columns[0].tasks).toHaveLength(1)
  })

  it('deleteTask removes from column', async () => {
    vi.mocked(boardsApi.boardsApi.getById).mockResolvedValue({
      ...mockBoard,
      columns: [{ ...mockColumn, tasks: [mockTask] }]
    })
    vi.mocked(tasksApi.tasksApi.delete).mockResolvedValue(undefined as any)
    const store = useBoardStore()
    await store.fetchBoard('b1')
    await store.deleteTask('t1', 'col1')
    expect(store.currentBoard?.columns[0].tasks).toHaveLength(0)
  })

  it('deleteBoard removes from list', async () => {
    vi.mocked(boardsApi.boardsApi.getAll).mockResolvedValue([mockBoard])
    vi.mocked(boardsApi.boardsApi.delete).mockResolvedValue(undefined as any)
    const store = useBoardStore()
    await store.fetchBoards()
    await store.deleteBoard('b1')
    expect(store.boards).toHaveLength(0)
  })
})
