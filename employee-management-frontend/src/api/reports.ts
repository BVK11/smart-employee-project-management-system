import { apiClient } from './client'
import type { EmployeeReportRow, PendingTaskReportRow, ProjectReportRow } from '@/types/report'

function parseFilename(disposition?: string, fallback = 'download') {
  if (!disposition) return fallback
  const match = /filename="?([^"]+)"?/.exec(disposition)
  return match?.[1] ?? fallback
}

async function downloadBlob(url: string, fallbackName: string) {
  const res = await apiClient.get(url, { responseType: 'blob' })
  const filename = parseFilename(res.headers['content-disposition'], fallbackName)
  const blobUrl = window.URL.createObjectURL(res.data)
  const a = document.createElement('a')
  a.href = blobUrl
  a.download = filename
  document.body.appendChild(a)
  a.click()
  a.remove()
  window.URL.revokeObjectURL(blobUrl)
}

export const reportsApi = {
  employeeReport: () => apiClient.get<EmployeeReportRow[]>('/reports/employee').then((r) => r.data),
  projectReport: () => apiClient.get<ProjectReportRow[]>('/reports/projects').then((r) => r.data),
  pendingTasksReport: () => apiClient.get<PendingTaskReportRow[]>('/reports/tasks/pending').then((r) => r.data),
  downloadPdf: () => downloadBlob('/reports/pdf', 'report.pdf'),
  downloadExcel: () => downloadBlob('/reports/excel', 'report.xlsx'),
}
