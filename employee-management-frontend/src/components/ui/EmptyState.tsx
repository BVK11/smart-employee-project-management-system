import type { LucideIcon } from 'lucide-react'
import { Inbox } from 'lucide-react'

interface Props {
  title: string
  description?: string
  icon?: LucideIcon
  action?: React.ReactNode
}

export default function EmptyState({ title, description, icon: Icon = Inbox, action }: Props) {
  return (
    <div className="flex flex-col items-center justify-center text-center py-16 px-4">
      <Icon size={30} color="var(--ink-soft)" strokeWidth={1.5} />
      <div className="mt-3 font-semibold text-[14px]">{title}</div>
      {description && <div className="mt-1 text-[12.5px] text-inksoft max-w-sm">{description}</div>}
      {action && <div className="mt-4">{action}</div>}
    </div>
  )
}
