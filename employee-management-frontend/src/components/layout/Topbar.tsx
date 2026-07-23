import { useLocation } from 'react-router-dom'
import { Menu, LogOut, User, Sun, Moon } from 'lucide-react'
import { useAuth } from '@/context/AuthContext'
import { useTheme } from '@/context/ThemeContext'
import NotificationCenter from '@/components/notifications/NotificationCenter'

const crumbLabels: Record<string, string> = {
  dashboard: 'Dashboard',
  employees: 'Employees',
  projects: 'Projects',
  tasks: 'Tasks',
  reports: 'Reports',
  new: 'New',
  edit: 'Edit',
}

export default function Topbar({ onMenuClick }: { onMenuClick: () => void }) {
  const { role, name, logout } = useAuth()
  const { theme, toggleTheme } = useTheme()
  const location = useLocation()
  const segments = location.pathname.split('/').filter(Boolean)
  const crumb = segments.map((s) => crumbLabels[s] ?? s).join(' / ') || 'Dashboard'

  return (
    <div className="bg-card border-b border-line px-4 sm:px-7 transition-colors duration-200">
      <div className="topstrip-inner flex justify-between items-center py-3 border-b border-line">
        <div className="flex items-center gap-3">
          <button className="md:hidden text-ink" onClick={onMenuClick} aria-label="Open menu">
            <Menu size={20} />
          </button>
          <div className="mono text-[11px] sm:text-[12px] tracking-widest uppercase text-inksoft">
            Smart EMS — Operations Console
          </div>
        </div>
        <div className="flex items-center gap-3 sm:gap-4">
          {/* Dark Mode Toggle Button */}
          <button
            onClick={toggleTheme}
            className="p-1.5 rounded border border-line text-ink hover:bg-card-hover transition-colors"
            title={`Switch to ${theme === 'light' ? 'Dark' : 'Light'} Mode`}
            aria-label="Toggle theme"
          >
            {theme === 'light' ? <Moon size={16} /> : <Sun size={16} className="text-amber" />}
          </button>

          <NotificationCenter />
          <div className="hidden sm:flex items-center gap-1.5 text-[12px] text-inksoft">
            <User size={13} />
            <span className="font-semibold text-ink">{name ?? 'User'}</span>
            <span className="mono text-[10px]">({role ?? 'GUEST'})</span>
          </div>
          <button className="btn ghost !py-1.5 !px-3" onClick={logout}>
            <LogOut size={13} />
            <span className="hidden sm:inline">Logout</span>
          </button>
        </div>
      </div>
      <div className="text-[12px] text-inksoft py-2.5 hidden sm:block">
        Console / <b className="text-ink">{crumb}</b>
      </div>
    </div>
  )
}
