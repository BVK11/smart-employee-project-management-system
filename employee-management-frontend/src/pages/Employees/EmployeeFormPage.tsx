import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useNavigate, useParams } from 'react-router-dom'
import SectionHeader from '@/components/ui/SectionHeader'
import { SelectField, TextField } from '@/components/ui/FormField'
import { employeesApi } from '@/api/employees'
import { extractErrorMessage, extractFieldErrors } from '@/api/client'
import { useToast } from '@/context/ToastContext'
import { toApiDate } from '@/utils/format'

const schema = z.object({
  firstName: z.string().min(1, 'First name is required'),
  lastName: z.string().min(1, 'Last name is required'),
  email: z.string().min(1, 'Email is required').email('Enter a valid email address'),
  phone: z.string().min(1, 'Phone is required'),
  department: z.string().min(1, 'Department is required'),
  designation: z.string().min(1, 'Designation is required'),
  salary: z.coerce.number().positive('Salary must be greater than 0'),
  joiningDate: z.string().min(1, 'Joining date is required'),
  status: z.string().min(1, 'Status is required'),
})
type FormValues = z.infer<typeof schema>

export default function EmployeeFormPage() {
  const { id } = useParams()
  const isEdit = !!id
  const navigate = useNavigate()
  const toast = useToast()
  const [loading, setLoading] = useState(isEdit)
  const [saving, setSaving] = useState(false)
  const [apiError, setApiError] = useState<string | null>(null)

  const {
    register,
    handleSubmit,
    reset,
    setError,
    formState: { errors },
  } = useForm<FormValues>({ resolver: zodResolver(schema), defaultValues: { status: 'ACTIVE' } })

  useEffect(() => {
    if (!isEdit) return
    employeesApi
      .get(id!)
      .then((emp) =>
        reset({
          firstName: emp.firstName,
          lastName: emp.lastName,
          email: emp.email,
          phone: emp.phone,
          department: emp.department,
          designation: emp.designation,
          salary: emp.salary,
          joiningDate: toApiDate(emp.joiningDate),
          status: emp.status,
        }),
      )
      .catch((e) => toast.error(extractErrorMessage(e, 'Could not load employee.')))
      .finally(() => setLoading(false))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id])

  const onSubmit = async (values: FormValues) => {
    setApiError(null)
    setSaving(true)
    const payload = { ...values, joiningDate: toApiDate(values.joiningDate) }
    try {
      if (isEdit) {
        await employeesApi.update(id!, payload)
        toast.success('Employee updated.')
      } else {
        await employeesApi.create(payload)
        toast.success('Employee created.')
      }
      navigate('/employees')
    } catch (e) {
      const fieldErrors = extractFieldErrors(e)
      Object.entries(fieldErrors).forEach(([field, message]) => {
        if (field in values) setError(field as keyof FormValues, { message })
      })
      setApiError(extractErrorMessage(e, 'Could not save employee.'))
    } finally {
      setSaving(false)
    }
  }

  if (loading) return <div className="panel">Loading…</div>

  return (
    <div>
      <SectionHeader eyebrow={isEdit ? '01 — Edit Record' : '01 — New Record'} title={isEdit ? 'Edit Employee' : 'Add Employee'} />
      <form onSubmit={handleSubmit(onSubmit)} className="panel max-w-2xl" noValidate>
        <div className="grid sm:grid-cols-2 gap-4">
          <TextField label="First Name" required error={errors.firstName?.message} {...register('firstName')} />
          <TextField label="Last Name" required error={errors.lastName?.message} {...register('lastName')} />
          <TextField label="Email" type="email" required error={errors.email?.message} {...register('email')} />
          <TextField label="Phone" required error={errors.phone?.message} {...register('phone')} />
          <TextField label="Department" required error={errors.department?.message} {...register('department')} />
          <TextField label="Designation" required error={errors.designation?.message} {...register('designation')} />
          <TextField label="Salary" type="number" step="0.01" required error={errors.salary?.message} {...register('salary')} />
          <TextField label="Joining Date" type="date" required error={errors.joiningDate?.message} {...register('joiningDate')} />
          <SelectField label="Status" required error={errors.status?.message} {...register('status')}>
            <option value="ACTIVE">Active</option>
            <option value="INACTIVE">Inactive</option>
          </SelectField>
        </div>
        {apiError && (
          <div className="text-[12.5px] p-2.5 mt-4" style={{ background: 'var(--red-bg)', color: 'var(--red)' }}>
            {apiError}
          </div>
        )}
        <div className="flex gap-2 mt-6">
          <button className="btn" type="submit" disabled={saving}>
            {saving ? 'Saving…' : isEdit ? 'Save Changes' : 'Create Employee'}
          </button>
          <button className="btn ghost" type="button" onClick={() => navigate(-1)}>
            Cancel
          </button>
        </div>
      </form>
    </div>
  )
}
