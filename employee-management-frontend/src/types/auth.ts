export type Role = 'ADMIN' | 'EMPLOYEE'

export interface RegisterRequest {
  name: string
  email: string
  password: string
  role: Role
}

export interface LoginRequest {
  email: string
  password: string
  loginType: Role
}

export interface LoginResponse {
  token: string
  userId: number
  employeeId: number | null
  name: string
  email: string
  userType: Role
  department: string | null
  role: string | null
}

export interface ApiErrorResponse {
  timestamp?: string
  message: string
  details?: Record<string, string>
  httpStatus?: number
}
