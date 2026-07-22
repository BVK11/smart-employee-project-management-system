import { apiClient } from './client'
import type { AssignEmployeesRequest, Project, ProjectInput, ProjectSearchParams } from '@/types/project'

export const projectsApi = {
  list: () => apiClient.get<Project[]>('/projects').then((r) => r.data),

  listEmployeeProjects: () => apiClient.get<Project[]>('/employee/projects').then((r) => r.data),

  search: (params: ProjectSearchParams) =>
    apiClient.get<Project[]>('/projects/search', { params }).then((r) => r.data),

  get: (id: number | string) => apiClient.get<Project>(`/projects/${id}`).then((r) => r.data),

  create: (payload: ProjectInput) => apiClient.post<Project>('/projects', payload).then((r) => r.data),

  update: (id: number | string, payload: ProjectInput) =>
    apiClient.put<Project>(`/projects/${id}`, payload).then((r) => r.data),

  remove: (id: number | string) => apiClient.delete(`/projects/${id}`),

  assignEmployees: (id: number | string, payload: AssignEmployeesRequest) =>
    apiClient.put<Project>(`/projects/${id}/employees`, payload).then((r) => r.data),
}
