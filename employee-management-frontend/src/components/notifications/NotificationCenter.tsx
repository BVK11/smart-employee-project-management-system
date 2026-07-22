import { useEffect, useState, useRef } from 'react'
import { Bell, Check, MailOpen } from 'lucide-react'
import { notificationsApi, type Notification } from '@/api/notifications'
import { formatDate } from '@/utils/format'

export default function NotificationCenter() {
  const [notifications, setNotifications] = useState<Notification[]>([])
  const [unreadCount, setUnreadCount] = useState(0)
  const [open, setOpen] = useState(false)
  const panelRef = useRef<HTMLDivElement>(null)

  const loadNotifications = () => {
    notificationsApi.list()
      .then(setNotifications)
      .catch(() => {})
    
    notificationsApi.unreadCount()
      .then(setUnreadCount)
      .catch(() => {})
  }

  useEffect(() => {
    loadNotifications()
    // Poll for new notifications every 10 seconds
    const interval = setInterval(loadNotifications, 10000)
    return () => clearInterval(interval)
  }, [])

  // Close panel when clicking outside
  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (panelRef.current && !panelRef.current.contains(event.target as Node)) {
        setOpen(false)
      }
    }
    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [])

  const markRead = async (id: number) => {
    try {
      await notificationsApi.markRead(id)
      setNotifications(prev => prev.map(n => n.id === id ? { ...n, isRead: true } : n))
      setUnreadCount(prev => Math.max(0, prev - 1))
    } catch {}
  }

  const markAllRead = async () => {
    try {
      await notificationsApi.markAllRead()
      setNotifications(prev => prev.map(n => ({ ...n, isRead: true })))
      setUnreadCount(0)
    } catch {}
  }

  return (
    <div className="relative" ref={panelRef}>
      <button
        onClick={() => setOpen(!open)}
        className="relative p-1.5 text-inksoft hover:text-ink transition-colors focus:outline-none"
        aria-label="Notifications"
      >
        <Bell size={20} />
        {unreadCount > 0 && (
          <span className="absolute top-0 right-0 inline-flex items-center justify-center px-1.5 py-0.5 text-[9.5px] font-bold leading-none text-white bg-red rounded-full transform translate-x-1/3 -translate-y-1/3">
            {unreadCount}
          </span>
        )}
      </button>

      {open && (
        <div className="absolute right-0 mt-2 w-80 sm:w-96 bg-card border border-line shadow-lg z-50 overflow-hidden flex flex-col max-h-[480px]">
          <div className="p-3.5 border-b border-line flex justify-between items-center bg-bg">
            <span className="text-[13px] font-bold">Notifications</span>
            {unreadCount > 0 && (
              <button
                onClick={markAllRead}
                className="text-[11px] font-semibold text-inksoft hover:text-ink flex items-center gap-1"
              >
                <MailOpen size={12} /> Mark all read
              </button>
            )}
          </div>

          <div className="overflow-y-auto divide-y divide-line flex-1">
            {notifications.length === 0 ? (
              <div className="p-8 text-center text-[12.5px] text-inksoft">
                No notifications yet.
              </div>
            ) : (
              notifications.map((n) => (
                <div
                  key={n.id}
                  className={`p-3.5 flex flex-col gap-1 transition-colors ${
                    n.isRead ? 'opacity-70 bg-card' : 'bg-white/[.02] border-l-2 border-amber'
                  }`}
                >
                  <div className="flex justify-between items-start gap-2">
                    <span className="text-[12.5px] font-bold text-ink leading-snug">{n.title}</span>
                    {!n.isRead && (
                      <button
                        onClick={() => markRead(n.id)}
                        className="text-inksoft hover:text-green p-0.5 shrink-0"
                        title="Mark as read"
                      >
                        <Check size={13} />
                      </button>
                    )}
                  </div>
                  <p className="text-[12px] text-inksoft m-0 leading-normal">{n.message}</p>
                  <span className="text-[10px] text-inksoft/60 mt-1 mono">
                    {formatDate(n.timestamp)}
                  </span>
                </div>
              ))
            )}
          </div>
        </div>
      )}
    </div>
  )
}
