import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { Pencil, Trash2, ArrowLeft, Users, Check } from 'lucide-react'
import SectionHeader from '@/components/ui/SectionHeader'
import StatusBadge from '@/components/ui/StatusBadge'
import ConfirmDialog from '@/components/ui/ConfirmDialog'
import Modal from '@/components/ui/Modal'
import { SkeletonBlock } from '@/components/ui/Skeleton'
import { projectsApi } from '@/api/projects'
import { employeesApi } from '@/api/employees'
import { extractErrorMessage } from '@/api/client'
import { formatDate } from '@/utils/format'
import type { Project } from '@/types/project'
import type { Employee } from '@/types/employee'
import { useAuth } from '@/context/AuthContext'
import { useToast } from '@/context/ToastContext'
import ProjectChat from '@/components/chat/ProjectChat'

export default function ProjectDetailPage() {
  const { id } = useParams()
  const { role } = useAuth()
  const navigate = useNavigate()
  const toast = useToast()
  const [project, setProject] = useState<Project | null>(null)
  const [employees, setEmployees] = useState<Employee[]>([])
  const [loading, setLoading] = useState(true)
  const [confirmOpen, setConfirmOpen] = useState(false)
  const [deleting, setDeleting] = useState(false)
  const [assignOpen, setAssignOpen] = useState(false)
  const [selectedIds, setSelectedIds] = useState<number[]>([])
  const [savingAssign, setSavingAssign] = useState(false)

  const load = () => {
    setLoading(true)
    Promise.all([projectsApi.get(id!), employeesApi.list({ page: 0, size: 200 })])
      .then(([p, empPage]) => {
        setProject(p)
        setEmployees(empPage.content)
        setSelectedIds(p.employeeIds ?? [])
      })
      .catch((e) => toast.error(extractErrorMessage(e, 'Could not load project.')))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id])

  const onDelete = async () => {
    setDeleting(true)
    try {
      await projectsApi.remove(id!)
      toast.success('Project deleted.')
      navigate('/projects')
    } catch (e) {
      toast.error(extractErrorMessage(e, 'Could not delete project.'))
    } finally {
      setDeleting(false)
    }
  }

  const toggleId = (empId: number) => {
    setSelectedIds((prev) => (prev.includes(empId) ? prev.filter((x) => x !== empId) : [...prev, empId]))
  }

  const saveAssignments = async () => {
    setSavingAssign(true)
    try {
      const updated = await projectsApi.assignEmployees(id!, { employeeIds: selectedIds })
      setProject(updated)
      toast.success('Team assignments updated.')
      setAssignOpen(false)
    } catch (e) {
      toast.error(extractErrorMessage(e, 'Could not update assignments.'))
    } finally {
      setSavingAssign(false)
    }
  }

  const employeeName = (empId: number) => {
    const e = employees.find((x) => x.id === empId)
    return e ? `${e.firstName} ${e.lastName}` : `#${empId}`
  }

  if (loading) return <SkeletonBlock className="h-72" />
  if (!project) return <div className="panel">Project not found.</div>

  return (
    <div>
      <SectionHeader
        eyebrow="01 — Project Record"
        title={project.projectName}
        action={
          <div className="flex gap-2">
            <Link to="/projects" className="btn ghost"><ArrowLeft size={14} /> Back</Link>
            {role === 'ADMIN' && (
              <>
                <Link to={`/projects/${project.id}/edit`} className="btn"><Pencil size={14} /> Edit</Link>
                <button className="btn danger" onClick={() => setConfirmOpen(true)}><Trash2 size={14} /> Delete</button>
              </>
            )}
          </div>
        }
      />

      <div className="grid lg:grid-cols-[1.4fr_1fr] gap-0 border border-line mb-6">
        <div className="p-5 border-b lg:border-b-0 lg:border-r border-line">
          <div className="block-title flex justify-between items-baseline mb-4">
            <h3 className="text-[14px] font-semibold m-0">Overview</h3>
          </div>
          <p className="text-[13px] text-inksoft mb-4">{project.description || 'No description provided.'}</p>
          <div className="grid grid-cols-2 gap-4 text-[13px]">
            <div><div className="field-label !mb-1">Priority</div><StatusBadge value={project.priority} /></div>
            <div><div className="field-label !mb-1">Status</div><StatusBadge value={project.status} /></div>
            <div><div className="field-label !mb-1">Start Date</div><span className="mono">{formatDate(project.startDate)}</span></div>
            <div><div className="field-label !mb-1">End Date</div><span className="mono">{formatDate(project.endDate)}</span></div>
            <div className="col-span-2 mt-2">
              <div className="field-label !mb-1">Project Progress</div>
              <div className="flex items-center gap-3">
                <div className="flex-1 bg-line h-2.5 rounded overflow-hidden">
                  <div
                    className="bg-green h-full rounded transition-all duration-500 ease-out"
                    style={{ width: `${project.progress ?? 0}%` }}
                  />
                </div>
                <span className="text-[12px] font-bold mono">{project.progress ?? 0}%</span>
              </div>
            </div>
          </div>
        </div>
        <div className="p-5">
          <div className="flex justify-between items-baseline mb-4">
            <h3 className="text-[14px] font-semibold m-0 flex items-center gap-2"><Users size={15} /> Team ({project.employeeIds?.length ?? 0})</h3>
            {role === 'ADMIN' && (
              <button className="btn ghost !py-1.5 !px-2.5 !text-[11px]" onClick={() => setAssignOpen(true)}>
                Manage
              </button>
            )}
          </div>
          {(!project.employeeIds || project.employeeIds.length === 0) ? (
            <p className="text-[12.5px] text-inksoft">No employees assigned yet.</p>
          ) : (
            <div className="flex flex-wrap gap-1.5">
              {project.employeeIds.map((eid) => (
                <span key={eid} className="status-tag neutral">{employeeName(eid)}</span>
              ))}
            </div>
          )}
        </div>
      </div>

      <div className="eyebrow mt-6">02 — Team Collaboration & Chat</div>
      <div className="mb-8">
        <ProjectChat projectId={project.id} />
      </div>

      <ConfirmDialog
        open={confirmOpen}
        title="Delete Project"
        description={`Remove "${project.projectName}"? This cannot be undone.`}
        confirmLabel="Delete"
        loading={deleting}
        onConfirm={onDelete}
        onCancel={() => setConfirmOpen(false)}
      />

      <Modal open={assignOpen} title="Manage Team Assignment" onClose={() => setAssignOpen(false)} width={480}>
        <div className="max-h-80 overflow-y-auto border border-line divide-y divide-line">
          {employees.map((emp) => {
            const checked = selectedIds.includes(emp.id)
            return (
              <button
                key={emp.id}
                type="button"
                onClick={() => toggleId(emp.id)}
                className="w-full flex items-center justify-between px-3 py-2.5 text-left hover:bg-bg"
              >
                <span className="text-[13px]">
                  {emp.firstName} {emp.lastName} <span className="text-inksoft text-[11.5px]">— {emp.department}</span>
                </span>
                <span
                  className="w-4 h-4 border flex items-center justify-center shrink-0"
                  style={{ borderColor: checked ? 'var(--ink)' : 'var(--line)', background: checked ? 'var(--ink)' : 'transparent' }}
                >
                  {checked && <Check size={11} color="#fff" />}
                </span>
              </button>
            )
          })}
        </div>
        <div className="flex justify-end gap-2 mt-5">
          <button className="btn ghost" onClick={() => setAssignOpen(false)}>Cancel</button>
          <button className="btn" onClick={saveAssignments} disabled={savingAssign}>
            {savingAssign ? 'Saving…' : 'Save Assignments'}
          </button>
        </div>
      </Modal>
    </div>
  )
}
