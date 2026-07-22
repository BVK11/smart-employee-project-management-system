import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { Plus, Search, Eye, Pencil, Trash2, LayoutGrid, List as ListIcon } from 'lucide-react'
import SectionHeader from '@/components/ui/SectionHeader'
import StatusBadge from '@/components/ui/StatusBadge'
import EmptyState from '@/components/ui/EmptyState'
import ConfirmDialog from '@/components/ui/ConfirmDialog'
import { SkeletonBlock, SkeletonRow } from '@/components/ui/Skeleton'
import { projectsApi } from '@/api/projects'
import { extractErrorMessage } from '@/api/client'
import { formatDate } from '@/utils/format'
import type { Project } from '@/types/project'
import { useAuth } from '@/context/AuthContext'
import { useToast } from '@/context/ToastContext'

export default function ProjectListPage() {
  const { role } = useAuth()
  const toast = useToast()
  const isAdmin = role === 'ADMIN'

  const [projects, setProjects] = useState<Project[]>([])
  const [loading, setLoading] = useState(true)
  const [keyword, setKeyword] = useState('')
  const [status, setStatus] = useState('')
  const [priority, setPriority] = useState('')
  const [view, setView] = useState<'grid' | 'table'>('grid')
  const [deleteTarget, setDeleteTarget] = useState<Project | null>(null)
  const [deleting, setDeleting] = useState(false)

  const load = () => {
    setLoading(true)
    const hasFilters = keyword || status || priority
    const call = hasFilters
      ? projectsApi.search({ keyword: keyword || undefined, status: status || undefined, priority: priority || undefined })
      : projectsApi.list()
    call
      .then(setProjects)
      .catch((e) => toast.error(extractErrorMessage(e, 'Could not load projects.')))
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

  const confirmDelete = async () => {
    if (!deleteTarget) return
    setDeleting(true)
    try {
      await projectsApi.remove(deleteTarget.id)
      toast.success(`${deleteTarget.projectName} was removed.`)
      setDeleteTarget(null)
      load()
    } catch (e) {
      toast.error(extractErrorMessage(e, 'Could not delete project.'))
    } finally {
      setDeleting(false)
    }
  }

  return (
    <div>
      <SectionHeader
        eyebrow="01 — Delivery Portfolio"
        title="Projects"
        action={
          <div className="flex gap-2">
            <div className="filters flex border border-line">
              <button className={`chip ${view === 'grid' ? 'active' : ''}`} onClick={() => setView('grid')} aria-label="Grid view">
                <LayoutGrid size={14} />
              </button>
              <button className={`chip ${view === 'table' ? 'active' : ''}`} onClick={() => setView('table')} aria-label="Table view">
                <ListIcon size={14} />
              </button>
            </div>
            {isAdmin && (
              <Link to="/projects/new" className="btn amber">
                <Plus size={14} /> New Project
              </Link>
            )}
          </div>
        }
      />

      <form onSubmit={onSearch} className="panel mb-5 flex flex-col sm:flex-row gap-3 sm:items-end flex-wrap">
        <div className="flex-1 min-w-[180px]">
          <label className="field-label">Search</label>
          <input className="field-input" placeholder="Project name…" value={keyword} onChange={(e) => setKeyword(e.target.value)} />
        </div>
        <div className="w-full sm:w-40">
          <label className="field-label">Status</label>
          <select className="field-input" value={status} onChange={(e) => setStatus(e.target.value)}>
            <option value="">All</option>
            <option value="ACTIVE">Active</option>
            <option value="COMPLETED">Completed</option>
            <option value="ON_HOLD">On Hold</option>
          </select>
        </div>
        <div className="w-full sm:w-40">
          <label className="field-label">Priority</label>
          <select className="field-input" value={priority} onChange={(e) => setPriority(e.target.value)}>
            <option value="">All</option>
            <option value="HIGH">High</option>
            <option value="MEDIUM">Medium</option>
            <option value="LOW">Low</option>
          </select>
        </div>
        <button className="btn" type="submit">
          <Search size={14} /> Search
        </button>
      </form>

      {loading ? (
        <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {Array.from({ length: 6 }).map((_, i) => (
            <SkeletonBlock key={i} className="h-40" />
          ))}
        </div>
      ) : projects.length === 0 ? (
        <div className="panel">
          <EmptyState title="No projects found" description="Try adjusting your search or filters." />
        </div>
      ) : view === 'grid' ? (
        <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {projects.map((p) => (
            <Link key={p.id} to={`/projects/${p.id}`} className="panel flex flex-col gap-3 hover:border-ink transition-colors">
              <div className="flex justify-between items-start gap-2">
                <h3 className="font-semibold text-[14.5px] m-0">{p.projectName}</h3>
                <StatusBadge value={p.priority} />
              </div>
              <p className="text-[12.5px] text-inksoft line-clamp-2 m-0">{p.description}</p>
              <div className="mt-1">
                <div className="flex justify-between items-center text-[11px] font-semibold text-inksoft mb-1">
                  <span>Progress</span>
                  <span className="mono">{p.progress ?? 0}%</span>
                </div>
                <div className="w-full bg-line h-1.5 rounded overflow-hidden">
                  <div
                    className="bg-green h-full rounded"
                    style={{ width: `${p.progress ?? 0}%` }}
                  />
                </div>
              </div>
              <div className="flex justify-between items-center mt-1">
                <StatusBadge value={p.status} />
                <span className="mono text-[11px] text-inksoft">{p.employeeIds?.length ?? 0} members</span>
              </div>
              <div className="text-[11px] mono text-inksoft border-t border-line pt-2 flex justify-between">
                <span>{formatDate(p.startDate)}</span>
                <span>{formatDate(p.endDate)}</span>
              </div>
            </Link>
          ))}
        </div>
      ) : (
        <div className="panel !p-0 overflow-x-auto">
          <table className="ledger">
            <thead>
              <tr>
                <th>Project</th><th>Priority</th><th>Status</th><th>Progress</th><th>Start</th><th>End</th><th>Members</th><th></th>
              </tr>
            </thead>
            <tbody>
              {loading
                ? Array.from({ length: 5 }).map((_, i) => <SkeletonRow key={i} cols={8} />)
                : projects.map((p) => (
                    <tr key={p.id}>
                      <td className="font-medium">{p.projectName}</td>
                      <td><StatusBadge value={p.priority} /></td>
                      <td><StatusBadge value={p.status} /></td>
                      <td className="mono">
                        <div className="flex items-center gap-2">
                          <div className="w-16 bg-line h-1.5 rounded overflow-hidden">
                            <div className="bg-green h-full rounded" style={{ width: `${p.progress ?? 0}%` }} />
                          </div>
                          <span>{p.progress ?? 0}%</span>
                        </div>
                      </td>
                      <td className="mono">{formatDate(p.startDate)}</td>
                      <td className="mono">{formatDate(p.endDate)}</td>
                      <td className="mono">{p.employeeIds?.length ?? 0}</td>
                      <td>
                        <div className="flex gap-1 justify-end">
                          <Link to={`/projects/${p.id}`} className="btn ghost !p-1.5" aria-label="View"><Eye size={14} /></Link>
                          {isAdmin && (
                            <>
                              <Link to={`/projects/${p.id}/edit`} className="btn ghost !p-1.5" aria-label="Edit"><Pencil size={14} /></Link>
                              <button className="btn ghost !p-1.5" aria-label="Delete" onClick={() => setDeleteTarget(p)}>
                                <Trash2 size={14} color="var(--red)" />
                              </button>
                            </>
                          )}
                        </div>
                      </td>
                    </tr>
                  ))}
            </tbody>
          </table>
        </div>
      )}

      <ConfirmDialog
        open={!!deleteTarget}
        title="Delete Project"
        description={`Remove "${deleteTarget?.projectName}"? This cannot be undone.`}
        confirmLabel="Delete"
        loading={deleting}
        onConfirm={confirmDelete}
        onCancel={() => setDeleteTarget(null)}
      />
    </div>
  )
}
