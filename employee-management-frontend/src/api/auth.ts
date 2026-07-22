import { authClient } from './client'
import type { LoginRequest, LoginResponse, RegisterRequest } from '@/types/auth'

export const authApi = {
  register: (payload: RegisterRequest) =>
    authClient.post<string>('/register', payload).then((r) => r.data),

  login: (payload: LoginRequest) =>
    authClient.post<LoginResponse>('/login', payload).then((r) => r.data),
}
