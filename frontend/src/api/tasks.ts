import apiClient from './client'
import type { Task, CreateTaskRequest, UpdateTaskRequest, MoveTaskRequest } from './types'

export const tasksApi = {
  getForBoard: (boardId: string) =>
    apiClient.get<Task[]>(`/api/boards/${boardId}/tasks`).then((r) => r.data),

  getById: (id: string) => apiClient.get<Task>(`/api/tasks/${id}`).then((r) => r.data),

  create: (boardId: string, data: CreateTaskRequest) =>
    apiClient.post<Task>(`/api/boards/${boardId}/tasks`, data).then((r) => r.data),

  update: (id: string, data: UpdateTaskRequest) =>
    apiClient.put<Task>(`/api/tasks/${id}`, data).then((r) => r.data),

  move: (id: string, data: MoveTaskRequest) =>
    apiClient.patch<Task>(`/api/tasks/${id}/move`, data).then((r) => r.data),

  delete: (id: string) => apiClient.delete(`/api/tasks/${id}`)
}
