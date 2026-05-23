<template>
  <div class="layout">
    <AppHeader />

    <div v-if="boardStore.loading" class="loading-full">
      <div class="spinner"></div>
    </div>

    <div v-else-if="boardStore.error" class="error-page">
      <div class="error-message">{{ boardStore.error }}</div>
      <router-link to="/boards" class="btn btn-secondary">Back to Boards</router-link>
    </div>

    <template v-else-if="boardStore.currentBoard">
      <div class="board-header">
        <div class="board-title-row">
          <router-link to="/boards" class="back-link">← Boards</router-link>
          <h1>{{ boardStore.currentBoard.name }}</h1>
        </div>
        <div class="board-actions">
          <button class="btn btn-secondary btn-sm" @click="showAddColumn = true">+ Column</button>
          <button class="btn btn-primary btn-sm" @click="showAddTask = true">+ Task</button>
        </div>
      </div>

      <div class="board-canvas">
        <div class="columns-container">
          <KanbanColumn
            v-for="column in boardStore.currentBoard.columns"
            :key="column.id"
            :column="column"
            :board-id="boardStore.currentBoard.id"
            @task-moved="handleTaskMoved"
            @task-click="openTask"
            @task-deleted="handleTaskDeleted"
            @column-deleted="handleColumnDeleted"
          />
          <div class="add-column-placeholder" @click="showAddColumn = true">
            <span>+ Add Column</span>
          </div>
        </div>
      </div>
    </template>

    <!-- Add Task Modal -->
    <CreateTaskModal
      v-if="showAddTask && boardStore.currentBoard"
      :board="boardStore.currentBoard"
      @close="showAddTask = false"
      @created="handleTaskCreated"
    />

    <!-- Task Detail Modal -->
    <TaskDetailModal
      v-if="selectedTask"
      :task="selectedTask"
      :board="boardStore.currentBoard!"
      @close="selectedTask = null"
      @updated="handleTaskUpdated"
      @deleted="handleTaskDeletedFromDetail"
    />

    <!-- Add Column Modal -->
    <div v-if="showAddColumn" class="modal-overlay" @click.self="showAddColumn = false">
      <div class="modal">
        <div class="modal-header">
          <h2>Add Column</h2>
          <button class="btn btn-ghost btn-icon" @click="showAddColumn = false">✕</button>
        </div>
        <form @submit.prevent="handleAddColumn">
          <div class="form-group">
            <label>Column Name *</label>
            <input v-model="newColumnName" type="text" placeholder="e.g. In Review" required />
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="showAddColumn = false">Cancel</button>
            <button type="submit" class="btn btn-primary" :disabled="addingColumn">
              {{ addingColumn ? 'Adding...' : 'Add Column' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useBoardStore } from '@/store/boards'
import AppHeader from '@/components/common/AppHeader.vue'
import KanbanColumn from '@/components/board/KanbanColumn.vue'
import CreateTaskModal from '@/components/board/CreateTaskModal.vue'
import TaskDetailModal from '@/components/board/TaskDetailModal.vue'
import type { Task } from '@/api/types'

const route = useRoute()
const boardStore = useBoardStore()

const showAddTask = ref(false)
const showAddColumn = ref(false)
const selectedTask = ref<Task | null>(null)
const newColumnName = ref('')
const addingColumn = ref(false)

onMounted(() => boardStore.fetchBoard(route.params.id as string))

function openTask(task: Task) {
  selectedTask.value = task
}

async function handleTaskMoved(taskId: string, targetColumnId: string, position: number, fromColumnId: string) {
  await boardStore.moveTask(taskId, { columnId: targetColumnId, position }, fromColumnId)
}

function handleTaskCreated() {
  showAddTask.value = false
}

function handleTaskUpdated(task: Task) {
  selectedTask.value = task
}

async function handleTaskDeleted(taskId: string, columnId: string) {
  await boardStore.deleteTask(taskId, columnId)
}

async function handleTaskDeletedFromDetail(taskId: string, columnId: string) {
  await boardStore.deleteTask(taskId, columnId)
  selectedTask.value = null
}

async function handleColumnDeleted(columnId: string) {
  await boardStore.deleteColumn(columnId)
}

async function handleAddColumn() {
  if (!newColumnName.value.trim()) return
  addingColumn.value = true
  try {
    await boardStore.createColumn(boardStore.currentBoard!.id, newColumnName.value.trim())
    showAddColumn.value = false
    newColumnName.value = ''
  } finally {
    addingColumn.value = false
  }
}
</script>

<style scoped>
.layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.loading-full {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.error-page {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
}

.board-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 24px;
  background: var(--color-surface);
  border-bottom: 1px solid var(--color-border);
}

.board-title-row {
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-link {
  font-size: 13px;
  color: var(--color-text-secondary);
  text-decoration: none;
}

.back-link:hover {
  color: var(--color-primary);
}

.board-title-row h1 {
  font-size: 20px;
  font-weight: 700;
}

.board-actions {
  display: flex;
  gap: 8px;
}

.board-canvas {
  flex: 1;
  overflow-x: auto;
  padding: 24px;
  background: var(--color-bg);
}

.columns-container {
  display: flex;
  gap: 16px;
  align-items: flex-start;
  min-height: calc(100vh - 160px);
}

.add-column-placeholder {
  min-width: 280px;
  padding: 14px 20px;
  border: 2px dashed var(--color-border);
  border-radius: var(--radius-lg);
  color: var(--color-text-muted);
  cursor: pointer;
  text-align: center;
  font-size: 14px;
  transition: border-color 0.15s, color 0.15s;
}

.add-column-placeholder:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}
</style>
