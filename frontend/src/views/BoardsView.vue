<template>
  <div class="layout">
    <AppHeader />
    <main class="main-content">
      <div class="page-header">
        <h1>My Boards</h1>
        <button class="btn btn-primary" @click="showCreate = true">+ New Board</button>
      </div>

      <div v-if="boardStore.loading" class="loading-state">
        <div class="spinner"></div>
      </div>

      <div v-else-if="boardStore.error" class="error-message">{{ boardStore.error }}</div>

      <div v-else-if="boardStore.boards.length === 0" class="empty-state">
        <div class="empty-icon">📋</div>
        <h3>No boards yet</h3>
        <p>Create your first board to get started</p>
        <button class="btn btn-primary" @click="showCreate = true">Create Board</button>
      </div>

      <div v-else class="boards-grid">
        <div
          v-for="board in boardStore.boards"
          :key="board.id"
          class="board-card card"
          @click="$router.push(`/boards/${board.id}`)"
        >
          <div class="board-card-header">
            <div class="board-color-strip"></div>
            <div class="board-info">
              <h3>{{ board.name }}</h3>
              <p v-if="board.description" class="board-desc">{{ board.description }}</p>
            </div>
          </div>
          <div class="board-meta">
            <span class="column-count">{{ board.columns.length }} columns</span>
            <span class="task-count">
              {{ board.columns.reduce((sum, c) => sum + c.tasks.length, 0) }} tasks
            </span>
          </div>
        </div>
      </div>
    </main>

    <!-- Create Board Modal -->
    <div v-if="showCreate" class="modal-overlay" @click.self="showCreate = false">
      <div class="modal">
        <div class="modal-header">
          <h2>Create Board</h2>
          <button class="btn btn-ghost btn-icon" @click="showCreate = false">✕</button>
        </div>
        <form @submit.prevent="handleCreate">
          <div class="form-group">
            <label>Board Name *</label>
            <input v-model="newBoard.name" type="text" placeholder="My Project" required />
          </div>
          <div class="form-group">
            <label>Description</label>
            <textarea v-model="newBoard.description" placeholder="What is this board for?" rows="3"></textarea>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="showCreate = false">Cancel</button>
            <button type="submit" class="btn btn-primary" :disabled="creating">
              {{ creating ? 'Creating...' : 'Create Board' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useBoardStore } from '@/store/boards'
import AppHeader from '@/components/common/AppHeader.vue'

const boardStore = useBoardStore()
const showCreate = ref(false)
const creating = ref(false)
const newBoard = reactive({ name: '', description: '' })

onMounted(() => boardStore.fetchBoards())

async function handleCreate() {
  creating.value = true
  try {
    await boardStore.createBoard(newBoard.name, newBoard.description || undefined)
    showCreate.value = false
    newBoard.name = ''
    newBoard.description = ''
  } finally {
    creating.value = false
  }
}
</script>

<style scoped>
.layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px 24px;
  width: 100%;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 32px;
}

.page-header h1 {
  font-size: 28px;
  font-weight: 700;
}

.loading-state {
  display: flex;
  justify-content: center;
  padding: 60px;
}

.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: var(--color-text-secondary);
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-state h3 {
  font-size: 20px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 8px;
}

.empty-state p {
  margin-bottom: 24px;
}

.boards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.board-card {
  padding: 0;
  overflow: hidden;
  cursor: pointer;
  transition: box-shadow 0.15s, transform 0.15s;
}

.board-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-1px);
}

.board-card-header {
  display: flex;
  gap: 12px;
  padding: 16px;
}

.board-color-strip {
  width: 4px;
  border-radius: 2px;
  background: var(--color-primary);
  flex-shrink: 0;
}

.board-info h3 {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 4px;
}

.board-desc {
  font-size: 13px;
  color: var(--color-text-secondary);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.board-meta {
  display: flex;
  gap: 12px;
  padding: 10px 16px;
  border-top: 1px solid var(--color-border);
  font-size: 12px;
  color: var(--color-text-muted);
}
</style>
