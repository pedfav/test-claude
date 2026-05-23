import apiClient from './client'
import type { Board, Column, CreateBoardRequest } from './types'

export const boardsApi = {
  getAll: () => apiClient.get<Board[]>('/api/boards').then((r) => r.data),

  getById: (id: string) => apiClient.get<Board>(`/api/boards/${id}`).then((r) => r.data),

  create: (data: CreateBoardRequest) =>
    apiClient.post<Board>('/api/boards', data).then((r) => r.data),

  update: (id: string, data: Partial<CreateBoardRequest>) =>
    apiClient.put<Board>(`/api/boards/${id}`, data).then((r) => r.data),

  delete: (id: string) => apiClient.delete(`/api/boards/${id}`),

  createColumn: (boardId: string, name: string, color?: string) =>
    apiClient.post<Column>(`/api/boards/${boardId}/columns`, { name, color }).then((r) => r.data),

  updateColumn: (columnId: string, data: { name?: string; color?: string; position?: number }) =>
    apiClient.put<Column>(`/api/boards/columns/${columnId}`, data).then((r) => r.data),

  deleteColumn: (columnId: string) => apiClient.delete(`/api/boards/columns/${columnId}`)
}
