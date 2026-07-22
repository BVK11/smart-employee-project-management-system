import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import AuthLayout from './AuthLayout'
import { TextField, SelectField } from '@/components/ui/FormField'
import { useAuth } from '@/context/AuthContext'
import { extractErrorMessage } from '@/api/client'
import { useToast } from '@/context/ToastContext'

const schema = z.object({
  loginType: z.enum(['ADMIN', 'EMPLOYEE']),
  email: z.string().min(1, 'Email is required').email('Enter a valid email address'),
  password: z.string().min(1, 'Password is required'),
})
type FormValues = z.infer<typeof schema>

export default function LoginPage() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const toast = useToast()
  const [apiError, setApiError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      loginType: 'EMPLOYEE',
      email: '',
      password: '',
    },
  })

  const onSubmit = async (values: FormValues) => {
    setApiError(null)
    setLoading(true)
    try {
      await login(values)
      toast.success('Signed in successfully.')
      const from = (location.state as { from?: Location })?.from?.pathname
      navigate(from || '/dashboard', { replace: true })
    } catch (e) {
      setApiError(extractErrorMessage(e, 'Invalid email or password.'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <AuthLayout eyebrow="01 — Sign In" title="Welcome back">
      <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4" noValidate>
        <SelectField
          label="Login As"
          required
          error={errors.loginType?.message}
          {...register('loginType')}
        >
          <option value="EMPLOYEE">Employee</option>
          <option value="ADMIN">Admin</option>
        </SelectField>
        <TextField
          label="Email"
          type="email"
          required
          autoComplete="email"
          error={errors.email?.message}
          {...register('email')}
        />
        <TextField
          label="Password"
          type="password"
          required
          autoComplete="current-password"
          error={errors.password?.message}
          {...register('password')}
        />
        {apiError && (
          <div className="text-[12.5px] p-2.5" style={{ background: 'var(--red-bg)', color: 'var(--red)' }}>
            {apiError}
          </div>
        )}
        <button className="btn amber justify-center mt-1" type="submit" disabled={loading}>
          {loading ? 'Signing in…' : 'Sign In'}
        </button>
      </form>
      <div className="text-[12.5px] text-inksoft mt-5 text-center">
        Don't have an account?{' '}
        <Link to="/register" className="text-ink font-semibold underline">
          Register
        </Link>
      </div>
    </AuthLayout>
  )
}
