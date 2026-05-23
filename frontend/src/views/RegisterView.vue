<template>
  <div class="auth-page">
    <div class="auth-card card">
      <div class="auth-logo">
        <span class="logo-icon">📋</span>
        <h1>TaskFlow</h1>
      </div>
      <h2>Create your account</h2>

      <div v-if="error" class="error-message">{{ error }}</div>

      <form @submit.prevent="handleRegister">
        <div class="form-group">
          <label for="displayName">Full Name</label>
          <input
            id="displayName"
            v-model="form.displayName"
            type="text"
            placeholder="Jane Smith"
            required
          />
        </div>
        <div class="form-group">
          <label for="email">Email</label>
          <input
            id="email"
            v-model="form.email"
            type="email"
            placeholder="you@example.com"
            required
            autocomplete="email"
          />
        </div>
        <div class="form-group">
          <label for="password">Password</label>
          <input
            id="password"
            v-model="form.password"
            type="password"
            placeholder="Min. 8 characters"
            required
            minlength="8"
            autocomplete="new-password"
          />
        </div>
        <button type="submit" class="btn btn-primary full-width" :disabled="loading">
          <span v-if="loading" class="spinner"></span>
          <span>{{ loading ? 'Creating account...' : 'Create account' }}</span>
        </button>
      </form>

      <p class="auth-link">
        Already have an account?
        <router-link to="/login">Sign in</router-link>
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'

const router = useRouter()
const authStore = useAuthStore()

const form = reactive({ email: '', password: '', displayName: '' })
const loading = ref(false)
const error = ref('')

async function handleRegister() {
  loading.value = true
  error.value = ''
  try {
    await authStore.register(form.email, form.password, form.displayName)
    router.push('/boards')
  } catch (e: any) {
    const errors = e.response?.data?.errors
    if (errors) {
      error.value = Object.values(errors).join(', ')
    } else {
      error.value = e.response?.data?.detail || 'Registration failed'
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.auth-card {
  width: 100%;
  max-width: 400px;
  padding: 32px;
}

.auth-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.logo-icon { font-size: 28px; }

.auth-logo h1 {
  font-size: 24px;
  font-weight: 700;
  color: var(--color-primary);
}

h2 {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text-secondary);
  margin-bottom: 24px;
}

.full-width { width: 100%; padding: 10px; }

.auth-link {
  text-align: center;
  margin-top: 16px;
  font-size: 14px;
  color: var(--color-text-secondary);
}
</style>
