export type ProjectPriority = 'HIGH' | 'MEDIUM' | 'LOW'
export type ProjectStatus = 'ACTIVE' | 'COMPLETED' | 'ON_HOLD'

export interface Project {
  id: number
  projectName: string
  description: string
  priority: ProjectPriority
  status: ProjectStatus
  startDate: string
  endDate: string
  employeeIds: number[]
  progress?: number
}

export type ProjectInput = Omit<Project, 'id' | 'progress'>

export interface ProjectSearchParams {
  status?: string
  priority?: string
  keyword?: string
}

export interface AssignEmployeesRequest {
  employeeIds: number[]
}
