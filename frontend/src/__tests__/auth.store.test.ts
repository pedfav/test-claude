import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from '@/store/auth'
import * as authApi from '@/api/auth'

vi.mock('@/api/auth')

const mockUser = {
  id: '1',
  email: 'test@test.com',
  displayName: 'Test User',
  role: 'MEMBER' as const,
  createdAt: new Date().toISOString()
}

describe('auth store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
    vi.clearAllMocks()
  })

  it('initializes unauthenticated', () => {
    const store = useAuthStore()
    expect(store.isAuthenticated).toBe(false)
    expect(store.user).toBeNull()
  })

  it('login sets token and user', async () => {
    vi.mocked(authApi.authApi.login).mockResolvedValue({ token: 'tok123', user: mockUser })
    const store = useAuthStore()
    await store.login('test@test.com', 'password')
    expect(store.isAuthenticated).toBe(true)
    expect(store.user?.email).toBe('test@test.com')
    expect(localStorage.getItem('token')).toBe('tok123')
  })

  it('logout clears token and user', async () => {
    vi.mocked(authApi.authApi.login).mockResolvedValue({ token: 'tok123', user: mockUser })
    const store = useAuthStore()
    await store.login('test@test.com', 'password')
    store.logout()
    expect(store.isAuthenticated).toBe(false)
    expect(store.user).toBeNull()
    expect(localStorage.getItem('token')).toBeNull()
  })

  it('register sets token and user', async () => {
    vi.mocked(authApi.authApi.register).mockResolvedValue({ token: 'newtoken', user: mockUser })
    const store = useAuthStore()
    await store.register('new@test.com', 'password', 'New User')
    expect(store.isAuthenticated).toBe(true)
    expect(localStorage.getItem('token')).toBe('newtoken')
  })

  it('login failure does not authenticate', async () => {
    vi.mocked(authApi.authApi.login).mockRejectedValue(new Error('Bad credentials'))
    const store = useAuthStore()
    await expect(store.login('x@x.com', 'wrong')).rejects.toThrow()
    expect(store.isAuthenticated).toBe(false)
  })
})
