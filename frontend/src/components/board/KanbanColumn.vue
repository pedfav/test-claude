<template>
  <div class="column">
    <div class="column-header">
      <div class="column-title-row">
        <div class="column-dot" :style="{ background: column.color || '#94a3b8' }"></div>
        <h3>{{ column.name }}</h3>
        <span class="task-count">{{ column.tasks.length }}</span>
      </div>
      <button class="btn btn-ghost btn-icon column-menu-btn" @click="showMenu = !showMenu">⋯</button>
      <div v-if="showMenu" class="column-menu card" v-click-outside="() => (showMenu = false)">
        <button class="menu-item danger" @click="handleDelete">Delete Column</button>
      </div>
    </div>

    <VueDraggable
      v-model="localTasks"
      group="tasks"
      class="task-list"
      ghost-class="task-ghost"
      drag-class="task-drag"
      :animation="150"
      @end="onDragEnd"
    >
      <TaskCard
        v-for="task in localTasks"
        :key="task.id"
        :task="task"
        @click="$emit('task-click', task)"
        @delete="$emit('task-deleted', task.id, column.id)"
      />
    </VueDraggable>

    <div class="column-footer">
      <button class="add-task-btn" @click="$emit('add-task', column.id)">+ Add task</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { VueDraggable } from 'vue-draggable-plus'
import TaskCard from './TaskCard.vue'
import type { Column, Task } from '@/api/types'

const props = defineProps<{
  column: Column
  boardId: string
}>()

const emit = defineEmits<{
  'task-moved': [taskId: string, targetColumnId: string, position: number, fromColumnId: string]
  'task-click': [task: Task]
  'task-deleted': [taskId: string, columnId: string]
  'column-deleted': [columnId: string]
  'add-task': [columnId: string]
}>()

const showMenu = ref(false)
const localTasks = ref<Task[]>([...props.column.tasks])

watch(
  () => props.column.tasks,
  (tasks) => { localTasks.value = [...tasks] },
  { deep: true }
)

function onDragEnd(event: any) {
  const taskId = localTasks.value[event.newIndex]?.id
  if (!taskId) return

  const fromColumnId = event.from?.dataset?.columnId || props.column.id
  const toColumnId = props.column.id

  emit('task-moved', taskId, toColumnId, event.newIndex, fromColumnId)
}

function handleDelete() {
  showMenu.value = false
  emit('column-deleted', props.column.id)
}

const vClickOutside = {
  mounted(el: HTMLElement, binding: any) {
    el._clickOutside = (event: Event) => {
      if (!el.contains(event.target as Node)) binding.value(event)
    }
    document.addEventListener('click', el._clickOutside)
  },
  unmounted(el: HTMLElement) {
    if (el._clickOutside) document.removeEventListener('click', el._clickOutside)
  }
}
</script>

<style scoped>
.column {
  min-width: 280px;
  max-width: 280px;
  display: flex;
  flex-direction: column;
  background: #f1f5f9;
  border-radius: var(--radius-lg);
  max-height: calc(100vh - 170px);
}

.column-header {
  display: flex;
  align-items: center;
  padding: 12px 12px 8px;
  position: relative;
}

.column-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 0;
}

.column-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}

.column-title-row h3 {
  font-size: 13px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--color-text-secondary);
  truncate: true;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.task-count {
  background: var(--color-border);
  color: var(--color-text-secondary);
  font-size: 11px;
  font-weight: 600;
  padding: 1px 6px;
  border-radius: 999px;
  flex-shrink: 0;
}

.column-menu-btn {
  color: var(--color-text-muted);
  flex-shrink: 0;
}

.column-menu {
  position: absolute;
  right: 8px;
  top: 100%;
  z-index: 10;
  padding: 4px;
  min-width: 140px;
}

.menu-item {
  display: block;
  width: 100%;
  padding: 8px 12px;
  text-align: left;
  font-size: 14px;
  border: none;
  background: none;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: background 0.1s;
}

.menu-item:hover { background: var(--color-border); }
.menu-item.danger { color: var(--color-danger); }
.menu-item.danger:hover { background: #fee2e2; }

.task-list {
  flex: 1;
  overflow-y: auto;
  padding: 0 8px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-height: 60px;
}

.column-footer {
  padding: 8px 8px 10px;
}

.add-task-btn {
  width: 100%;
  padding: 8px;
  border: none;
  background: transparent;
  color: var(--color-text-muted);
  font-size: 13px;
  border-radius: var(--radius-md);
  cursor: pointer;
  text-align: left;
  transition: background 0.1s, color 0.1s;
}

.add-task-btn:hover {
  background: var(--color-border);
  color: var(--color-text);
}

:deep(.task-ghost) {
  opacity: 0.4;
}

:deep(.task-drag) {
  box-shadow: var(--shadow-lg);
  transform: rotate(2deg);
}
</style>
