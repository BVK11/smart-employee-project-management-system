import { apiClient } from './client'

export interface Notification {
  id: number
  title: string
  message: string
  timestamp: string
  isRead: boolean
  userId: number
  referenceType?: string
  referenceId?: number
}

export const notificationsApi = {
  list: () => apiClient.get<Notification[]>('/notifications').then((r) => r.data),
  unreadCount: () => apiClient.get<number>('/notifications/unread-count').then((r) => r.data),
  markRead: (id: number) => apiClient.put<void>(`/notifications/${id}/read`).then((r) => r.data),
  markAllRead: () => apiClient.put<void>('/notifications/read-all').then((r) => r.data),
}
