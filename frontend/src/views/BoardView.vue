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
          <div v-if="boardStats" class="board-stats">
            <span class="stat-chip">
              <span class="stat-value">{{ boardStats.total }}</span> tasks
            </span>
            <span v-if="boardStats.done > 0" class="stat-chip done">
              <span class="stat-value">{{ boardStats.done }}</span> done
            </span>
            <span v-if="boardStats.overdue > 0" class="stat-chip overdue">
              ⚠ {{ boardStats.overdue }} overdue
            </span>
            <div v-if="boardStats.total > 0" class="stat-progress">
              <div class="stat-progress-track">
                <div class="stat-progress-fill" :style="{ width: `${boardStats.progress}%` }"></div>
              </div>
              <span class="stat-pct">{{ boardStats.progress }}%</span>
            </div>
          </div>
        </div>
        <div class="board-actions">
          <button class="btn btn-secondary btn-sm" @click="showAddColumn = true">+ Column</button>
          <button class="btn btn-primary btn-sm" @click="showAddTask = true">+ Task</button>
        </div>
      </div>

      <FilterBar
        :filters="filters"
        :users="boardUsers"
        @update:filters="filters = $event"
      />

      <div class="board-canvas">
        <div class="columns-container">
          <KanbanColumn
            v-for="column in boardStore.currentBoard.columns"
            :key="column.id"
            :column="column"
            :board-id="boardStore.currentBoard.id"
            :filter-fn="hasActiveFilters ? taskMatchesFilter : undefined"
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
              {{ addingColumn ? 'Adding…' : 'Add Column' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useBoardStore } from '@/store/boards'
import { usersApi } from '@/api/users'
import AppHeader from '@/components/common/AppHeader.vue'
import KanbanColumn from '@/components/board/KanbanColumn.vue'
import FilterBar from '@/components/board/FilterBar.vue'
import CreateTaskModal from '@/components/board/CreateTaskModal.vue'
import TaskDetailModal from '@/components/board/TaskDetailModal.vue'
import type { Task, TaskFilters, User } from '@/api/types'

const route = useRoute()
const boardStore = useBoardStore()

const showAddTask = ref(false)
const showAddColumn = ref(false)
const selectedTask = ref<Task | null>(null)
const newColumnName = ref('')
const addingColumn = ref(false)
const boardUsers = ref<User[]>([])

const filters = ref<TaskFilters>({ search: '', priority: '', assigneeId: '' })

const hasActiveFilters = computed(() =>
  filters.value.search !== '' || filters.value.priority !== '' || filters.value.assigneeId !== ''
)

const boardStats = computed(() => {
  const board = boardStore.currentBoard
  if (!board || board.columns.length === 0) return null
  const allTasks = board.columns.flatMap((c) => c.tasks)
  if (allTasks.length === 0) return null
  const sortedCols = [...board.columns].sort((a, b) => b.position - a.position)
  const lastCol = sortedCols[0]
  const done = lastCol?.tasks.length ?? 0
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const overdue = allTasks.filter(
    (t) => t.dueDate && new Date(t.dueDate) < today && t.columnId !== lastCol?.id
  ).length
  const total = allTasks.length
  const progress = total > 0 ? Math.round((done / total) * 100) : 0
  return { total, done, overdue, progress }
})

function taskMatchesFilter(task: Task): boolean {
  const f = filters.value
  if (f.search && !task.title.toLowerCase().includes(f.search.toLowerCase())) return false
  if (f.priority && task.priority !== f.priority) return false
  if (f.assigneeId === '__unassigned__' && task.assignee) return false
  if (f.assigneeId && f.assigneeId !== '__unassigned__' && task.assignee?.id !== f.assigneeId) return false
  return true
}

onMounted(async () => {
  await boardStore.fetchBoard(route.params.id as string)
  boardUsers.value = await usersApi.getAll()
})

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
  gap: 16px;
  flex-wrap: wrap;
}

.board-title-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.back-link {
  font-size: 13px;
  color: var(--color-text-secondary);
  text-decoration: none;
  flex-shrink: 0;
}

.back-link:hover {
  color: var(--color-primary);
}

.board-title-row h1 {
  font-size: 20px;
  font-weight: 700;
}

.board-stats {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.stat-chip {
  font-size: 12px;
  color: var(--color-text-secondary);
  background: var(--color-border);
  padding: 2px 8px;
  border-radius: 999px;
}

.stat-chip .stat-value {
  font-weight: 700;
  color: var(--color-text);
}

.stat-chip.done {
  background: rgba(16, 185, 129, 0.12);
  color: var(--color-success);
}

.stat-chip.done .stat-value {
  color: var(--color-success);
}

.stat-chip.overdue {
  background: rgba(239, 68, 68, 0.1);
  color: var(--color-danger);
  font-weight: 500;
}

.stat-progress {
  display: flex;
  align-items: center;
  gap: 6px;
}

.stat-progress-track {
  width: 60px;
  height: 5px;
  background: var(--color-border);
  border-radius: 3px;
  overflow: hidden;
}

.stat-progress-fill {
  height: 100%;
  background: var(--color-primary);
  border-radius: 3px;
  transition: width 0.4s ease;
}

.stat-pct {
  font-size: 12px;
  font-weight: 600;
  color: var(--color-text-secondary);
}

.board-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.board-canvas {
  flex: 1;
  overflow-x: auto;
  padding: 20px 24px;
  background: var(--color-bg);
}

.columns-container {
  display: flex;
  gap: 16px;
  align-items: flex-start;
  min-height: calc(100vh - 200px);
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
