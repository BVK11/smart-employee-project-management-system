import { Link } from 'react-router-dom'
import { ShieldAlert } from 'lucide-react'

export default function Unauthorized() {
  return (
    <div className="min-h-screen flex items-center justify-center bg-bg p-4">
      <div className="panel text-center max-w-sm">
        <ShieldAlert size={30} color="var(--red)" className="mx-auto mb-3" />
        <h1 className="page-title">Access Denied</h1>
        <p className="text-[13px] text-inksoft mb-5">
          You don't have permission to view this page. This area is restricted to administrators.
        </p>
        <Link to="/dashboard" className="btn justify-center">
          Back to Dashboard
        </Link>
      </div>
    </div>
  )
}
