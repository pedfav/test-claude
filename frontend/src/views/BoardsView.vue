<template>
  <div class="layout">
    <AppHeader />
    <main class="main-content">
      <div class="page-header">
        <div>
          <h1>My Boards</h1>
          <p class="page-subtitle">Manage your projects and track progress</p>
        </div>
        <button class="btn btn-primary" @click="showCreate = true">+ New Board</button>
      </div>

      <div v-if="boardStore.loading" class="loading-state">
        <div class="spinner"></div>
      </div>

      <div v-else-if="boardStore.error" class="error-message">{{ boardStore.error }}</div>

      <div v-else-if="boardStore.boards.length === 0" class="empty-state">
        <div class="empty-icon">📋</div>
        <h3>No boards yet</h3>
        <p>Create your first board to get started managing your work</p>
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
            <div class="board-color-strip" :style="{ background: boardColor(board.id) }"></div>
            <div class="board-info">
              <h3>{{ board.name }}</h3>
              <p v-if="board.description" class="board-desc">{{ board.description }}</p>
            </div>
            <div v-if="boardOverdue(board) > 0" class="overdue-badge">
              ⚠ {{ boardOverdue(board) }}
            </div>
          </div>

          <div class="board-progress-row">
            <div class="progress-track">
              <div
                class="progress-fill"
                :style="{ width: `${boardProgress(board)}%` }"
                :class="{ complete: boardProgress(board) === 100 }"
              ></div>
            </div>
            <span class="progress-label">{{ boardDone(board) }}/{{ boardTotal(board) }}</span>
          </div>

          <div class="board-meta">
            <span class="meta-item">{{ board.columns.length }} columns</span>
            <span class="meta-dot">·</span>
            <span class="meta-item">{{ boardTotal(board) }} tasks</span>
            <span v-if="boardProgress(board) === 100 && boardTotal(board) > 0" class="meta-item done-tag">
              ✓ Complete
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
            <input v-model="newBoard.name" type="text" placeholder="My Project" required autofocus />
          </div>
          <div class="form-group">
            <label>Description</label>
            <textarea v-model="newBoard.description" placeholder="What is this board for?" rows="3"></textarea>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="showCreate = false">Cancel</button>
            <button type="submit" class="btn btn-primary" :disabled="creating">
              {{ creating ? 'Creating…' : 'Create Board' }}
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
import type { Board } from '@/api/types'

const boardStore = useBoardStore()
const showCreate = ref(false)
const creating = ref(false)
const newBoard = reactive({ name: '', description: '' })

const BOARD_COLORS = ['#6366f1', '#f59e0b', '#10b981', '#3b82f6', '#ec4899', '#8b5cf6', '#f97316', '#14b8a6']

function boardColor(id: string): string {
  const idx = id.charCodeAt(0) % BOARD_COLORS.length
  return BOARD_COLORS[idx]
}

function boardTotal(board: Board): number {
  return board.columns.reduce((s, c) => s + c.tasks.length, 0)
}

function boardDone(board: Board): number {
  if (board.columns.length === 0) return 0
  const lastCol = [...board.columns].sort((a, b) => b.position - a.position)[0]
  return lastCol?.tasks.length ?? 0
}

function boardProgress(board: Board): number {
  const total = boardTotal(board)
  if (total === 0) return 0
  return Math.round((boardDone(board) / total) * 100)
}

function boardOverdue(board: Board): number {
  if (board.columns.length === 0) return 0
  const lastCol = [...board.columns].sort((a, b) => b.position - a.position)[0]
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return board.columns
    .filter((c) => c.id !== lastCol?.id)
    .flatMap((c) => c.tasks)
    .filter((t) => t.dueDate && new Date(t.dueDate) < today).length
}

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
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 32px;
}

.page-header h1 {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 4px;
}

.page-subtitle {
  font-size: 14px;
  color: var(--color-text-muted);
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
  max-width: 320px;
  margin-left: auto;
  margin-right: auto;
}

.boards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
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
  transform: translateY(-2px);
}

.board-card-header {
  display: flex;
  gap: 12px;
  padding: 16px 16px 12px;
  align-items: flex-start;
}

.board-color-strip {
  width: 4px;
  min-height: 36px;
  border-radius: 2px;
  flex-shrink: 0;
}

.board-info {
  flex: 1;
  min-width: 0;
}

.board-info h3 {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.board-desc {
  font-size: 13px;
  color: var(--color-text-secondary);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
}

.overdue-badge {
  font-size: 11px;
  font-weight: 600;
  color: var(--color-danger);
  background: rgba(239, 68, 68, 0.1);
  padding: 2px 7px;
  border-radius: 999px;
  white-space: nowrap;
  flex-shrink: 0;
}

.board-progress-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 16px 10px;
}

.progress-track {
  flex: 1;
  height: 5px;
  background: var(--color-border);
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: var(--color-primary);
  border-radius: 3px;
  transition: width 0.4s ease;
}

.progress-fill.complete {
  background: var(--color-success);
}

.progress-label {
  font-size: 11px;
  font-weight: 600;
  color: var(--color-text-muted);
  white-space: nowrap;
}

.board-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px 12px;
  border-top: 1px solid var(--color-border);
  font-size: 12px;
  color: var(--color-text-muted);
}

.meta-dot {
  color: var(--color-border);
}

.done-tag {
  color: var(--color-success);
  font-weight: 600;
  margin-left: auto;
}
</style>
