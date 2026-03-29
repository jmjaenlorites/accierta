interface MetricCardProps {
  label: string
  value: string
  tone?: 'win' | 'loss' | 'warn' | ''
}

export function MetricCard({ label, value, tone = '' }: MetricCardProps) {
  return (
    <div className="metric-card">
      <div className="metric-label">{label}</div>
      <div className={`metric-value${tone ? ` ${tone}` : ''}`}>{value}</div>
    </div>
  )
}
