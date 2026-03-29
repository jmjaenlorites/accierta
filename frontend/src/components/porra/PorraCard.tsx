import { Clock, Target } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Link } from 'react-router-dom'
import { humanize, statusLabel, tagClass } from '../../lib/format'
import type { PorraSummary } from '../../lib/types'
import { CountdownTimer } from '../ui/CountdownTimer'
import { Pill } from '../ui/Pill'

interface PorraCardProps {
  porra: PorraSummary
  currency: string
}

export function PorraCard({ porra, currency }: PorraCardProps) {
  const { t } = useTranslation()

  return (
    <Link className="card interactive porra-card" to={`/porras/${porra.porraId}`}>
      <div className="row between" style={{ alignItems: 'flex-start' }}>
        <div style={{ flex: 1, minWidth: 0 }}>
          <div className="card-title" style={{ fontSize: 'var(--text-base)', marginBottom: 4 }}>
            {porra.title}
          </div>
          <div className="row" style={{ gap: 8, flexWrap: 'wrap' }}>
            <span className="muted" style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
              <Target size={12} />
              {t('porra.typeAndBets', {
                type: humanize(porra.type),
                count: porra.totalBets,
                stake: porra.stake,
                currency,
              })}
            </span>
          </div>
        </div>
        <Pill tone={tagClass(porra.status)}>{statusLabel(porra.status)}</Pill>
      </div>

      {porra.status === 'OPEN' && (
        <div className="row" style={{ gap: 6 }}>
          <Clock size={14} color="var(--text-muted)" />
          <CountdownTimer deadline={porra.bettingDeadline} />
        </div>
      )}
    </Link>
  )
}
