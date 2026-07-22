import { Navigate, Route, Routes } from 'react-router-dom'
import AppLayout from '@/components/layout/AppLayout'
import ProtectedRoute from '@/routes/ProtectedRoute'
import AdminRoute from '@/routes/AdminRoute'

import LoginPage from '@/pages/Auth/LoginPage'
import RegisterPage from '@/pages/Auth/RegisterPage'
import Unauthorized from '@/pages/Unauthorized'
import NotFound from '@/pages/NotFound'

import AdminDashboardPage from '@/pages/Dashboard/AdminDashboardPage'
import EmployeeDashboardPage from '@/pages/Dashboard/EmployeeDashboardPage'

import EmployeeListPage from '@/pages/Employees/EmployeeListPage'
import EmployeeFormPage from '@/pages/Employees/EmployeeFormPage'
import EmployeeDetailPage from '@/pages/Employees/EmployeeDetailPage'

import ProjectListPage from '@/pages/Projects/ProjectListPage'
import ProjectFormPage from '@/pages/Projects/ProjectFormPage'
import ProjectDetailPage from '@/pages/Projects/ProjectDetailPage'

import TaskListPage from '@/pages/Tasks/TaskListPage'
import TaskFormPage from '@/pages/Tasks/TaskFormPage'
import TaskDetailPage from '@/pages/Tasks/TaskDetailPage'

import ReportsPage from '@/pages/Reports/ReportsPage'
import { useAuth } from '@/context/AuthContext'

function DashboardRouter() {
  const { role } = useAuth()
  return role === 'ADMIN' ? <AdminDashboardPage /> : <EmployeeDashboardPage />
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/unauthorized" element={<Unauthorized />} />

      <Route element={<ProtectedRoute />}>
        <Route element={<AppLayout />}>
          <Route path="/dashboard" element={<DashboardRouter />} />
          <Route path="/dashboard/employee" element={<EmployeeDashboardPage />} />

          <Route element={<AdminRoute />}>
            <Route path="/employees" element={<EmployeeListPage />} />
            <Route path="/employees/:id" element={<EmployeeDetailPage />} />
            <Route path="/employees/new" element={<EmployeeFormPage />} />
            <Route path="/employees/:id/edit" element={<EmployeeFormPage />} />
          </Route>

          <Route path="/projects" element={<ProjectListPage />} />
          <Route path="/projects/:id" element={<ProjectDetailPage />} />
          <Route element={<AdminRoute />}>
            <Route path="/projects/new" element={<ProjectFormPage />} />
            <Route path="/projects/:id/edit" element={<ProjectFormPage />} />
          </Route>

          <Route path="/tasks" element={<TaskListPage />} />
          <Route path="/tasks/:id" element={<TaskDetailPage />} />
          <Route element={<AdminRoute />}>
            <Route path="/tasks/new" element={<TaskFormPage />} />
            <Route path="/tasks/:id/edit" element={<TaskFormPage />} />
          </Route>

          <Route element={<AdminRoute />}>
            <Route path="/reports" element={<ReportsPage />} />
          </Route>
        </Route>
      </Route>

      <Route path="/" element={<Navigate to="/dashboard" replace />} />
      <Route path="*" element={<NotFound />} />
    </Routes>
  )
}
