<template>
  <header class="app-header">
    <div class="header-inner">
      <router-link to="/boards" class="logo">
        <span>📋</span>
        <span class="logo-text">TaskFlow</span>
      </router-link>

      <nav class="header-nav">
        <router-link to="/boards" class="nav-link">Boards</router-link>
      </nav>

      <div class="header-right">
        <div class="user-menu" v-if="authStore.isAuthenticated">
          <template v-if="authStore.user">
            <div class="avatar" :title="authStore.user.displayName">
              {{ authStore.user.displayName.charAt(0).toUpperCase() }}
            </div>
            <span class="user-name">{{ authStore.user.displayName }}</span>
          </template>
          <button class="btn btn-ghost btn-sm" @click="handleLogout">Sign out</button>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'

const router = useRouter()
const authStore = useAuthStore()

function handleLogout() {
  authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.app-header {
  background: var(--color-surface);
  border-bottom: 1px solid var(--color-border);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-inner {
  max-width: 1440px;
  margin: 0 auto;
  padding: 0 24px;
  height: 56px;
  display: flex;
  align-items: center;
  gap: 24px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 700;
  color: var(--color-primary);
  text-decoration: none;
}

.logo-text {
  font-size: 18px;
}

.header-nav {
  display: flex;
  gap: 4px;
}

.nav-link {
  padding: 6px 12px;
  border-radius: var(--radius-md);
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-secondary);
  text-decoration: none;
  transition: background 0.15s, color 0.15s;
}

.nav-link:hover,
.nav-link.router-link-active {
  background: var(--color-border);
  color: var(--color-text);
}

.header-right {
  margin-left: auto;
}

.user-menu {
  display: flex;
  align-items: center;
  gap: 10px;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--color-primary);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  cursor: default;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text);
}
</style>
