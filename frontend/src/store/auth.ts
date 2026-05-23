import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/auth'
import type { User } from '@/api/types'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('token'))
  const user = ref<User | null>(null)

  const isAuthenticated = computed(() => !!token.value)

  function setAuth(newToken: string, newUser: User) {
    token.value = newToken
    user.value = newUser
    localStorage.setItem('token', newToken)
  }

  function clearAuth() {
    token.value = null
    user.value = null
    localStorage.removeItem('token')
  }

  async function login(email: string, password: string) {
    const data = await authApi.login(email, password)
    setAuth(data.token, data.user)
    return data
  }

  async function register(email: string, password: string, displayName: string) {
    const data = await authApi.register(email, password, displayName)
    setAuth(data.token, data.user)
    return data
  }

  function logout() {
    clearAuth()
  }

  return { token, user, isAuthenticated, login, register, logout, setAuth }
})
