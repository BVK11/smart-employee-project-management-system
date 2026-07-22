import type { Role } from '@/types/auth'

const TOKEN_KEY = 'ems_token'
const ROLE_KEY = 'ems_role'
const EMPLOYEE_ID_KEY = 'ems_employee_id'
const NAME_KEY = 'ems_name'
const USER_ID_KEY = 'ems_user_id'
const DEPARTMENT_KEY = 'ems_department'
const EMAIL_KEY = 'ems_email'

export const session = {
  getToken: () => localStorage.getItem(TOKEN_KEY),
  setToken: (token: string) => localStorage.setItem(TOKEN_KEY, token),
  clearToken: () => localStorage.removeItem(TOKEN_KEY),

  getRole: (): Role | null => (localStorage.getItem(ROLE_KEY) as Role) || null,
  setRole: (role: Role) => localStorage.setItem(ROLE_KEY, role),

  getEmployeeId: (): number | null => {
    const v = localStorage.getItem(EMPLOYEE_ID_KEY)
    return v ? Number(v) : null
  },
  setEmployeeId: (id: number | null) => {
    if (id !== null && id !== undefined) {
      localStorage.setItem(EMPLOYEE_ID_KEY, String(id))
    } else {
      localStorage.removeItem(EMPLOYEE_ID_KEY)
    }
  },

  getUserId: (): number | null => {
    const v = localStorage.getItem(USER_ID_KEY)
    return v ? Number(v) : null
  },
  setUserId: (id: number | null) => {
    if (id !== null && id !== undefined) {
      localStorage.setItem(USER_ID_KEY, String(id))
    } else {
      localStorage.removeItem(USER_ID_KEY)
    }
  },

  getName: () => localStorage.getItem(NAME_KEY),
  setName: (name: string) => localStorage.setItem(NAME_KEY, name),

  getEmail: () => localStorage.getItem(EMAIL_KEY),
  setEmail: (email: string) => localStorage.setItem(EMAIL_KEY, email),

  getDepartment: () => localStorage.getItem(DEPARTMENT_KEY),
  setDepartment: (dept: string | null) => {
    if (dept) {
      localStorage.setItem(DEPARTMENT_KEY, dept)
    } else {
      localStorage.removeItem(DEPARTMENT_KEY)
    }
  },

  clearAll: () => {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(ROLE_KEY)
    localStorage.removeItem(EMPLOYEE_ID_KEY)
    localStorage.removeItem(NAME_KEY)
    localStorage.removeItem(USER_ID_KEY)
    localStorage.removeItem(DEPARTMENT_KEY)
    localStorage.removeItem(EMAIL_KEY)
  },
}
