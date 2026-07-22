import { useEffect, useState } from 'react'
import { Download, FileSpreadsheet, FileText } from 'lucide-react'
import SectionHeader from '@/components/ui/SectionHeader'
import EmptyState from '@/components/ui/EmptyState'
import { SkeletonRow } from '@/components/ui/Skeleton'
import { reportsApi } from '@/api/reports'
import { extractErrorMessage } from '@/api/client'
import { formatDate } from '@/utils/format'
import type { EmployeeReportRow, PendingTaskReportRow, ProjectReportRow } from '@/types/report'
import { useToast } from '@/context/ToastContext'

type Tab = 'employee' | 'project' | 'pending'

export default function ReportsPage() {
  const toast = useToast()
  const [tab, setTab] = useState<Tab>('employee')
  const [loading, setLoading] = useState(true)
  const [employeeRows, setEmployeeRows] = useState<EmployeeReportRow[]>([])
  const [projectRows, setProjectRows] = useState<ProjectReportRow[]>([])
  const [pendingRows, setPendingRows] = useState<PendingTaskReportRow[]>([])
  const [downloading, setDownloading] = useState<'pdf' | 'excel' | null>(null)

  useEffect(() => {
    setLoading(true)
    Promise.all([reportsApi.employeeReport(), reportsApi.projectReport(), reportsApi.pendingTasksReport()])
      .then(([e, p, pend]) => {
        setEmployeeRows(e)
        setProjectRows(p)
        setPendingRows(pend)
      })
      .catch((err) => toast.error(extractErrorMessage(err, 'Could not load reports.')))
      .finally(() => setLoading(false))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const download = async (kind: 'pdf' | 'excel') => {
    setDownloading(kind)
    try {
      if (kind === 'pdf') await reportsApi.downloadPdf()
      else await reportsApi.downloadExcel()
      toast.success(`${kind.toUpperCase()} download started.`)
    } catch (e) {
      toast.error(extractErrorMessage(e, `Could not download ${kind.toUpperCase()} report.`))
    } finally {
      setDownloading(null)
    }
  }

  return (
    <div>
      <SectionHeader
        eyebrow="01 — Analytics"
        title="Reports"
        action={
          <div className="flex gap-2">
            <button className="btn ghost" onClick={() => download('pdf')} disabled={downloading === 'pdf'}>
              <FileText size={14} /> {downloading === 'pdf' ? 'Preparing…' : 'Download PDF'}
            </button>
            <button className="btn ghost" onClick={() => download('excel')} disabled={downloading === 'excel'}>
              <FileSpreadsheet size={14} /> {downloading === 'excel' ? 'Preparing…' : 'Download Excel'}
            </button>
          </div>
        }
      />

      <div className="filters flex border border-line mb-5 w-fit">
        <button className={`chip ${tab === 'employee' ? 'active' : ''}`} onClick={() => setTab('employee')}>Employee Report</button>
        <button className={`chip ${tab === 'project' ? 'active' : ''}`} onClick={() => setTab('project')}>Project Progress</button>
        <button className={`chip ${tab === 'pending' ? 'active' : ''}`} onClick={() => setTab('pending')}>Pending Tasks</button>
      </div>

      <div className="panel !p-0 overflow-x-auto">
        {tab === 'employee' && (
          <table className="ledger">
            <thead><tr><th>Employee</th><th>Assigned</th><th>Completed</th><th>Pending</th></tr></thead>
            <tbody>
              {loading ? (
                Array.from({ length: 4 }).map((_, i) => <SkeletonRow key={i} cols={4} />)
              ) : employeeRows.length === 0 ? (
                <tr><td colSpan={4}><EmptyState title="No data available" icon={Download} /></td></tr>
              ) : (
                employeeRows.map((r, i) => (
                  <tr key={i}>
                    <td className="font-medium">{r.employeeName}</td>
                    <td className="mono">{r.assignedTasks}</td>
                    <td className="mono">{r.completedTasks}</td>
                    <td className="mono">{r.pendingTasks}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        )}

        {tab === 'project' && (
          <table className="ledger">
            <thead><tr><th>Project</th><th>Team</th><th>Total Tasks</th><th>Completed</th><th>Remaining</th><th>Progress</th></tr></thead>
            <tbody>
              {loading ? (
                Array.from({ length: 4 }).map((_, i) => <SkeletonRow key={i} cols={6} />)
              ) : projectRows.length === 0 ? (
                <tr><td colSpan={6}><EmptyState title="No data available" icon={Download} /></td></tr>
              ) : (
                projectRows.map((r, i) => (
                  <tr key={i}>
                    <td className="font-medium">{r.projectName}</td>
                    <td className="mono">{r.assignedEmployeesCount}</td>
                    <td className="mono">{r.totalTasks}</td>
                    <td className="mono">{r.completedTasks}</td>
                    <td className="mono">{r.remainingTasks}</td>
                    <td>
                      <div className="flex items-center gap-2">
                        <div className="w-24 h-2 bg-bg border border-line">
                          <div className="h-full bg-ink" style={{ width: `${r.progressPercentage}%` }} />
                        </div>
                        <span className="mono text-[11.5px]">{r.progressPercentage.toFixed(0)}%</span>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        )}

        {tab === 'pending' && (
          <table className="ledger">
            <thead><tr><th>Employee</th><th>Project</th><th>Deadline</th><th>Priority</th><th>Status</th></tr></thead>
            <tbody>
              {loading ? (
                Array.from({ length: 4 }).map((_, i) => <SkeletonRow key={i} cols={5} />)
              ) : pendingRows.length === 0 ? (
                <tr><td colSpan={5}><EmptyState title="No pending tasks" icon={Download} /></td></tr>
              ) : (
                pendingRows.map((r, i) => (
                  <tr key={i}>
                    <td className="font-medium">{r.employeeName}</td>
                    <td>{r.projectName}</td>
                    <td className="mono">{formatDate(r.deadline)}</td>
                    <td>{r.priority}</td>
                    <td>{r.status}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        )}
      </div>
    </div>
  )
}
