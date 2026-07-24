import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { Plus, Search, Eye, Pencil, Trash2 } from 'lucide-react'
import SectionHeader from '@/components/ui/SectionHeader'
import StatusBadge from '@/components/ui/StatusBadge'
import EmptyState from '@/components/ui/EmptyState'
import Pagination from '@/components/ui/Pagination'
import ConfirmDialog from '@/components/ui/ConfirmDialog'
import { SkeletonRow } from '@/components/ui/Skeleton'
import { employeesApi } from '@/api/employees'
import { extractErrorMessage } from '@/api/client'
import { formatCurrency, formatDate } from '@/utils/format'
import type { Employee } from '@/types/employee'
import { useAuth } from '@/context/AuthContext'
import { useToast } from '@/context/ToastContext'

export default function EmployeeListPage() {
  const { role } = useAuth()
  const toast = useToast()
  const isAdmin = role === 'ADMIN'

  const [employees, setEmployees] = useState<Employee[]>([])
  const [loading, setLoading] = useState(true)
  const [pageNo, setPageNo] = useState(0)
  const [pageSize, setPageSize] = useState(10)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [keyword, setKeyword] = useState('')
  const [department, setDepartment] = useState('')
  const [status, setStatus] = useState('')
  const [deleteTarget, setDeleteTarget] = useState<Employee | null>(null)
  const [deleting, setDeleting] = useState(false)

  const load = (currentPage = pageNo, currentSize = pageSize) => {
    setLoading(true)
    const hasFilters = keyword || department || status
    const call = hasFilters
      ? employeesApi.search({ keyword: keyword || undefined, department: department || undefined, status: status || undefined, page: currentPage, size: currentSize, sortBy: 'id', sortDir: 'asc' })
      : employeesApi.list({ page: currentPage, size: currentSize, sortBy: 'id', sortDir: 'asc' })

    call
      .then((res) => {
        setEmployees(res.content)
        setTotalPages(res.totalPages)
        setTotalElements(res.totalElements ?? 0)
      })
      .catch((e) => toast.error(extractErrorMessage(e, 'Could not load employees.')))
      .finally(() => setLoading(false))
  }

  const handlePageSizeChange = (newSize: number) => {
    setPageSize(newSize)
    setPageNo(0)
    load(0, newSize)
  }

  useEffect(() => {
    load(pageNo, pageSize)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [pageNo])

  const onSearch = (e: React.FormEvent) => {
    e.preventDefault()
    setPageNo(0)
    load()
  }

  const confirmDelete = async () => {
    if (!deleteTarget) return
    setDeleting(true)
    try {
      await employeesApi.remove(deleteTarget.id)
      toast.success(`${deleteTarget.firstName} ${deleteTarget.lastName} was removed.`)
      setDeleteTarget(null)
      load()
    } catch (e) {
      toast.error(extractErrorMessage(e, 'Could not delete employee.'))
    } finally {
      setDeleting(false)
    }
  }

  return (
    <div>
      <SectionHeader
        eyebrow="01 — Workforce"
        title="Employees"
        action={
          isAdmin && (
            <Link to="/employees/new" className="btn amber">
              <Plus size={14} /> Add Employee
            </Link>
          )
        }
      />

      <form onSubmit={onSearch} className="panel mb-5 flex flex-col sm:flex-row gap-3 sm:items-end flex-wrap">
        <div className="flex-1 min-w-[180px]">
          <label className="field-label">Search</label>
          <input className="field-input" placeholder="Name, email…" value={keyword} onChange={(e) => setKeyword(e.target.value)} />
        </div>
        <div className="w-full sm:w-44">
          <label className="field-label">Department</label>
          <input className="field-input" placeholder="Engineering" value={department} onChange={(e) => setDepartment(e.target.value)} />
        </div>
        <div className="w-full sm:w-40">
          <label className="field-label">Status</label>
          <select className="field-input" value={status} onChange={(e) => setStatus(e.target.value)}>
            <option value="">All</option>
            <option value="ACTIVE">Active</option>
            <option value="INACTIVE">Inactive</option>
          </select>
        </div>
        <button className="btn" type="submit">
          <Search size={14} /> Search
        </button>
      </form>

      <div className="panel !p-0">
        <div className="overflow-x-auto">
          <table className="ledger">
            <thead>
              <tr>
                <th>Code</th>
                <th>Name</th>
                <th>Email</th>
                <th>Department</th>
                <th>Designation</th>
                <th>Salary</th>
                <th>Joined</th>
                <th>Status</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                Array.from({ length: 5 }).map((_, i) => <SkeletonRow key={i} cols={9} />)
              ) : employees.length === 0 ? (
                <tr>
                  <td colSpan={9}>
                    <EmptyState title="No employees found" description="Try adjusting your search or filters." />
                  </td>
                </tr>
              ) : (
                employees.map((emp) => (
                  <tr key={emp.id}>
                    <td className="mono row-id">{emp.employeeCode ?? emp.id}</td>
                    <td className="font-medium">{emp.firstName} {emp.lastName}</td>
                    <td className="text-inksoft">{emp.email}</td>
                    <td>{emp.department}</td>
                    <td>{emp.designation}</td>
                    <td className="mono">{formatCurrency(emp.salary)}</td>
                    <td className="mono">{formatDate(emp.joiningDate)}</td>
                    <td><StatusBadge value={emp.status} /></td>
                    <td>
                      <div className="flex gap-1 justify-end">
                        <Link to={`/employees/${emp.id}`} className="btn ghost !p-1.5" aria-label="View">
                          <Eye size={14} />
                        </Link>
                        {isAdmin && (
                          <>
                            <Link to={`/employees/${emp.id}/edit`} className="btn ghost !p-1.5" aria-label="Edit">
                              <Pencil size={14} />
                            </Link>
                            <button className="btn ghost !p-1.5" aria-label="Delete" onClick={() => setDeleteTarget(emp)}>
                              <Trash2 size={14} color="var(--red)" />
                            </button>
                          </>
                        )}
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
        <div className="px-4">
          <Pagination
            pageNo={pageNo}
            totalPages={totalPages}
            onChange={setPageNo}
            pageSize={pageSize}
            onPageSizeChange={handlePageSizeChange}
            totalElements={totalElements}
          />
        </div>
      </div>

      <ConfirmDialog
        open={!!deleteTarget}
        title="Delete Employee"
        description={`Remove ${deleteTarget?.firstName} ${deleteTarget?.lastName}? This cannot be undone.`}
        confirmLabel="Delete"
        loading={deleting}
        onConfirm={confirmDelete}
        onCancel={() => setDeleteTarget(null)}
      />
    </div>
  )
}
