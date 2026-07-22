import { apiClient } from './client'
import type {
  AssignTaskRequest,
  Task,
  TaskInput,
  TaskSearchParams,
  UpdateProgressRequest,
  UpdateRemarksRequest,
  UpdateStatusRequest,
} from '@/types/task'

export const tasksApi = {
  list: () => apiClient.get<Task[]>('/tasks').then((r) => r.data),

  listEmployeeTasks: () => apiClient.get<Task[]>('/employee/tasks').then((r) => r.data),

  search: (params: TaskSearchParams) =>
    apiClient.get<Task[]>('/tasks/search', { params }).then((r) => r.data),

  get: (id: number | string) => apiClient.get<Task>(`/tasks/${id}`).then((r) => r.data),

  create: (payload: TaskInput) => apiClient.post<Task>('/tasks', payload).then((r) => r.data),

  update: (id: number | string, payload: TaskInput) =>
    apiClient.put<Task>(`/tasks/${id}`, payload).then((r) => r.data),

  remove: (id: number | string) => apiClient.delete(`/tasks/${id}`),

  assign: (id: number | string, payload: AssignTaskRequest) =>
    apiClient.put<Task>(`/tasks/${id}/assign`, payload).then((r) => r.data),

  updateProgress: (id: number | string, payload: UpdateProgressRequest) =>
    apiClient.put<Task>(`/tasks/${id}/progress`, payload).then((r) => r.data),

  updateStatus: (id: number | string, payload: UpdateStatusRequest) =>
    apiClient.put<Task>(`/tasks/${id}/status`, payload).then((r) => r.data),

  updateRemarks: (id: number | string, payload: UpdateRemarksRequest) =>
    apiClient.put<Task>(`/tasks/${id}/remarks`, payload).then((r) => r.data),
}
