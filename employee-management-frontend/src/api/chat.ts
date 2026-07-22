import { apiClient } from './client'
import type { Employee } from '@/types/employee'

export interface ChatMessage {
  id: number
  projectId: number
  senderId: number
  senderName: string
  senderEmail: string
  content: string
  timestamp: string
}

export interface TeamMember {
  employeeId: number
  userId: number
  firstName: string
  lastName: string
  email: string
  department?: string
  designation?: string
  employeeCode?: string
}

export const chatApi = {
  getHistory: (projectId: number | string) =>
    apiClient.get<ChatMessage[]>(`/projects/${projectId}/chat`).then((r) => r.data),
  
  sendMessage: (projectId: number | string, content: string) =>
    apiClient.post<ChatMessage>(`/projects/${projectId}/chat`, { content }).then((r) => r.data),

  getTeam: (projectId: number | string) =>
    apiClient.get<TeamMember[]>(`/projects/${projectId}/team`).then((r) => r.data),
}
