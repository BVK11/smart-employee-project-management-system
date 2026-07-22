import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { Pencil, Trash2, ArrowLeft } from 'lucide-react'
import SectionHeader from '@/components/ui/SectionHeader'
import StatusBadge from '@/components/ui/StatusBadge'
import ConfirmDialog from '@/components/ui/ConfirmDialog'
import { SkeletonBlock } from '@/components/ui/Skeleton'
import { employeesApi } from '@/api/employees'
import { extractErrorMessage } from '@/api/client'
import { formatCurrency, formatDate } from '@/utils/format'
import type { Employee } from '@/types/employee'
import { useAuth } from '@/context/AuthContext'
import { useToast } from '@/context/ToastContext'

function Field({ label, value }: { label: string; value: React.ReactNode }) {
  return (
    <div className="border-r border-b border-line p-4">
      <div className="ledger-label text-[11px] uppercase tracking-wide text-inksoft mb-1.5">{label}</div>
      <div className="text-[14px] font-medium">{value}</div>
    </div>
  )
}

export default function EmployeeDetailPage() {
  const { id } = useParams()
  const { role } = useAuth()
  const navigate = useNavigate()
  const toast = useToast()
  const [emp, setEmp] = useState<Employee | null>(null)
  const [loading, setLoading] = useState(true)
  const [confirmOpen, setConfirmOpen] = useState(false)
  const [deleting, setDeleting] = useState(false)

  useEffect(() => {
    employeesApi
      .get(id!)
      .then(setEmp)
      .catch((e) => toast.error(extractErrorMessage(e, 'Could not load employee.')))
      .finally(() => setLoading(false))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id])

  const onDelete = async () => {
    setDeleting(true)
    try {
      await employeesApi.remove(id!)
      toast.success('Employee deleted.')
      navigate('/employees')
    } catch (e) {
      toast.error(extractErrorMessage(e, 'Could not delete employee.'))
    } finally {
      setDeleting(false)
    }
  }

  if (loading) return <SkeletonBlock className="h-72" />
  if (!emp) return <div className="panel">Employee not found.</div>

  return (
    <div>
      <SectionHeader
        eyebrow="01 — Employee Record"
        title={`${emp.firstName} ${emp.lastName}`}
        action={
          <div className="flex gap-2">
            <Link to="/employees" className="btn ghost">
              <ArrowLeft size={14} /> Back
            </Link>
            {role === 'ADMIN' && (
              <>
                <Link to={`/employees/${emp.id}/edit`} className="btn">
                  <Pencil size={14} /> Edit
                </Link>
                <button className="btn danger" onClick={() => setConfirmOpen(true)}>
                  <Trash2 size={14} /> Delete
                </button>
              </>
            )}
          </div>
        }
      />
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 border-l border-t border-line">
        <Field label="Employee Code" value={<span className="mono">{emp.employeeCode ?? emp.id}</span>} />
        <Field label="Email" value={emp.email} />
        <Field label="Phone" value={emp.phone} />
        <Field label="Department" value={emp.department} />
        <Field label="Designation" value={emp.designation} />
        <Field label="Salary" value={<span className="mono">{formatCurrency(emp.salary)}</span>} />
        <Field label="Joining Date" value={<span className="mono">{formatDate(emp.joiningDate)}</span>} />
        <Field label="Status" value={<StatusBadge value={emp.status} />} />
      </div>

      <ConfirmDialog
        open={confirmOpen}
        title="Delete Employee"
        description={`Remove ${emp.firstName} ${emp.lastName}? This cannot be undone.`}
        confirmLabel="Delete"
        loading={deleting}
        onConfirm={onDelete}
        onCancel={() => setConfirmOpen(false)}
      />
    </div>
  )
}
