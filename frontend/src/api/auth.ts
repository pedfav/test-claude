import apiClient from './client'
import type { AuthResponse } from './types'

export const authApi = {
  login: (email: string, password: string) =>
    apiClient.post<AuthResponse>('/api/auth/login', { email, password }).then((r) => r.data),

  register: (email: string, password: string, displayName: string) =>
    apiClient
      .post<AuthResponse>('/api/auth/register', { email, password, displayName })
      .then((r) => r.data)
}
