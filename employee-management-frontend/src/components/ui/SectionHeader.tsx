interface Props {
  eyebrow: string
  title: string
  action?: React.ReactNode
}

export default function SectionHeader({ eyebrow, title, action }: Props) {
  return (
    <div className="flex items-end justify-between mb-4 flex-wrap gap-3">
      <div>
        <div className="eyebrow">{eyebrow}</div>
        <h1 className="page-title !mb-0">{title}</h1>
      </div>
      {action}
    </div>
  )
}
