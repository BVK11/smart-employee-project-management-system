export interface AdminDashboard {
  totalEmployees: number
  totalProjects: number
  completedTasks: number
  pendingTasks: number
}

export interface EmployeeDashboard {
  assignedTasks: number
  completedTasks: number
  upcomingDeadlines: number
}
