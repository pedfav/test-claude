export interface User {
  id: string
  email: string
  displayName: string
  avatarUrl?: string
  role: 'ADMIN' | 'MEMBER'
  createdAt: string
}

export interface AuthResponse {
  token: string
  user: User
}

export interface Team {
  id: string
  name: string
  description?: string
  createdBy: User
  members: TeamMember[]
  createdAt: string
}

export interface TeamMember {
  user: User
  role: 'OWNER' | 'ADMIN' | 'MEMBER'
  joinedAt: string
}

export interface Board {
  id: string
  name: string
  description?: string
  teamId?: string
  createdBy: User
  columns: Column[]
  createdAt: string
  updatedAt: string
}

export interface Column {
  id: string
  boardId: string
  name: string
  position: number
  color?: string
  tasks: Task[]
}

export interface Task {
  id: string
  title: string
  description?: string
  boardId: string
  columnId: string
  assignee?: User
  createdBy: User
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT'
  position: number
  dueDate?: string
  labels: string[]
  createdAt: string
  updatedAt: string
}

export interface CreateBoardRequest {
  name: string
  description?: string
  teamId?: string
}

export interface CreateTaskRequest {
  title: string
  description?: string
  columnId: string
  assigneeId?: string
  priority?: Task['priority']
  dueDate?: string
  labels?: string[]
}

export interface UpdateTaskRequest {
  title?: string
  description?: string
  columnId?: string
  assigneeId?: string
  priority?: Task['priority']
  position?: number
  dueDate?: string
  labels?: string[]
}

export interface MoveTaskRequest {
  columnId: string
  position: number
}
