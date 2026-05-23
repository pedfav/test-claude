import apiClient from './client'
import type { User } from './types'

export const usersApi = {
  getMe: () => apiClient.get<User>('/api/users/me').then((r) => r.data),
  getAll: () => apiClient.get<User[]>('/api/users').then((r) => r.data),
  updateProfile: (data: { displayName: string; avatarUrl?: string }) =>
    apiClient.put<User>('/api/users/me', data).then((r) => r.data)
}
