import { createContext, useCallback, useContext, useState, type ReactNode } from 'react'
import { CheckCircle2, XCircle, X } from 'lucide-react'

type ToastKind = 'success' | 'error' | 'info'
interface Toast { id: number; kind: ToastKind; message: string }

interface ToastContextValue {
  success: (msg: string) => void
  error: (msg: string) => void
  info: (msg: string) => void
}

const ToastContext = createContext<ToastContextValue | undefined>(undefined)

export function ToastProvider({ children }: { children: ReactNode }) {
  const [toasts, setToasts] = useState<Toast[]>([])

  const push = useCallback((kind: ToastKind, message: string) => {
    const id = Date.now() + Math.random()
    setToasts((t) => [...t, { id, kind, message }])
    setTimeout(() => setToasts((t) => t.filter((x) => x.id !== id)), 4500)
  }, [])

  const value: ToastContextValue = {
    success: (msg) => push('success', msg),
    error: (msg) => push('error', msg),
    info: (msg) => push('info', msg),
  }

  return (
    <ToastContext.Provider value={value}>
      {children}
      <div className="fixed bottom-4 right-4 z-[100] flex flex-col gap-2 w-[min(360px,90vw)]">
        {toasts.map((t) => (
          <div
            key={t.id}
            className="panel flex items-start gap-2 shadow-lg animate-in"
            style={{
              borderLeft: `3px solid ${t.kind === 'success' ? 'var(--green)' : t.kind === 'error' ? 'var(--red)' : 'var(--amber)'}`,
            }}
          >
            {t.kind === 'success' ? (
              <CheckCircle2 size={18} color="var(--green)" className="shrink-0 mt-0.5" />
            ) : t.kind === 'error' ? (
              <XCircle size={18} color="var(--red)" className="shrink-0 mt-0.5" />
            ) : (
              <CheckCircle2 size={18} color="var(--amber)" className="shrink-0 mt-0.5" />
            )}
            <div className="text-[13px] flex-1">{t.message}</div>
            <button
              onClick={() => setToasts((ts) => ts.filter((x) => x.id !== t.id))}
              aria-label="Dismiss notification"
              className="text-inksoft hover:text-ink"
            >
              <X size={14} />
            </button>
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  )
}

export function useToast() {
  const ctx = useContext(ToastContext)
  if (!ctx) throw new Error('useToast must be used within ToastProvider')
  return ctx
}
