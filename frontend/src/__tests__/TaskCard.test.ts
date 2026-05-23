import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import TaskCard from '@/components/board/TaskCard.vue'
import type { Task } from '@/api/types'

const mockUser = {
  id: 'u1', email: 'test@test.com', displayName: 'Alice Test', role: 'MEMBER' as const,
  createdAt: new Date().toISOString()
}

const baseTask: Task = {
  id: 't1', title: 'Fix Login Bug', boardId: 'b1', columnId: 'col1',
  createdBy: mockUser, priority: 'HIGH', position: 0,
  labels: ['bug', 'urgent'], checklist: [],
  createdAt: new Date().toISOString(), updatedAt: new Date().toISOString()
}

describe('TaskCard', () => {
  it('renders task title', () => {
    const wrapper = mount(TaskCard, { props: { task: baseTask } })
    expect(wrapper.text()).toContain('Fix Login Bug')
  })

  it('renders priority badge', () => {
    const wrapper = mount(TaskCard, { props: { task: baseTask } })
    expect(wrapper.text()).toContain('HIGH')
  })

  it('renders labels', () => {
    const wrapper = mount(TaskCard, { props: { task: baseTask } })
    expect(wrapper.text()).toContain('bug')
    expect(wrapper.text()).toContain('urgent')
  })

  it('emits click on card click', async () => {
    const wrapper = mount(TaskCard, { props: { task: baseTask } })
    await wrapper.trigger('click')
    expect(wrapper.emitted('click')).toBeTruthy()
  })

  it('shows assignee initial when assigned', () => {
    const task = { ...baseTask, assignee: mockUser }
    const wrapper = mount(TaskCard, { props: { task } })
    expect(wrapper.text()).toContain('A')
  })

  it('shows description when provided', () => {
    const task = { ...baseTask, description: 'A detailed description' }
    const wrapper = mount(TaskCard, { props: { task } })
    expect(wrapper.text()).toContain('A detailed description')
  })

  it('shows due date when provided', () => {
    const task = { ...baseTask, dueDate: '2026-12-31' }
    const wrapper = mount(TaskCard, { props: { task } })
    expect(wrapper.text()).toContain('Dec')
  })

  it('applies correct priority class', () => {
    const wrapper = mount(TaskCard, { props: { task: baseTask } })
    expect(wrapper.find('.task-priority-bar').classes()).toContain('priority-high')
  })
})
