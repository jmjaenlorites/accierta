import type { ReactNode } from 'react'

interface CardProps {
  children: ReactNode
  hero?: boolean
  className?: string
}

export function Card({ children, hero = false, className = '' }: CardProps) {
  const cls = ['card', 'card-body', hero ? 'hero' : '', className].filter(Boolean).join(' ')
  return <div className={cls}>{children}</div>
}
