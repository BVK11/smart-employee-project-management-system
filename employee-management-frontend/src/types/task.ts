export type TaskStatus = 'PENDING' | 'IN_PROGRESS' | 'COMPLETED'
export type TaskPriority = 'HIGH' | 'MEDIUM' | 'LOW'

export interface Task {
  id: number
  title: string
  description: string
  status: TaskStatus
  progress: number
  priority?: TaskPriority
  remarks: string
  deadline: string
  assignedDate?: string
  dueDate?: string
  completedDate?: string | null
  estimatedHours?: number | null
  employeeId?: number | null
  employeeName?: string | null
  projectId: number
  projectName?: string | null
}

export type TaskInput = Omit<Task, 'id' | 'employeeName' | 'projectName'>

export interface TaskSearchParams {
  status?: string
  deadline?: string
}

export interface AssignTaskRequest {
  employeeId: number
  projectId: number
}
export interface UpdateProgressRequest { progress: number }
export interface UpdateStatusRequest { status: TaskStatus }
export interface UpdateRemarksRequest { remarks: string }
