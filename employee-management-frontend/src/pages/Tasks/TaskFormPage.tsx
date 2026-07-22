import { useEffect, useMemo, useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useNavigate, useParams } from 'react-router-dom'
import SectionHeader from '@/components/ui/SectionHeader'
import { SelectField, TextField, TextareaField } from '@/components/ui/FormField'
import { tasksApi } from '@/api/tasks'
import { projectsApi } from '@/api/projects'
import { employeesApi } from '@/api/employees'
import { extractErrorMessage, extractFieldErrors } from '@/api/client'
import { useToast } from '@/context/ToastContext'
import { toApiDate } from '@/utils/format'
import type { Project } from '@/types/project'
import type { Employee } from '@/types/employee'

const schema = z.object({
  title: z.string().min(1, 'Title is required'),
  description: z.string().optional().default(''),
  status: z.enum(['PENDING', 'IN_PROGRESS', 'COMPLETED']),
  progress: z.coerce.number().int().min(0).max(100),
  remarks: z.string().optional().default(''),
  deadline: z.string().min(1, 'Deadline is required'),
  projectId: z.coerce.number({ invalid_type_error: 'Project is required' }).min(1, 'Project is required'),
  employeeId: z.union([z.coerce.number(), z.literal('')]).optional(),
})
type FormValues = z.infer<typeof schema>

export default function TaskFormPage() {
  const { id } = useParams()
  const isEdit = !!id
  const navigate = useNavigate()
  const toast = useToast()
  const [loading, setLoading] = useState(isEdit)
  const [saving, setSaving] = useState(false)
  const [apiError, setApiError] = useState<string | null>(null)
  const [projects, setProjects] = useState<Project[]>([])
  const [employees, setEmployees] = useState<Employee[]>([])

  const {
    register,
    handleSubmit,
    reset,
    watch,
    setError,
    formState: { errors },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { status: 'PENDING', progress: 0 },
  })

  const selectedProjectId = watch('projectId')

  useEffect(() => {
    Promise.all([projectsApi.list(), employeesApi.list({ page: 0, size: 200 })]).then(([p, e]) => {
      setProjects(p)
      setEmployees(e.content)
    })
  }, [])

  useEffect(() => {
    if (!isEdit) return
    tasksApi
      .get(id!)
      .then((t) =>
        reset({
          title: t.title,
          description: t.description,
          status: t.status,
          progress: t.progress,
          remarks: t.remarks,
          deadline: toApiDate(t.deadline),
          projectId: t.projectId,
          employeeId: t.employeeId ?? '',
        }),
      )
      .catch((e) => toast.error(extractErrorMessage(e, 'Could not load task.')))
      .finally(() => setLoading(false))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id])

  const assignedEmployees = useMemo(() => {
    const proj = projects.find((p) => p.id === Number(selectedProjectId))
    if (!proj) return []
    return employees.filter((e) => proj.employeeIds?.includes(e.id))
  }, [projects, employees, selectedProjectId])

  const onSubmit = async (values: FormValues) => {
    setApiError(null)
    setSaving(true)
    const payload = {
      ...values,
      description: values.description ?? '',
      remarks: values.remarks ?? '',
      deadline: toApiDate(values.deadline),
      projectId: Number(values.projectId),
      employeeId: values.employeeId === '' || values.employeeId === undefined ? null : Number(values.employeeId),
    }
    try {
      if (isEdit) {
        await tasksApi.update(id!, payload)
        toast.success('Task updated.')
      } else {
        await tasksApi.create(payload)
        toast.success('Task created.')
      }
      navigate('/tasks')
    } catch (e) {
      const fieldErrors = extractFieldErrors(e)
      Object.entries(fieldErrors).forEach(([field, message]) => {
        if (field in values) setError(field as keyof FormValues, { message })
      })
      setApiError(extractErrorMessage(e, 'Could not save task.'))
    } finally {
      setSaving(false)
    }
  }

  if (loading) return <div className="panel">Loading…</div>

  return (
    <div>
      <SectionHeader eyebrow={isEdit ? '01 — Edit Record' : '01 — New Record'} title={isEdit ? 'Edit Task' : 'New Task'} />
      <form onSubmit={handleSubmit(onSubmit)} className="panel max-w-2xl" noValidate>
        <div className="flex flex-col gap-4">
          <TextField label="Title" required error={errors.title?.message} {...register('title')} />
          <TextareaField label="Description" error={errors.description?.message} {...register('description')} />
          <div className="grid sm:grid-cols-2 gap-4">
            <SelectField label="Project" required error={errors.projectId?.message} {...register('projectId')}>
              <option value="">Select project…</option>
              {projects.map((p) => (
                <option key={p.id} value={p.id}>{p.projectName}</option>
              ))}
            </SelectField>
            <SelectField
              label="Employee"
              hint="Only employees assigned to the selected project appear here"
              error={errors.employeeId?.message}
              {...register('employeeId')}
            >
              <option value="">Unassigned</option>
              {assignedEmployees.map((e) => (
                <option key={e.id} value={e.id}>{e.firstName} {e.lastName}</option>
              ))}
            </SelectField>
            <SelectField label="Status" required error={errors.status?.message} {...register('status')}>
              <option value="PENDING">Pending</option>
              <option value="IN_PROGRESS">In Progress</option>
              <option value="COMPLETED">Completed</option>
            </SelectField>
            <TextField label="Progress (%)" type="number" min={0} max={100} required error={errors.progress?.message} {...register('progress')} />
            <TextField label="Deadline" type="date" required error={errors.deadline?.message} {...register('deadline')} />
          </div>
          <TextareaField label="Remarks" error={errors.remarks?.message} {...register('remarks')} />
        </div>
        {apiError && (
          <div className="text-[12.5px] p-2.5 mt-4" style={{ background: 'var(--red-bg)', color: 'var(--red)' }}>
            {apiError}
          </div>
        )}
        <div className="flex gap-2 mt-6">
          <button className="btn" type="submit" disabled={saving}>
            {saving ? 'Saving…' : isEdit ? 'Save Changes' : 'Create Task'}
          </button>
          <button className="btn ghost" type="button" onClick={() => navigate(-1)}>
            Cancel
          </button>
        </div>
      </form>
    </div>
  )
}
