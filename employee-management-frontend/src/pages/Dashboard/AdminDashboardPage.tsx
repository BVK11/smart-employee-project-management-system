import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip, Legend } from 'recharts'
import { Users, FolderKanban, CheckSquare, FileBarChart, ArrowRight, Bell } from 'lucide-react'
import SectionHeader from '@/components/ui/SectionHeader'
import KpiCard from '@/components/ui/KpiCard'
import { SkeletonBlock } from '@/components/ui/Skeleton'
import { dashboardApi } from '@/api/dashboard'
import { projectsApi } from '@/api/projects'
import { notificationsApi, type Notification } from '@/api/notifications'
import { extractErrorMessage } from '@/api/client'
import type { AdminDashboard } from '@/types/dashboard'
import type { Project } from '@/types/project'
import { useToast } from '@/context/ToastContext'
import { formatDate } from '@/utils/format'

const COLORS = ['var(--green)', 'var(--red)']

export default function AdminDashboardPage() {
  const toast = useToast()
  const [data, setData] = useState<AdminDashboard | null>(null)
  const [projects, setProjects] = useState<Project[]>([])
  const [notifications, setNotifications] = useState<Notification[]>([])
  const [loading, setLoading] = useState(true)

  const loadData = () => {
    setLoading(true)
    Promise.all([
      dashboardApi.admin(),
      projectsApi.list(),
      notificationsApi.list()
    ])
      .then(([dbData, projList, notifList]) => {
        setData(dbData)
        setProjects(projList)
        setNotifications(notifList)
      })
      .catch((e) => toast.error(extractErrorMessage(e, 'Could not load dashboard data.')))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    loadData()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const chartData = data
    ? [
        { name: 'Completed', value: data.completedTasks },
        { name: 'Pending', value: data.pendingTasks },
      ]
    : []

  const activeProjects = projects.filter(p => p.status !== 'COMPLETED').length
  const completedProjects = projects.filter(p => p.status === 'COMPLETED').length

  return (
    <div>
      <SectionHeader eyebrow="01 — Organization Snapshot" title="Admin Dashboard" />

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
          <KpiCard label="Total Employees" value={data?.totalEmployees ?? 0} />
          <KpiCard label="Active Projects" value={activeProjects} />
          <KpiCard label="Completed Projects" value={completedProjects} subTone="up" />
          <KpiCard label="Pending Tasks" value={data?.pendingTasks ?? 0} subTone="down" sub="Needs attention" />
          <KpiCard label="Completed Tasks" value={data?.completedTasks ?? 0} subTone="up" sub="All time" />
        </div>
      )}

      <div className="grid lg:grid-cols-[1.4fr_1fr] gap-6 mb-6">
        {/* Left Column: Chart & Recent Projects */}
        <div className="flex flex-col gap-6">
          <div className="panel">
            <div className="flex justify-between items-baseline mb-4">
              <h3 className="text-[14px] font-bold m-0">Task Status Overview</h3>
              <span className="text-[11px] mono text-inksoft">
                N = {(data?.completedTasks ?? 0) + (data?.pendingTasks ?? 0)}
              </span>
            </div>
            {loading ? (
              <SkeletonBlock className="h-64" />
            ) : (
              <ResponsiveContainer width="100%" height={260}>
                <PieChart>
                  <Pie data={chartData} dataKey="value" nameKey="name" innerRadius={65} outerRadius={95} paddingAngle={2}>
                    {chartData.map((_, i) => (
                      <Cell key={i} fill={COLORS[i % COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip />
                  <Legend />
                </PieChart>
              </ResponsiveContainer>
            )}
          </div>

          <div className="panel">
            <h3 className="text-[14px] font-bold mb-4 flex items-center gap-2">
              <FolderKanban size={15} /> Recent Projects
            </h3>
            {projects.length === 0 ? (
              <div className="text-center text-[12.5px] text-inksoft py-6">No projects defined yet.</div>
            ) : (
              <div className="flex flex-col gap-3">
                {projects.slice(0, 4).map((p) => (
                  <Link
                    key={p.id}
                    to={`/projects/${p.id}`}
                    className="p-3 border border-line hover:border-ink transition-colors flex flex-col gap-2 rounded bg-card"
                  >
                    <div className="flex justify-between items-center">
                      <span className="text-[13px] font-bold text-ink">{p.projectName}</span>
                      <span className="text-[11px] mono text-inksoft">{p.status}</span>
                    </div>
                    <div>
                      <div className="flex justify-between items-center text-[10px] text-inksoft mb-0.5 font-semibold">
                        <span>Progress</span>
                        <span>{p.progress ?? 0}%</span>
                      </div>
                      <div className="w-full bg-line h-1 rounded overflow-hidden">
                        <div className="bg-green h-full rounded" style={{ width: `${p.progress ?? 0}%` }} />
                      </div>
                    </div>
                  </Link>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* Right Column: Quick Links & Recent Notifications */}
        <div className="flex flex-col gap-6">
          <div className="panel">
            <h3 className="text-[14px] font-bold mb-4">Quick Links</h3>
            <div className="flex flex-col gap-2">
              {[
                { to: '/employees', label: 'Employees', icon: Users },
                { to: '/projects', label: 'Projects', icon: FolderKanban },
                { to: '/tasks', label: 'Tasks', icon: CheckSquare },
                { to: '/reports', label: 'Reports', icon: FileBarChart },
              ].map(({ to, label, icon: Icon }) => (
                <Link
                  key={to}
                  to={to}
                  className="flex items-center justify-between px-3 py-2.5 border border-line hover:border-ink transition-colors"
                >
                  <span className="flex items-center gap-2.5 text-[12.5px] font-medium">
                    <Icon size={15} /> {label}
                  </span>
                  <ArrowRight size={13} color="var(--ink-soft)" />
                </Link>
              ))}
            </div>
          </div>

          <div className="panel flex-1">
            <h3 className="text-[14px] font-bold mb-4 flex items-center gap-2">
              <Bell size={15} /> Recent System Activities
            </h3>
            {notifications.length === 0 ? (
              <div className="text-center text-[12.5px] text-inksoft py-8">
                No recent activities recorded.
              </div>
            ) : (
              <div className="flex flex-col gap-3 divide-y divide-line max-h-[350px] overflow-y-auto pr-1">
                {notifications.slice(0, 6).map((n) => (
                  <div key={n.id} className="pt-3 first:pt-0 flex flex-col gap-0.5">
                    <div className="flex justify-between items-start gap-2">
                      <span className="text-[12px] font-bold text-ink leading-tight">{n.title}</span>
                      <span className="text-[9.5px] text-inksoft/60 mono whitespace-nowrap">{formatDate(n.timestamp)}</span>
                    </div>
                    <p className="text-[11.5px] text-inksoft m-0 leading-normal">{n.message}</p>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}
