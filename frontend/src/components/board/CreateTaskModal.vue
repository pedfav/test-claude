<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="modal">
      <div class="modal-header">
        <h2>Create Task</h2>
        <button class="btn btn-ghost btn-icon" @click="$emit('close')">✕</button>
      </div>

      <div v-if="error" class="error-message">{{ error }}</div>

      <form @submit.prevent="handleSubmit">
        <div class="form-group">
          <label>Title *</label>
          <input v-model="form.title" type="text" placeholder="Task title" required />
        </div>

        <div class="form-group">
          <label>Description</label>
          <textarea v-model="form.description" placeholder="Add a description..." rows="3"></textarea>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label>Column *</label>
            <select v-model="form.columnId" required>
              <option v-for="col in board.columns" :key="col.id" :value="col.id">
                {{ col.name }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label>Priority</label>
            <select v-model="form.priority">
              <option value="LOW">Low</option>
              <option value="MEDIUM">Medium</option>
              <option value="HIGH">High</option>
              <option value="URGENT">Urgent</option>
            </select>
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label>Assignee</label>
            <select v-model="form.assigneeId">
              <option value="">Unassigned</option>
              <option v-for="user in users" :key="user.id" :value="user.id">
                {{ user.displayName }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label>Due Date</label>
            <input v-model="form.dueDate" type="date" />
          </div>
        </div>

        <div class="form-group">
          <label>Labels (comma-separated)</label>
          <input v-model="labelsInput" type="text" placeholder="bug, feature, urgent" />
        </div>

        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" @click="$emit('close')">Cancel</button>
          <button type="submit" class="btn btn-primary" :disabled="creating">
            {{ creating ? 'Creating...' : 'Create Task' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useBoardStore } from '@/store/boards'
import { usersApi } from '@/api/users'
import type { Board, User } from '@/api/types'

const props = defineProps<{ board: Board }>()
const emit = defineEmits<{ close: []; created: [] }>()

const boardStore = useBoardStore()
const users = ref<User[]>([])
const creating = ref(false)
const error = ref('')
const labelsInput = ref('')

const form = reactive({
  title: '',
  description: '',
  columnId: props.board.columns[0]?.id || '',
  assigneeId: '',
  priority: 'MEDIUM' as 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT',
  dueDate: ''
})

onMounted(async () => {
  users.value = await usersApi.getAll()
})

async function handleSubmit() {
  creating.value = true
  error.value = ''
  try {
    const labels = labelsInput.value
      .split(',')
      .map((l) => l.trim())
      .filter(Boolean)

    await boardStore.createTask(props.board.id, {
      title: form.title,
      description: form.description || undefined,
      columnId: form.columnId,
      assigneeId: form.assigneeId || undefined,
      priority: form.priority,
      dueDate: form.dueDate || undefined,
      labels
    })
    emit('created')
  } catch (e: any) {
    error.value = e.response?.data?.detail || 'Failed to create task'
  } finally {
    creating.value = false
  }
}
</script>

<style scoped>
.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
</style>
