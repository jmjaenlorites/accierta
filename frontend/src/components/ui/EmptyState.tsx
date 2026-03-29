import type { ReactNode } from 'react'

interface EmptyStateProps {
  icon?: ReactNode
  title: string
  subtitle?: string
  action?: ReactNode
}

export function EmptyState({ icon, title, subtitle, action }: EmptyStateProps) {
  return (
    <div className="empty-state">
      {icon && <div className="empty-state-icon">{icon}</div>}
      <p className="card-title">{title}</p>
      {subtitle && <p className="muted">{subtitle}</p>}
      {action}
    </div>
  )
}
