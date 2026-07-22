import { useEffect, useMemo, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { Pencil, Trash2, ArrowLeft, AlertCircle } from 'lucide-react'
import SectionHeader from '@/components/ui/SectionHeader'
import StatusBadge from '@/components/ui/StatusBadge'
import ConfirmDialog from '@/components/ui/ConfirmDialog'
import { SkeletonBlock } from '@/components/ui/Skeleton'
import { tasksApi } from '@/api/tasks'
import { projectsApi } from '@/api/projects'
import { employeesApi } from '@/api/employees'
import { extractErrorMessage } from '@/api/client'
import { formatDate, isOverdue } from '@/utils/format'
import type { Task, TaskStatus } from '@/types/task'
import type { Project } from '@/types/project'
import type { Employee } from '@/types/employee'
import { useAuth } from '@/context/AuthContext'
import { useToast } from '@/context/ToastContext'

export default function TaskDetailPage() {
  const { id } = useParams()
  const { role, employeeId: authEmployeeId } = useAuth()
  const navigate = useNavigate()
  const toast = useToast()
  const isAdmin = role === 'ADMIN'

  const [task, setTask] = useState<Task | null>(null)
  const [projects, setProjects] = useState<Project[]>([])
  const [employees, setEmployees] = useState<Employee[]>([])
  const [loading, setLoading] = useState(true)
  const [confirmOpen, setConfirmOpen] = useState(false)
  const [deleting, setDeleting] = useState(false)

  const [progressDraft, setProgressDraft] = useState(0)
  const [remarksDraft, setRemarksDraft] = useState('')
  const [assignEmpId, setAssignEmpId] = useState<string>('')
  const [savingField, setSavingField] = useState<string | null>(null)

  const load = () => {
    setLoading(true)
    Promise.all([tasksApi.get(id!), projectsApi.list(), employeesApi.list({ page: 0, size: 200 })])
      .then(([t, p, e]) => {
        setTask(t)
        setProjects(p)
        setEmployees(e.content)
        setProgressDraft(t.progress)
        setRemarksDraft(t.remarks ?? '')
        setAssignEmpId(t.employeeId ? String(t.employeeId) : '')
      })
      .catch((e) => toast.error(extractErrorMessage(e, 'Could not load task.')))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id])

  const project = useMemo(() => projects.find((p) => p.id === task?.projectId), [projects, task])
  const assignedEmployees = useMemo(
    () => employees.filter((e) => project?.employeeIds?.includes(e.id)),
    [employees, project],
  )
  const employeeName = (empId?: number | null) => {
    if (!empId) return 'Unassigned'
    const e = employees.find((x) => x.id === empId)
    return e ? `${e.firstName} ${e.lastName}` : `#${empId}`
  }

  const onDelete = async () => {
    setDeleting(true)
    try {
      await tasksApi.remove(id!)
      toast.success('Task deleted.')
      navigate('/tasks')
    } catch (e) {
      toast.error(extractErrorMessage(e, 'Could not delete task.'))
    } finally {
      setDeleting(false)
    }
  }

  const saveProgress = async () => {
    setSavingField('progress')
    try {
      const updated = await tasksApi.updateProgress(id!, { progress: progressDraft })
      setTask(updated)
      toast.success('Progress updated.')
    } catch (e) {
      toast.error(extractErrorMessage(e, 'Could not update progress.'))
    } finally {
      setSavingField(null)
    }
  }

  const saveStatus = async (status: TaskStatus) => {
    setSavingField('status')
    try {
      const updated = await tasksApi.updateStatus(id!, { status })
      setTask(updated)
      setProgressDraft(updated.progress)
      toast.success('Status updated.')
    } catch (e) {
      toast.error(extractErrorMessage(e, 'Could not update status.'))
    } finally {
      setSavingField(null)
    }
  }

  const saveRemarks = async () => {
    setSavingField('remarks')
    try {
      const updated = await tasksApi.updateRemarks(id!, { remarks: remarksDraft })
      setTask(updated)
      toast.success('Remarks updated.')
    } catch (e) {
      toast.error(extractErrorMessage(e, 'Could not update remarks.'))
    } finally {
      setSavingField(null)
    }
  }

  const saveAssignment = async () => {
    if (!assignEmpId || !task) return
    setSavingField('assign')
    try {
      const updated = await tasksApi.assign(id!, { employeeId: Number(assignEmpId), projectId: task.projectId })
      setTask(updated)
      toast.success('Task assigned.')
    } catch (e) {
      toast.error(extractErrorMessage(e, 'Could not assign task. Employee may not be on this project.'))
    } finally {
      setSavingField(null)
    }
  }

  if (loading) return <SkeletonBlock className="h-72" />
  if (!task) return <div className="panel">Task not found.</div>

  const overdue = isOverdue(task.deadline, task.status)

  return (
    <div>
      <SectionHeader
        eyebrow="01 — Task Record"
        title={task.title}
        action={
          <div className="flex gap-2">
            <Link to="/tasks" className="btn ghost"><ArrowLeft size={14} /> Back</Link>
            {isAdmin && (
              <>
                <Link to={`/tasks/${task.id}/edit`} className="btn"><Pencil size={14} /> Edit</Link>
                <button className="btn danger" onClick={() => setConfirmOpen(true)}><Trash2 size={14} /> Delete</button>
              </>
            )}
          </div>
        }
      />

      <div className="grid lg:grid-cols-[1.4fr_1fr] gap-0 border border-line mb-6">
        <div className="p-5 border-b lg:border-b-0 lg:border-r border-line">
          <h3 className="text-[14px] font-semibold mt-0 mb-4">Overview</h3>
          <p className="text-[13px] text-inksoft mb-4">{task.description || 'No description provided.'}</p>
          <div className="grid grid-cols-2 gap-4 text-[13px]">
            <div><div className="field-label !mb-1">Project</div>{project?.projectName ?? `PRJ-${task.projectId}`}</div>
            <div><div className="field-label !mb-1">Employee</div>{employeeName(task.employeeId)}</div>
            <div><div className="field-label !mb-1">Status</div><StatusBadge value={task.status} /></div>
            <div><div className="field-label !mb-1">Priority</div><StatusBadge value={task.priority ?? 'MEDIUM'} /></div>
            <div><div className="field-label !mb-1">Assigned Date</div><span className="mono">{formatDate(task.assignedDate)}</span></div>
            <div><div className="field-label !mb-1">Due Date</div><span className="mono">{formatDate(task.dueDate || task.deadline)}</span></div>
            <div><div className="field-label !mb-1">Completed Date</div><span className="mono">{task.completedDate ? formatDate(task.completedDate) : 'Not completed'}</span></div>
            <div><div className="field-label !mb-1">Estimated Hours</div><span className="mono">{task.estimatedHours ? `${task.estimatedHours} hrs` : 'N/A'}</span></div>
          </div>
          <div className="mt-4">
            <div className="field-label !mb-1">Progress</div>
            <div className="h-2 bg-bg border border-line">
              <div className="h-full bg-ink" style={{ width: `${task.progress}%` }} />
            </div>
            <div className="text-[11.5px] mono text-inksoft mt-1">{task.progress}%</div>
          </div>
          {task.remarks && (
            <div className="mt-4">
              <div className="field-label !mb-1">Remarks</div>
              <p className="text-[13px] m-0">{task.remarks}</p>
            </div>
          )}
        </div>

        {(isAdmin || (authEmployeeId != null && task.employeeId === authEmployeeId)) && (
          <div className="p-5">
            <h3 className="text-[14px] font-semibold mt-0 mb-4">Quick Actions</h3>
            <div className="flex flex-col gap-5">
              <div>
                <label className="field-label">Update Progress</label>
                <div className="flex gap-2">
                  <input
                    type="number"
                    min={0}
                    max={100}
                    className="field-input"
                    value={progressDraft}
                    onChange={(e) => setProgressDraft(Number(e.target.value))}
                  />
                  <button className="btn ghost" onClick={saveProgress} disabled={savingField === 'progress'}>
                    Save
                  </button>
                </div>
              </div>
              <div>
                <label className="field-label">Update Status</label>
                <div className="flex flex-wrap gap-1.5">
                  {(['PENDING', 'IN_PROGRESS', 'COMPLETED'] as TaskStatus[]).map((s) => (
                    <button
                      key={s}
                      className={`chip border ${task.status === s ? 'active' : ''}`}
                      style={{ borderColor: 'var(--line)' }}
                      onClick={() => saveStatus(s)}
                      disabled={savingField === 'status'}
                    >
                      {s.replace('_', ' ')}
                    </button>
                  ))}
                </div>
              </div>
              {isAdmin && (
                <div>
                  <label className="field-label">Assign Employee</label>
                  <div className="flex gap-2">
                    <select className="field-input" value={assignEmpId} onChange={(e) => setAssignEmpId(e.target.value)}>
                      <option value="">Select…</option>
                      {assignedEmployees.map((e) => (
                        <option key={e.id} value={e.id}>{e.firstName} {e.lastName}</option>
                      ))}
                    </select>
                    <button className="btn ghost" onClick={saveAssignment} disabled={savingField === 'assign' || !assignEmpId}>
                      Assign
                    </button>
                  </div>
                  {assignedEmployees.length === 0 && (
                    <p className="text-[11px] text-inksoft mt-1.5">No employees are assigned to this project yet.</p>
                  )}
                </div>
              )}
              <div>
                <label className="field-label">Remarks</label>
                <textarea className="field-input" rows={3} value={remarksDraft} onChange={(e) => setRemarksDraft(e.target.value)} />
                <button className="btn ghost mt-2" onClick={saveRemarks} disabled={savingField === 'remarks'}>
                  Save Remarks
                </button>
              </div>
            </div>
          </div>
        )}
      </div>

      <ConfirmDialog
        open={confirmOpen}
        title="Delete Task"
        description={`Remove "${task.title}"? This cannot be undone.`}
        confirmLabel="Delete"
        loading={deleting}
        onConfirm={onDelete}
        onCancel={() => setConfirmOpen(false)}
      />
    </div>
  )
}
