import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { FolderKanban, CheckSquare, Clock, Bell, RefreshCw } from 'lucide-react'
import SectionHeader from '@/components/ui/SectionHeader'
import KpiCard from '@/components/ui/KpiCard'
import { SkeletonBlock } from '@/components/ui/Skeleton'
import { dashboardApi } from '@/api/dashboard'
import { projectsApi } from '@/api/projects'
import { notificationsApi, type Notification } from '@/api/notifications'
import { extractErrorMessage } from '@/api/client'
import type { EmployeeDashboard } from '@/types/dashboard'
import type { Project } from '@/types/project'
import { useAuth } from '@/context/AuthContext'
import { useToast } from '@/context/ToastContext'
import { formatDate } from '@/utils/format'

export default function EmployeeDashboardPage() {
  const { employeeId } = useAuth()
  const toast = useToast()
  const [data, setData] = useState<EmployeeDashboard | null>(null)
  const [projects, setProjects] = useState<Project[]>([])
  const [notifications, setNotifications] = useState<Notification[]>([])
  const [unreadCount, setUnreadCount] = useState(0)
  const [loading, setLoading] = useState(true)

  const loadData = () => {
    if (!employeeId) {
      setLoading(false)
      return
    }
    setLoading(true)
    Promise.all([
      dashboardApi.employee(employeeId),
      projectsApi.listEmployeeProjects(),
      notificationsApi.list(),
      notificationsApi.unreadCount()
    ])
      .then(([dbData, projList, notifList, count]) => {
        setData(dbData)
        setProjects(projList)
        setNotifications(notifList)
        setUnreadCount(count)
      })
      .catch((e) => toast.error(extractErrorMessage(e, 'Could not load dashboard.')))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    loadData()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [employeeId])

  if (!employeeId) {
    return (
      <div className="panel text-center text-[13px] text-inksoft py-16">
        No employee profile associated with this account.
      </div>
    )
  }

  const pendingTasks = data ? (data.assignedTasks - data.completedTasks) : 0

  return (
    <div>
      <SectionHeader 
        eyebrow="01 — Operations Dashboard" 
        title="My Dashboard" 
        action={
          <button className="btn ghost" onClick={loadData} disabled={loading}>
            <RefreshCw size={13} className={loading ? 'animate-spin' : ''} /> Refresh
          </button>
        }
      />

      {loading ? (
        <div className="grid grid-cols-2 lg:grid-cols-5 border border-line mb-6">
          {Array.from({ length: 5 }).map((_, i) => (
            <div key={i} className="p-5 border-r border-line last:border-r-0">
              <SkeletonBlock className="h-16" />
            </div>
          ))}
        </div>
      ) : (
        <div className="grid grid-cols-2 lg:grid-cols-5 border border-t-[1.5px] border-t-ink border-line mb-6">
          <KpiCard label="Assigned Projects" value={projects.length} />
          <KpiCard label="Pending Tasks" value={pendingTasks} subTone="down" sub="Needs attention" />
          <KpiCard label="Completed Tasks" value={data?.completedTasks ?? 0} subTone="up" sub="All time" />
          <KpiCard 
            label="Upcoming Deadlines" 
            value={data?.upcomingDeadlines ?? 0} 
            subTone={data?.upcomingDeadlines && data.upcomingDeadlines > 0 ? 'down' : 'neutral'}
            sub={data?.upcomingDeadlines && data.upcomingDeadlines > 0 ? 'Due within 7 days' : 'No urgent deadlines'}
          />
          <KpiCard label="Unread Notifications" value={unreadCount} subTone={unreadCount > 0 ? 'down' : 'neutral'} />
        </div>
      )}

      <div className="grid lg:grid-cols-2 gap-6 mt-4">
        {/* Assigned Projects list */}
        <div className="panel">
          <h3 className="text-[14px] font-bold mb-4 flex items-center gap-2">
            <FolderKanban size={15} /> My Projects
          </h3>
          {projects.length === 0 ? (
            <div className="text-center text-[12.5px] text-inksoft py-8">
              No projects assigned to you yet.
            </div>
          ) : (
            <div className="flex flex-col gap-3">
              {projects.map((p) => (
                <Link
                  key={p.id}
                  to={`/projects/${p.id}`}
                  className="p-3 border border-line hover:border-ink transition-colors flex flex-col gap-2 rounded bg-card"
                >
                  <div className="flex justify-between items-center">
                    <span className="text-[13px] font-bold text-ink">{p.projectName}</span>
                    <span className="text-[11px] mono text-inksoft">Priority: {p.priority}</span>
                  </div>
                  <div>
                    <div className="flex justify-between items-center text-[10.5px] text-inksoft mb-1 font-semibold">
                      <span>Progress</span>
                      <span>{p.progress ?? 0}%</span>
                    </div>
                    <div className="w-full bg-line h-1.5 rounded overflow-hidden">
                      <div className="bg-green h-full rounded" style={{ width: `${p.progress ?? 0}%` }} />
                    </div>
                  </div>
                </Link>
              ))}
            </div>
          )}
        </div>

        {/* Recent Notifications feed */}
        <div className="panel">
          <h3 className="text-[14px] font-bold mb-4 flex items-center gap-2">
            <Bell size={15} /> Recent Activity & Notifications
          </h3>
          {notifications.length === 0 ? (
            <div className="text-center text-[12.5px] text-inksoft py-8">
              No recent notifications.
            </div>
          ) : (
            <div className="flex flex-col gap-3 max-h-[300px] overflow-y-auto divide-y divide-line pr-2">
              {notifications.slice(0, 5).map((n) => (
                <div key={n.id} className="pt-3.5 first:pt-0 flex flex-col gap-1">
                  <div className="flex justify-between items-center">
                    <span className="text-[12.5px] font-bold text-ink leading-snug">{n.title}</span>
                    <span className="text-[10px] text-inksoft/60 mono">{formatDate(n.timestamp)}</span>
                  </div>
                  <p className="text-[11.5px] text-inksoft m-0">{n.message}</p>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
