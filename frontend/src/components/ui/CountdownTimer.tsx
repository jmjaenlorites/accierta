import { useEffect, useState } from 'react'
import { countdown } from '../../lib/format'

interface CountdownTimerProps {
  deadline: string
}

function urgencyClass(deadline: string): string {
  const diff = new Date(deadline).getTime() - Date.now()
  if (diff <= 0) return 'danger'
  if (diff < 60 * 60 * 1000) return 'danger'   // < 1h
  if (diff < 24 * 60 * 60 * 1000) return 'warning' // < 24h
  return ''
}

export function CountdownTimer({ deadline }: CountdownTimerProps) {
  const [display, setDisplay] = useState(() => countdown(deadline))
  const [cls, setCls] = useState(() => urgencyClass(deadline))

  useEffect(() => {
    const tick = () => {
      setDisplay(countdown(deadline))
      setCls(urgencyClass(deadline))
    }
    const interval = window.setInterval(tick, 30_000)
    tick()
    return () => window.clearInterval(interval)
  }, [deadline])

  return <span className={`countdown ${cls}`}>{display}</span>
}
