import type { ReactNode } from 'react'

interface PillProps {
  tone?: string
  children: ReactNode
}

export function Pill({ tone = 'neutral', children }: PillProps) {
  return <span className={`pill ${tone}`}>{children}</span>
}
