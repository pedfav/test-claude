<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="modal modal-wide">
      <div class="modal-header">
        <div class="task-priority-indicator" :class="`priority-${task.priority.toLowerCase()}`"></div>
        <h2 v-if="!editing" @click="editing = true" class="title-display">{{ task.title }}</h2>
        <input v-else v-model="editForm.title" class="title-input" @blur="saveTitle" @keydown.enter="saveTitle" />
        <button class="btn btn-ghost btn-icon" @click="$emit('close')">✕</button>
      </div>

      <div class="task-detail-body">
        <div class="detail-main">
          <!-- Description -->
          <div class="detail-section">
            <label class="section-label">Description</label>
            <textarea
              v-if="editingDesc"
              v-model="editForm.description"
              class="desc-textarea"
              rows="4"
              @blur="saveDesc"
              placeholder="Add a description…"
              autofocus
            ></textarea>
            <p
              v-else
              class="desc-text"
              @click="editingDesc = true"
            >{{ task.description || 'Click to add description…' }}</p>
          </div>

          <!-- Checklist -->
          <div class="detail-section checklist-section">
            <div class="checklist-header">
              <label class="section-label">Checklist</label>
              <span v-if="checklistTotal > 0" class="checklist-summary">
                {{ checklistDone }}/{{ checklistTotal }}
              </span>
            </div>

            <div v-if="checklistTotal > 0" class="checklist-progress-track">
              <div
                class="checklist-progress-fill"
                :style="{ width: `${checklistPercent}%` }"
                :class="{ complete: checklistDone === checklistTotal }"
              ></div>
            </div>

            <ul v-if="editForm.checklist.length > 0" class="checklist-list">
              <li v-for="item in editForm.checklist" :key="item.id" class="checklist-item">
                <input
                  type="checkbox"
                  :checked="item.done"
                  class="checklist-checkbox"
                  @change="toggleItem(item.id)"
                />
                <span class="checklist-text" :class="{ done: item.done }">{{ item.text }}</span>
                <button class="remove-item-btn" @click="removeItem(item.id)" title="Remove">✕</button>
              </li>
            </ul>

            <div class="add-checklist-row">
              <input
                v-model="newItemText"
                type="text"
                placeholder="Add a subtask…"
                class="add-checklist-input"
                @keydown.enter.prevent="addItem"
              />
              <button class="btn btn-secondary btn-sm" @click="addItem" :disabled="!newItemText.trim()">Add</button>
            </div>
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

          <div class="sidebar-divider"></div>

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

          <div class="sidebar-field">
            <label>Updated</label>
            <span class="meta-text">{{ formatDate(task.updatedAt) }}</span>
          </div>
        </div>
      </div>

      <div class="modal-footer">
        <button class="btn btn-danger btn-sm" @click="handleDelete">Delete Task</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useBoardStore } from '@/store/boards'
import { usersApi } from '@/api/users'
import type { Board, Task, User, UpdateTaskRequest, ChecklistItem } from '@/api/types'

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
const newItemText = ref('')

const editForm = reactive({
  title: props.task.title,
  description: props.task.description || '',
  checklist: (props.task.checklist ?? []).map((i) => ({ ...i })) as ChecklistItem[]
})

const checklistTotal = computed(() => editForm.checklist.length)
const checklistDone = computed(() => editForm.checklist.filter((i) => i.done).length)
const checklistPercent = computed(() =>
  checklistTotal.value === 0 ? 0 : Math.round((checklistDone.value / checklistTotal.value) * 100)
)

onMounted(async () => {
  users.value = await usersApi.getAll()
})

async function saveTitle() {
  editing.value = false
  if (editForm.title && editForm.title !== props.task.title) {
    const updated = await boardStore.updateTask(props.task.id, { title: editForm.title })
    emit('updated', updated)
  }
}

async function saveDesc() {
  editingDesc.value = false
  if (editForm.description !== (props.task.description || '')) {
    const updated = await boardStore.updateTask(props.task.id, { description: editForm.description })
    emit('updated', updated)
  }
}

async function saveChecklist() {
  const updated = await boardStore.updateTask(props.task.id, { checklist: editForm.checklist })
  emit('updated', updated)
}

function addItem() {
  const text = newItemText.value.trim()
  if (!text) return
  editForm.checklist.push({ id: crypto.randomUUID(), text, done: false })
  newItemText.value = ''
  saveChecklist()
}

