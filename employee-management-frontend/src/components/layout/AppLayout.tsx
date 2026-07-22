import { useState } from 'react'
import { Outlet } from 'react-router-dom'
import Sidebar from './Sidebar'
import Topbar from './Topbar'

export default function AppLayout() {
  const [mobileOpen, setMobileOpen] = useState(false)

  return (
    <div className="min-h-screen grid grid-cols-1 md:grid-cols-[64px_1fr] bg-bg">
      <Sidebar mobileOpen={mobileOpen} onCloseMobile={() => setMobileOpen(false)} />
      <main className="min-w-0">
        <Topbar onMenuClick={() => setMobileOpen(true)} />
        <div className="p-4 sm:p-7">
          <Outlet />
        </div>
      </main>
    </div>
  )
}
