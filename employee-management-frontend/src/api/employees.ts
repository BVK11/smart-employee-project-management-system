import { apiClient } from './client'
import type { Employee, EmployeeInput, EmployeeSearchParams } from '@/types/employee'
import type { PageParams, PageResponse } from '@/types/common'

export const employeesApi = {
  list: (params: PageParams) =>
    apiClient.get<PageResponse<Employee>>('/employees', { params }).then((r) => r.data),

  search: (params: EmployeeSearchParams) =>
    apiClient.get<PageResponse<Employee>>('/employees/search', { params }).then((r) => r.data),

  get: (id: number | string) =>
    apiClient.get<Employee>(`/employees/${id}`).then((r) => r.data),

  create: (payload: EmployeeInput) =>
    apiClient.post<Employee>('/employees', payload).then((r) => r.data),

  update: (id: number | string, payload: EmployeeInput) =>
    apiClient.put<Employee>(`/employees/${id}`, payload).then((r) => r.data),

  remove: (id: number | string) => apiClient.delete(`/employees/${id}`),
}
