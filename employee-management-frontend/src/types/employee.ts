export type EmployeeStatus = string

export interface Employee {
  id: number
  employeeCode?: string
  firstName: string
  lastName: string
  email: string
  phone: string
  department: string
  designation: string
  salary: number
  joiningDate: string
  status: EmployeeStatus
}

export type EmployeeInput = Omit<Employee, 'id' | 'employeeCode'>

export interface EmployeeSearchParams {
  department?: string
  status?: string
  keyword?: string
  page?: number
  size?: number
  sortBy?: string
  sortDir?: 'asc' | 'desc'
}
