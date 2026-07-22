interface Props {
  value: string
  map?: Record<string, 'done' | 'wip' | 'pending' | 'neutral'>
}

const defaultMap: Record<string, 'done' | 'wip' | 'pending' | 'neutral'> = {
  ACTIVE: 'done',
  COMPLETED: 'done',
  IN_PROGRESS: 'wip',
  ON_HOLD: 'wip',
  PENDING: 'pending',
  INACTIVE: 'pending',
  HIGH: 'pending',
  MEDIUM: 'wip',
  LOW: 'done',
}

export default function StatusBadge({ value, map = defaultMap }: Props) {
  const cls = map[value] ?? 'neutral'
  return <span className={`status-tag ${cls}`}>{value?.replace(/_/g, ' ')}</span>
}
