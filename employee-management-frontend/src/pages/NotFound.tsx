import { Link } from 'react-router-dom'
import { FileQuestion } from 'lucide-react'

export default function NotFound() {
  return (
    <div className="min-h-screen flex items-center justify-center bg-bg p-4">
      <div className="panel text-center max-w-sm">
        <FileQuestion size={30} color="var(--ink-soft)" className="mx-auto mb-3" />
        <h1 className="page-title">Page Not Found</h1>
        <p className="text-[13px] text-inksoft mb-5">The page you're looking for doesn't exist or has moved.</p>
        <Link to="/dashboard" className="btn justify-center">
          Back to Dashboard
        </Link>
      </div>
    </div>
  )
}
