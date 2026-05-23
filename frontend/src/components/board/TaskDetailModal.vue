<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="modal modal-wide">
      <div class="modal-header">
        <div class="task-priority-indicator" :class="`priority-${task.priority.toLowerCase()}`"></div>
        <h2 v-if="!editing">{{ task.title }}</h2>
        <input v-else v-model="editForm.title" class="title-input" @blur="saveTitle" />
        <button class="btn btn-ghost btn-icon" @click="$emit('close')">✕</button>
      </div>

      <div class="task-detail-body">
        <div class="detail-main">
          <div class="detail-section">
            <label>Description</label>
            <textarea
              v-if="editingDesc"
              v-model="editForm.description"
              class="desc-textarea"
              rows="5"
              @blur="saveDesc"
              placeholder="Add a description..."
            ></textarea>
            <p
              v-else
              class="desc-text"
              @click="editingDesc = true"
            >{{ task.description || 'Click to add description...' }}</p>
          </div>
        </div>

        <div class="detail-sidebar">
          <div class="sidebar-field">
            <label>Status</label>
            <select :value="task.columnId" @change="updateColumn($event)">
              <option v-for="col in board.columns" :key="col.id" :value="col.id">{{ col.name }}</option>
            </select>
          </div>

          <div class="sidebar-field">
            <label>Priority</label>
            <select :value="task.priority" @change="updateField('priority', ($event.target as HTMLSelectElement).value)">
              <option value="LOW">Low</option>
              <option value="MEDIUM">Medium</option>
              <option value="HIGH">High</option>
              <option value="URGENT">Urgent</option>
            </select>
          </div>

          <div class="sidebar-field">
            <label>Assignee</label>
            <select :value="task.assignee?.id || ''" @change="updateAssignee($event)">
              <option value="">Unassigned</option>
              <option v-for="user in users" :key="user.id" :value="user.id">{{ user.displayName }}</option>
            </select>
          </div>

          <div class="sidebar-field">
            <label>Due Date</label>
            <input
              type="date"
              :value="task.dueDate"
              @change="updateField('dueDate', ($event.target as HTMLInputElement).value || undefined)"
            />
          </div>

          <div class="sidebar-field">
            <label>Created by</label>
            <div class="creator">
              <div class="avatar avatar-sm">{{ task.createdBy.displayName.charAt(0) }}</div>
              <span>{{ task.createdBy.displayName }}</span>
            </div>
          </div>

          <div class="sidebar-field">
            <label>Created</label>
            <span class="meta-text">{{ formatDate(task.createdAt) }}</span>
          </div>
        </div>
      </div>

      <div class="modal-footer">
        <button class="btn btn-danger btn-sm" @click="handleDelete">Delete Task</button>
        <div class="flex-gap">
          <button class="btn btn-ghost btn-sm" @click="editing = !editing">
            {{ editing ? 'Done editing' : 'Edit title' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useBoardStore } from '@/store/boards'
import { usersApi } from '@/api/users'
import type { Board, Task, User, UpdateTaskRequest } from '@/api/types'

const props = defineProps<{ task: Task; board: Board }>()
const emit = defineEmits<{
  close: []
  updated: [task: Task]
  deleted: [taskId: string, columnId: string]
}>()

const boardStore = useBoardStore()
const users = ref<User[]>([])
const editing = ref(false)
const editingDesc = ref(false)

const editForm = reactive({
  title: props.task.title,
  description: props.task.description || ''
})

onMounted(async () => {
  users.value = await usersApi.getAll()
})

async function saveTitle() {
  if (editForm.title && editForm.title !== props.task.title) {
    const updated = await boardStore.updateTask(props.task.id, { title: editForm.title })
    emit('updated', updated)
  }
  editing.value = false
}

async function saveDesc() {
  editingDesc.value = false
  if (editForm.description !== props.task.description) {
    const updated = await boardStore.updateTask(props.task.id, { description: editForm.description })
    emit('updated', updated)
  }
}

async function updateField(field: keyof UpdateTaskRequest, value: any) {
  const updated = await boardStore.updateTask(props.task.id, { [field]: value } as UpdateTaskRequest)
  emit('updated', updated)
}

async function updateColumn(event: Event) {
  const columnId = (event.target as HTMLSelectElement).value
  const updated = await boardStore.updateTask(props.task.id, { columnId })
  emit('updated', updated)
}

async function updateAssignee(event: Event) {
  const assigneeId = (event.target as HTMLSelectElement).value || undefined
  const updated = await boardStore.updateTask(props.task.id, { assigneeId })
  emit('updated', updated)
}

async function handleDelete() {
  if (confirm('Delete this task?')) {
    emit('deleted', props.task.id, props.task.columnId)
  }
}

function formatDate(dateStr: string) {
  return new Date(dateStr).toLocaleDateString('en-US', {
    year: 'numeric', month: 'short', day: 'numeric'
  })
}
</script>

<style scoped>
.modal-wide {
  max-width: 680px;
}

.task-priority-indicator {
  width: 4px;
  height: 24px;
  border-radius: 2px;
  flex-shrink: 0;
  margin-right: 4px;
}

.priority-low { background: #3b82f6; }
.priority-medium { background: #f59e0b; }
.priority-high { background: #f97316; }
.priority-urgent { background: #ef4444; }

.modal-header {
  gap: 10px;
}

.modal-header h2 {
  flex: 1;
  cursor: pointer;
}

.title-input {
  flex: 1;
  font-size: 18px;
  font-weight: 600;
  border: 1px solid var(--color-primary);
  border-radius: var(--radius-sm);
  padding: 2px 8px;
  outline: none;
}

.task-detail-body {
  display: grid;
  grid-template-columns: 1fr 220px;
  gap: 24px;
  margin: 16px 0;
}

.detail-section label {
  display: block;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--color-text-secondary);
  margin-bottom: 8px;
}

.desc-text {
  font-size: 14px;
  color: var(--color-text-secondary);
  cursor: pointer;
  padding: 8px;
  border-radius: var(--radius-sm);
  min-height: 60px;
  border: 1px solid transparent;
}

.desc-text:hover {
  border-color: var(--color-border);
  background: var(--color-bg);
}

.desc-textarea {
  width: 100%;
  padding: 8px;
  border: 1px solid var(--color-primary);
  border-radius: var(--radius-sm);
  font-size: 14px;
  font-family: inherit;
  outline: none;
  resize: vertical;
}

.detail-sidebar {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.sidebar-field label {
  display: block;
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--color-text-muted);
  margin-bottom: 4px;
}

.sidebar-field select,
.sidebar-field input {
  width: 100%;
  padding: 6px 10px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: 13px;
  background: var(--color-surface);
  color: var(--color-text);
  outline: none;
}

.creator {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}

.avatar-sm {
  width: 24px;
  height: 24px;
  font-size: 11px;
}

.meta-text {
  font-size: 13px;
  color: var(--color-text-secondary);
}

.modal-footer {
  justify-content: space-between;
}

.flex-gap {
  display: flex;
  gap: 8px;
}
</style>
