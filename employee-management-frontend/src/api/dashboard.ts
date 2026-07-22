import { apiClient } from './client'
import type { AdminDashboard, EmployeeDashboard } from '@/types/dashboard'

export const dashboardApi = {
  admin: () => apiClient.get<AdminDashboard>('/dashboard/admin').then((r) => r.data),
  employee: (employeeId: number | string) =>
    apiClient.get<EmployeeDashboard>(`/dashboard/employee/${employeeId}`).then((r) => r.data),
}
