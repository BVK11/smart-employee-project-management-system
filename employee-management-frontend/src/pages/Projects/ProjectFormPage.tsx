import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useNavigate, useParams } from 'react-router-dom'
import SectionHeader from '@/components/ui/SectionHeader'
import { SelectField, TextField, TextareaField } from '@/components/ui/FormField'
import { projectsApi } from '@/api/projects'
import { extractErrorMessage, extractFieldErrors } from '@/api/client'
import { useToast } from '@/context/ToastContext'
import { toApiDate } from '@/utils/format'

const schema = z
  .object({
    projectName: z.string().min(1, 'Project name is required'),
    description: z.string().optional().default(''),
    priority: z.enum(['HIGH', 'MEDIUM', 'LOW']),
    status: z.enum(['ACTIVE', 'COMPLETED', 'ON_HOLD']),
    startDate: z.string().min(1, 'Start date is required'),
    endDate: z.string().min(1, 'End date is required'),
  })
  .refine((v) => new Date(v.endDate) >= new Date(v.startDate), {
    message: 'End date cannot be before start date',
    path: ['endDate'],
  })
type FormValues = z.infer<typeof schema>

export default function ProjectFormPage() {
  const { id } = useParams()
  const isEdit = !!id
  const navigate = useNavigate()
  const toast = useToast()
  const [loading, setLoading] = useState(isEdit)
  const [saving, setSaving] = useState(false)
  const [apiError, setApiError] = useState<string | null>(null)
  const [employeeIds, setEmployeeIds] = useState<number[]>([])

  const {
    register,
    handleSubmit,
    reset,
    setError,
    formState: { errors },
  } = useForm<FormValues>({ resolver: zodResolver(schema), defaultValues: { priority: 'MEDIUM', status: 'ACTIVE' } })

  useEffect(() => {
    if (!isEdit) return
    projectsApi
      .get(id!)
      .then((p) => {
        reset({
          projectName: p.projectName,
          description: p.description,
          priority: p.priority,
          status: p.status,
          startDate: toApiDate(p.startDate),
          endDate: toApiDate(p.endDate),
        })
        setEmployeeIds(p.employeeIds ?? [])
      })
      .catch((e) => toast.error(extractErrorMessage(e, 'Could not load project.')))
      .finally(() => setLoading(false))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id])

  const onSubmit = async (values: FormValues) => {
    setApiError(null)
    setSaving(true)
    const payload = {
      ...values,
      description: values.description ?? '',
      startDate: toApiDate(values.startDate),
      endDate: toApiDate(values.endDate),
      employeeIds,
    }
    try {
      if (isEdit) {
        await projectsApi.update(id!, payload)
        toast.success('Project updated.')
      } else {
        await projectsApi.create(payload)
        toast.success('Project created.')
      }
      navigate('/projects')
    } catch (e) {
      const fieldErrors = extractFieldErrors(e)
      Object.entries(fieldErrors).forEach(([field, message]) => {
        if (field in values) setError(field as keyof FormValues, { message })
      })
      setApiError(extractErrorMessage(e, 'Could not save project.'))
    } finally {
      setSaving(false)
    }
  }

  if (loading) return <div className="panel">Loading…</div>

  return (
    <div>
      <SectionHeader eyebrow={isEdit ? '01 — Edit Record' : '01 — New Record'} title={isEdit ? 'Edit Project' : 'New Project'} />
      <form onSubmit={handleSubmit(onSubmit)} className="panel max-w-2xl" noValidate>
        <div className="flex flex-col gap-4">
          <TextField label="Project Name" required error={errors.projectName?.message} {...register('projectName')} />
          <TextareaField label="Description" error={errors.description?.message} {...register('description')} />
          <div className="grid sm:grid-cols-2 gap-4">
            <SelectField label="Priority" required error={errors.priority?.message} {...register('priority')}>
              <option value="HIGH">High</option>
              <option value="MEDIUM">Medium</option>
              <option value="LOW">Low</option>
            </SelectField>
            <SelectField label="Status" required error={errors.status?.message} {...register('status')}>
              <option value="ACTIVE">Active</option>
              <option value="COMPLETED">Completed</option>
              <option value="ON_HOLD">On Hold</option>
            </SelectField>
            <TextField label="Start Date" type="date" required error={errors.startDate?.message} {...register('startDate')} />
            <TextField label="End Date" type="date" required error={errors.endDate?.message} {...register('endDate')} />
          </div>
          {isEdit && (
            <div className="text-[11.5px] text-inksoft border-t border-line pt-3">
              Manage team assignments from the project detail page after saving.
            </div>
          )}
        </div>
        {apiError && (
          <div className="text-[12.5px] p-2.5 mt-4" style={{ background: 'var(--red-bg)', color: 'var(--red)' }}>
            {apiError}
          </div>
        )}
        <div className="flex gap-2 mt-6">
          <button className="btn" type="submit" disabled={saving}>
            {saving ? 'Saving…' : isEdit ? 'Save Changes' : 'Create Project'}
          </button>
          <button className="btn ghost" type="button" onClick={() => navigate(-1)}>
            Cancel
          </button>
        </div>
      </form>
    </div>
  )
}
