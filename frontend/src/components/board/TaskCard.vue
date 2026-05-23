<template>
  <div class="task-card card" @click="$emit('click')">
    <div class="task-priority-bar" :class="`priority-${task.priority.toLowerCase()}`"></div>
    <div class="task-body">
      <p class="task-title">{{ task.title }}</p>
      <p v-if="task.description" class="task-desc">{{ task.description }}</p>

      <div v-if="task.labels.length > 0" class="task-labels">
        <span v-for="label in task.labels" :key="label" class="label-chip">{{ label }}</span>
      </div>

      <div class="task-footer">
        <div class="task-meta">
          <span v-if="task.dueDate" class="due-date" :class="{ overdue: isOverdue }">
            📅 {{ formatDate(task.dueDate) }}
          </span>
          <span :class="`badge badge-${task.priority.toLowerCase()}`">{{ task.priority }}</span>
        </div>
        <div v-if="task.assignee" class="assignee avatar" :title="task.assignee.displayName">
          {{ task.assignee.displayName.charAt(0).toUpperCase() }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { Task } from '@/api/types'

const props = defineProps<{ task: Task }>()
defineEmits<{ click: []; delete: [] }>()

const isOverdue = computed(() => {
  if (!props.task.dueDate) return false
  return new Date(props.task.dueDate) < new Date()
})

function formatDate(dateStr: string) {
  return new Date(dateStr).toLocaleDateString('en-US', { month: 'short', day: 'numeric' })
}
</script>

<style scoped>
.task-card {
  padding: 0;
  overflow: hidden;
  cursor: grab;
  transition: box-shadow 0.15s;
  user-select: none;
}

.task-card:hover {
  box-shadow: var(--shadow-md);
}

.task-card:active {
  cursor: grabbing;
}

.task-priority-bar {
  height: 3px;
}

.priority-low { background: #3b82f6; }
.priority-medium { background: #f59e0b; }
.priority-high { background: #f97316; }
.priority-urgent { background: #ef4444; }

.task-body {
  padding: 10px 12px 12px;
}

.task-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text);
  margin-bottom: 4px;
  line-height: 1.4;
}

.task-desc {
  font-size: 12px;
  color: var(--color-text-secondary);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 8px;
}

.task-labels {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-bottom: 8px;
}

.label-chip {
  font-size: 11px;
  padding: 1px 6px;
  background: #e0e7ff;
  color: #4338ca;
  border-radius: 3px;
  font-weight: 500;
}

.task-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 8px;
}

.task-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.due-date {
  font-size: 11px;
  color: var(--color-text-muted);
}

.due-date.overdue {
  color: var(--color-danger);
  font-weight: 500;
}

.assignee {
  width: 24px;
  height: 24px;
  font-size: 11px;
}
</style>
