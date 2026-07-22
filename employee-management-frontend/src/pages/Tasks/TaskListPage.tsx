import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { Plus, Search, Eye, Pencil, Trash2, AlertCircle } from 'lucide-react'
import SectionHeader from '@/components/ui/SectionHeader'
import StatusBadge from '@/components/ui/StatusBadge'
import EmptyState from '@/components/ui/EmptyState'
import ConfirmDialog from '@/components/ui/ConfirmDialog'
import { SkeletonRow } from '@/components/ui/Skeleton'
import { tasksApi } from '@/api/tasks'
import { extractErrorMessage } from '@/api/client'
import { formatDate, isOverdue } from '@/utils/format'
import type { Task } from '@/types/task'
import { useAuth } from '@/context/AuthContext'
import { useToast } from '@/context/ToastContext'

export default function TaskListPage() {
  const { role } = useAuth()
  const toast = useToast()
  const isAdmin = role === 'ADMIN'

  const [tasks, setTasks] = useState<Task[]>([])
  const [loading, setLoading] = useState(true)
  const [status, setStatus] = useState('')
  const [deadline, setDeadline] = useState('')
  const [statusChip, setStatusChip] = useState('ALL')
  const [deleteTarget, setDeleteTarget] = useState<Task | null>(null)
  const [deleting, setDeleting] = useState(false)

  const load = () => {
    setLoading(true)
    const hasFilters = status || deadline
    const call = hasFilters ? tasksApi.search({ status: status || undefined, deadline: deadline || undefined }) : tasksApi.list()
    call
      .then(setTasks)
      .catch((e) => toast.error(extractErrorMessage(e, 'Could not load tasks.')))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const onSearch = (e: React.FormEvent) => {
    e.preventDefault()
    load()
  }

  const filtered = useMemo(() => {
    if (statusChip === 'ALL') return tasks
    return tasks.filter((t) => t.status === statusChip)
  }, [tasks, statusChip])

  const confirmDelete = async () => {
    if (!deleteTarget) return
    setDeleting(true)
    try {
      await tasksApi.remove(deleteTarget.id)
      toast.success(`"${deleteTarget.title}" was removed.`)
      setDeleteTarget(null)
      load()
    } catch (e) {
      toast.error(extractErrorMessage(e, 'Could not delete task.'))
    } finally {
      setDeleting(false)
    }
  }

  return (
    <div>
      <SectionHeader
        eyebrow="01 — Work Items"
        title="Tasks"
        action={
          isAdmin && (
            <Link to="/tasks/new" className="btn amber">
              <Plus size={14} /> New Task
            </Link>
          )
        }
      />

      <form onSubmit={onSearch} className="panel mb-5 flex flex-col sm:flex-row gap-3 sm:items-end flex-wrap">
        <div className="w-full sm:w-48">
          <label className="field-label">Status</label>
          <select className="field-input" value={status} onChange={(e) => setStatus(e.target.value)}>
            <option value="">All</option>
            <option value="PENDING">Pending</option>
            <option value="IN_PROGRESS">In Progress</option>
            <option value="COMPLETED">Completed</option>
          </select>
        </div>
        <div className="w-full sm:w-48">
          <label className="field-label">Deadline</label>
          <input type="date" className="field-input" value={deadline} onChange={(e) => setDeadline(e.target.value)} />
        </div>
        <button className="btn" type="submit">
          <Search size={14} /> Search
        </button>
      </form>

      <div className="panel !p-0">
        <div className="flex justify-between items-center p-4 pb-0 flex-wrap gap-3">
          <h3 className="text-[14px] font-semibold m-0">All Tasks</h3>
          <div className="filters flex border border-line">
            {['ALL', 'PENDING', 'IN_PROGRESS', 'COMPLETED'].map((s) => (
              <button key={s} className={`chip ${statusChip === s ? 'active' : ''}`} onClick={() => setStatusChip(s)}>
                {s.replace('_', ' ')}
              </button>
            ))}
          </div>
        </div>
        <div className="overflow-x-auto mt-4">
          <table className="ledger">
            <thead>
              <tr>
                <th>Task</th><th>Employee</th><th>Project</th><th>Deadline</th><th>Progress</th><th>Status</th><th></th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                Array.from({ length: 5 }).map((_, i) => <SkeletonRow key={i} cols={7} />)
              ) : filtered.length === 0 ? (
                <tr>
                  <td colSpan={7}>
                    <EmptyState title="No tasks found" description="Try adjusting your search or filters." />
                  </td>
                </tr>
              ) : (
                filtered.map((t) => {
                  const overdue = isOverdue(t.deadline, t.status)
                  return (
                    <tr key={t.id}>
                      <td className="font-medium max-w-[220px]">{t.title}</td>
                      <td>{t.employeeName ?? <span className="text-inksoft">Unassigned</span>}</td>
                      <td>{t.projectName ?? <span className="text-inksoft">N/A</span>}</td>
                      <td className="mono">
                        <span className="flex items-center gap-1" style={{ color: overdue ? 'var(--red)' : undefined }}>
                          {overdue && <AlertCircle size={12} />} {formatDate(t.deadline)}
                        </span>
                      </td>
                      <td className="mono">{t.progress}%</td>
                      <td><StatusBadge value={t.status} /></td>
                      <td>
                        <div className="flex gap-1 justify-end">
                          <Link to={`/tasks/${t.id}`} className="btn ghost !p-1.5" aria-label="View"><Eye size={14} /></Link>
                          {isAdmin && (
                            <>
                              <Link to={`/tasks/${t.id}/edit`} className="btn ghost !p-1.5" aria-label="Edit"><Pencil size={14} /></Link>
                              <button className="btn ghost !p-1.5" aria-label="Delete" onClick={() => setDeleteTarget(t)}>
                                <Trash2 size={14} color="var(--red)" />
                              </button>
                            </>
                          )}
                        </div>
                      </td>
                    </tr>
                  )
                })
              )}
            </tbody>
          </table>
        </div>
      </div>

      <ConfirmDialog
        open={!!deleteTarget}
        title="Delete Task"
        description={`Remove "${deleteTarget?.title}"? This cannot be undone.`}
        confirmLabel="Delete"
        loading={deleting}
        onConfirm={confirmDelete}
        onCancel={() => setDeleteTarget(null)}
      />
    </div>
  )
}
