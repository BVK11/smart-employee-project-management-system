import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Link, useNavigate } from 'react-router-dom'
import AuthLayout from './AuthLayout'
import { SelectField, TextField } from '@/components/ui/FormField'
import { useAuth } from '@/context/AuthContext'
import { extractErrorMessage, extractFieldErrors } from '@/api/client'
import { useToast } from '@/context/ToastContext'

const schema = z.object({
  name: z.string().min(1, 'Name is required'),
  email: z.string().min(1, 'Email is required').email('Enter a valid email address'),
  password: z
    .string()
    .min(8, 'Password must be at least 8 characters')
    .regex(/[A-Z]/, 'Include at least one uppercase letter')
    .regex(/[0-9]/, 'Include at least one number'),
  role: z.enum(['ADMIN', 'EMPLOYEE']),
})
type FormValues = z.infer<typeof schema>

export default function RegisterPage() {
  const { register: doRegister } = useAuth()
  const navigate = useNavigate()
  const toast = useToast()
  const [apiError, setApiError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  const {
    register,
    handleSubmit,
    setError,
    formState: { errors },
  } = useForm<FormValues>({ resolver: zodResolver(schema), defaultValues: { role: 'EMPLOYEE' } })

  const onSubmit = async (values: FormValues) => {
    setApiError(null)
    setLoading(true)
    try {
      await doRegister(values)
      toast.success('Account created. Please sign in.')
      navigate('/login', { replace: true })
    } catch (e) {
      const fieldErrors = extractFieldErrors(e)
      Object.entries(fieldErrors).forEach(([field, message]) => {
        if (field in values) setError(field as keyof FormValues, { message })
      })
      setApiError(extractErrorMessage(e, 'Registration failed. Please try again.'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <AuthLayout eyebrow="01 — Create Account" title="Register">
      <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4" noValidate>
        <TextField label="Full Name" required error={errors.name?.message} {...register('name')} />
        <TextField label="Email" type="email" required error={errors.email?.message} {...register('email')} />
        <TextField
          label="Password"
          type="password"
          required
          hint="Min. 8 characters, one uppercase letter, one number"
          error={errors.password?.message}
          {...register('password')}
        />
        <SelectField label="Role" required error={errors.role?.message} {...register('role')}>
          <option value="EMPLOYEE">Employee</option>
          <option value="ADMIN">Admin</option>
        </SelectField>
        {apiError && (
          <div className="text-[12.5px] p-2.5" style={{ background: 'var(--red-bg)', color: 'var(--red)' }}>
            {apiError}
          </div>
        )}
        <button className="btn amber justify-center mt-1" type="submit" disabled={loading}>
          {loading ? 'Creating account…' : 'Create Account'}
        </button>
      </form>
      <div className="text-[12.5px] text-inksoft mt-5 text-center">
        Already have an account?{' '}
        <Link to="/login" className="text-ink font-semibold underline">
          Sign In
        </Link>
      </div>
    </AuthLayout>
  )
}
