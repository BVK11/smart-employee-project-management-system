import type { ReactNode } from 'react'

export default function AuthLayout({ title, eyebrow, children }: { title: string; eyebrow: string; children: ReactNode }) {
  return (
    <div className="min-h-screen bg-bg flex items-center justify-center p-4">
      <div className="w-full max-w-[420px]">
        <div className="flex items-center gap-2.5 mb-6 justify-center">
          <div className="w-8 h-8 bg-ink text-white font-bold mono flex items-center justify-center text-[12px]">
            EM
          </div>
          <div className="mono text-[12px] tracking-widest uppercase text-inksoft">Smart EMS</div>
        </div>
        <div className="panel">
          <div className="eyebrow">{eyebrow}</div>
          <h1 className="page-title">{title}</h1>
          {children}
        </div>
      </div>
    </div>
  )
}
