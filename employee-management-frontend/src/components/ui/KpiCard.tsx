interface Props {
  label: string
  value: string | number
  sub?: string
  subTone?: 'up' | 'down' | 'neutral'
}

export default function KpiCard({ label, value, sub, subTone = 'neutral' }: Props) {
  const color = subTone === 'up' ? 'var(--green)' : subTone === 'down' ? 'var(--red)' : 'var(--ink-soft)'
  return (
    <div className="p-4 sm:p-5 border-r border-line last:border-r-0 border-b sm:border-b-0">
      <div className="text-[11px] uppercase tracking-wide text-inksoft mb-2">{label}</div>
      <div className="mono text-[24px] sm:text-[26px] font-semibold">{value}</div>
      {sub && (
        <div className="text-[11.5px] mt-1.5" style={{ color }}>
          {sub}
        </div>
      )}
    </div>
  )
}
