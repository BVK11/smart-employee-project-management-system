export interface EmployeeReportRow {
  employeeName: string
  assignedTasks: number
  completedTasks: number
  pendingTasks: number
}

export interface ProjectReportRow {
  projectName: string
  assignedEmployeesCount: number
  totalTasks: number
  completedTasks: number
  remainingTasks: number
  progressPercentage: number
}

export interface PendingTaskReportRow {
  employeeName: string
  projectName: string
  deadline: string
  priority: string
  status: string
}
