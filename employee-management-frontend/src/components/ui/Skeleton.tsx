export function SkeletonRow({ cols = 5 }: { cols?: number }) {
  return (
    <tr>
      {Array.from({ length: cols }).map((_, i) => (
        <td key={i}>
          <div className="h-3.5 bg-line/60 animate-pulse" style={{ width: `${60 + (i % 3) * 15}%` }} />
        </td>
      ))}
    </tr>
  )
}

export function SkeletonBlock({ className = 'h-24' }: { className?: string }) {
  return <div className={`bg-line/40 animate-pulse ${className}`} />
}