function toggleItem(id: string) {
  const item = editForm.checklist.find((i) => i.id === id)
  if (item) {
    item.done = !item.done
    saveChecklist()
  }
}

function removeItem(id: string) {
  editForm.checklist = editForm.checklist.filter((i) => i.id !== id)
  saveChecklist()
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
  max-width: 720px;
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

.title-display {
  flex: 1;
  cursor: pointer;
  border-radius: var(--radius-sm);
  padding: 2px 4px;
  margin-left: -4px;
  transition: background 0.1s;
}

.title-display:hover {
  background: var(--color-border);
}

.title-input {
  flex: 1;
  font-size: 18px;
  font-weight: 600;
  border: 1px solid var(--color-primary);
  border-radius: var(--radius-sm);
  padding: 2px 8px;
  outline: none;
  background: var(--color-surface);
  color: var(--color-text);
}

.task-detail-body {
  display: grid;
  grid-template-columns: 1fr 220px;
  gap: 24px;
  margin: 16px 0;
}

.section-label {
  display: block;
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.07em;
  color: var(--color-text-muted);
  margin-bottom: 8px;
}

.detail-section {
  margin-bottom: 20px;
}

.desc-text {
  font-size: 14px;
  color: var(--color-text-secondary);
  cursor: pointer;
  padding: 8px;
  border-radius: var(--radius-sm);
  min-height: 56px;
  border: 1px solid transparent;
  line-height: 1.5;
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
  background: var(--color-surface);
  color: var(--color-text);
}

/* Checklist */
.checklist-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.checklist-header .section-label {
  margin-bottom: 0;
}

.checklist-summary {
  font-size: 12px;
  font-weight: 600;
  color: var(--color-text-secondary);
}

.checklist-progress-track {
  height: 4px;
  background: var(--color-border);
  border-radius: 2px;
  margin-bottom: 12px;
  overflow: hidden;
}

.checklist-progress-fill {
  height: 100%;
  background: var(--color-primary);
  border-radius: 2px;
  transition: width 0.3s ease;
}

.checklist-progress-fill.complete {
  background: var(--color-success);
}

.checklist-list {
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 2px;
  margin-bottom: 10px;
}

.checklist-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 5px 6px;
  border-radius: var(--radius-sm);
  transition: background 0.1s;
}

.checklist-item:hover {
  background: var(--color-bg);
}

.checklist-item:hover .remove-item-btn {
  opacity: 1;
}

.checklist-checkbox {
  width: 15px;
  height: 15px;
  flex-shrink: 0;
  cursor: pointer;
  accent-color: var(--color-primary);
}

.checklist-text {
  flex: 1;
  font-size: 14px;
  color: var(--color-text);
  line-height: 1.4;
}

.checklist-text.done {
  text-decoration: line-through;
  color: var(--color-text-muted);
}

.remove-item-btn {
  opacity: 0;
  background: none;
  border: none;
  font-size: 10px;
  color: var(--color-text-muted);
  cursor: pointer;
  padding: 2px 4px;
  border-radius: 3px;
  transition: opacity 0.1s, color 0.1s;
  flex-shrink: 0;
}

.remove-item-btn:hover {
  color: var(--color-danger);
}

.add-checklist-row {
  display: flex;
  gap: 6px;
  align-items: center;
}

.add-checklist-input {
  flex: 1;
  padding: 6px 10px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: 13px;
  outline: none;
  background: var(--color-surface);
  color: var(--color-text);
  transition: border-color 0.15s;
}

.add-checklist-input:focus {
  border-color: var(--color-primary);
}

/* Sidebar */
.detail-sidebar {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.sidebar-field label {
  display: block;
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.07em;
  color: var(--color-text-muted);
  margin-bottom: 4px;
}

.sidebar-field select,
.sidebar-field input[type="date"] {
  width: 100%;
  padding: 6px 10px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: 13px;
  background: var(--color-surface);
  color: var(--color-text);
  outline: none;
  transition: border-color 0.15s;
}

.sidebar-field select:focus,
.sidebar-field input[type="date"]:focus {
  border-color: var(--color-primary);
}

.sidebar-divider {
  height: 1px;
  background: var(--color-border);
  margin: 2px 0;
}

.creator {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--color-text-secondary);
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
</style>
