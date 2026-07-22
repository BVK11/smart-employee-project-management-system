import { NavLink } from 'react-router-dom'
import { LayoutDashboard, Users, FolderKanban, CheckSquare, FileBarChart, X } from 'lucide-react'
import { useAuth } from '@/context/AuthContext'

const items = [
  { to: '/dashboard', label: 'Dashboard', icon: LayoutDashboard, roles: ['ADMIN', 'EMPLOYEE'] },
  { to: '/employees', label: 'Employees', icon: Users, roles: ['ADMIN'] },
  { to: '/projects', label: 'Projects', icon: FolderKanban, roles: ['ADMIN', 'EMPLOYEE'] },
  { to: '/tasks', label: 'Tasks', icon: CheckSquare, roles: ['ADMIN', 'EMPLOYEE'] },
  { to: '/reports', label: 'Reports', icon: FileBarChart, roles: ['ADMIN'] },
]

export default function Sidebar({ mobileOpen, onCloseMobile }: { mobileOpen: boolean; onCloseMobile: () => void }) {
  const { role } = useAuth()
  const visible = items.filter((i) => !role || i.roles.includes(role))

  const content = (
    <div className="h-full flex flex-col items-center py-[18px] gap-1.5 bg-sidebar">
      <div className="w-8 h-8 bg-amber text-ink font-bold mono flex items-center justify-center text-[12px] mb-[22px]">
        EM
      </div>
      {visible.map(({ to, label, icon: Icon }) => (
        <NavLink
          key={to}
          to={to}
          onClick={onCloseMobile}
          title={label}
          className={({ isActive }) =>
            `w-10 h-10 flex items-center justify-center rounded text-gray-400 hover:text-white ${
              isActive ? 'bg-white/[.08] text-white border-l-2 border-amber' : ''
            }`
          }
        >
          <Icon size={18} strokeWidth={1.8} />
        </NavLink>
      ))}
    </div>
  )

  return (
    <>
      <div className="hidden md:block w-16 shrink-0">{content}</div>
      {mobileOpen && (
        <div className="fixed inset-0 z-40 md:hidden">
          <div className="absolute inset-0 bg-ink/50" onClick={onCloseMobile} />
          <div className="absolute left-0 top-0 bottom-0 w-16 flex flex-col">
            {content}
            <button
              onClick={onCloseMobile}
              aria-label="Close menu"
              className="absolute -right-9 top-3 text-white bg-ink/70 p-1.5"
            >
              <X size={16} />
            </button>
          </div>
        </div>
      )}
    </>
  )
}
