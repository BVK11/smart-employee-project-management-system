import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'
import { useNavigate } from 'react-router-dom'
import { authApi } from '@/api/auth'
import { registerUnauthorizedHandler } from '@/api/client'
import { session } from '@/utils/session'
import type { LoginRequest, RegisterRequest, Role } from '@/types/auth'

interface AuthContextValue {
  isAuthenticated: boolean
  role: Role | null
  userId: number | null
  employeeId: number | null
  name: string | null
  email: string | null
  department: string | null
  login: (payload: LoginRequest) => Promise<void>
  register: (payload: RegisterRequest) => Promise<void>
  logout: () => void
  setRole: (role: Role) => void
  setEmployeeId: (id: number) => void
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const navigate = useNavigate()
  const [token, setToken] = useState<string | null>(session.getToken())
  const [role, setRoleState] = useState<Role | null>(session.getRole())
  const [userId, setUserIdState] = useState<number | null>(session.getUserId())
  const [employeeId, setEmployeeIdState] = useState<number | null>(session.getEmployeeId())
  const [name, setName] = useState<string | null>(session.getName())
  const [email, setEmail] = useState<string | null>(session.getEmail())
  const [department, setDepartment] = useState<string | null>(session.getDepartment())

  useEffect(() => {
    registerUnauthorizedHandler(() => {
      session.clearAll()
      setToken(null)
      setRoleState(null)
      setUserIdState(null)
      setEmployeeIdState(null)
      setName(null)
      setEmail(null)
      setDepartment(null)
      navigate('/login', { replace: true })
    })
  }, [navigate])

  const login = async (payload: LoginRequest) => {
    const res = await authApi.login(payload)

    session.setToken(res.token)
    setToken(res.token)

    session.setRole(res.userType)
    setRoleState(res.userType)

    session.setUserId(res.userId)
    setUserIdState(res.userId)

    session.setName(res.name)
    setName(res.name)

    session.setEmail(res.email)
    setEmail(res.email)

    session.setEmployeeId(res.employeeId)
    setEmployeeIdState(res.employeeId)

    session.setDepartment(res.department)
    setDepartment(res.department)
  }

  const register = async (payload: RegisterRequest) => {
    await authApi.register(payload)
  }

  const logout = () => {
    session.clearAll()
    setToken(null)
    setRoleState(null)
    setUserIdState(null)
    setEmployeeIdState(null)
    setName(null)
    setEmail(null)
    setDepartment(null)
    navigate('/login', { replace: true })
  }

  const setRole = (r: Role) => {
    session.setRole(r)
    setRoleState(r)
  }

  const setEmployeeId = (id: number) => {
    session.setEmployeeId(id)
    setEmployeeIdState(id)
  }

  const value = useMemo(
    () => ({
      isAuthenticated: !!token,
      role,
      userId,
      employeeId,
      name,
      email,
      department,
      login,
      register,
      logout,
      setRole,
      setEmployeeId,
    }),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [token, role, userId, employeeId, name, email, department],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
