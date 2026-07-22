import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import AuthLayout from './AuthLayout'
import { useAuth } from '@/context/AuthContext'
import type { Role } from '@/types/auth'
import { TextField } from '@/components/ui/FormField'

// TEMPORARY screen: the backend login response does not include role or
// employeeId. This lets the signed-in user tell the frontend which
// dashboard/permissions to use, persisted locally. Replace once a real
// profile/"me" endpoint exists.
export default function SelectRolePage() {
  const { setRole, setEmployeeId } = useAuth()
  const navigate = useNavigate()
  const [selected, setSelected] = useState<Role>('EMPLOYEE')
  const [empId, setEmpId] = useState('')

  const onContinue = () => {
    setRole(selected)
    if (selected === 'EMPLOYEE' && empId) setEmployeeId(Number(empId))
    navigate('/dashboard', { replace: true })
  }

  return (
    <AuthLayout eyebrow="02 — One-Time Setup" title="Confirm your role">
      <p className="text-[12.5px] text-inksoft mb-4">
        The backend doesn't yet return your role or employee ID after login. Tell us which one applies so we can show
        the right screens. Backend permissions are still enforced server-side regardless of this choice.
      </p>
      <div className="flex flex-col gap-4">
        <div className="grid grid-cols-2 gap-2">
          {(['ADMIN', 'EMPLOYEE'] as Role[]).map((r) => (
            <button
              key={r}
              type="button"
              onClick={() => setSelected(r)}
              className={`btn ${selected === r ? '' : 'ghost'} justify-center`}
            >
              {r}
            </button>
          ))}
        </div>
        {selected === 'EMPLOYEE' && (
          <TextField
            label="Your Employee ID"
            type="number"
            hint="Used to load your dashboard and assigned tasks"
            value={empId}
            onChange={(e) => setEmpId(e.target.value)}
          />
        )}
        <button className="btn amber justify-center mt-1" onClick={onContinue}>
          Continue
        </button>
      </div>
    </AuthLayout>
  )
}
