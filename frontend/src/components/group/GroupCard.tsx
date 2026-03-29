import { AlertCircle, BellRing, ChevronRight, Coins, Users } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Link } from 'react-router-dom'
import { avatarColor } from '../../lib/format'
import type { GroupInsights, GroupSummary } from '../../lib/types'
import { Pill } from '../ui/Pill'

interface GroupCardProps {
  group: GroupSummary
  insights?: GroupInsights
}

function GroupAvatar({ name, groupId }: { name: string; groupId: string }) {
  const initial = name[0]?.toUpperCase() ?? 'G'
  return (
    <div
      style={{
        width: 44,
        height: 44,
        borderRadius: 12,
        background: avatarColor(groupId),
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        color: 'white',
        fontWeight: 800,
        fontFamily: '"Plus Jakarta Sans", sans-serif',
        fontSize: 18,
        flexShrink: 0,
      }}
    >
      {initial}
    </div>
  )
}

export function GroupCard({ group, insights }: GroupCardProps) {
  const { t } = useTranslation()
  const hasOpen = (insights?.openPorras ?? 0) > 0
  const hasPending = (insights?.pendingDebts ?? 0) > 0 || (insights?.pendingResolution ?? 0) > 0

  return (
    <Link className="card interactive group-card" to={`/groups/${group.groupId}`}>
      <div className="row between">
        <div className="row" style={{ gap: 12, minWidth: 0 }}>
          <GroupAvatar name={group.name} groupId={group.groupId} />
          <div style={{ minWidth: 0 }}>
            <div className="card-title" style={{ fontSize: 'var(--text-base)' }}>
              {group.name}
            </div>
            <div className="row" style={{ gap: 8, marginTop: 4, flexWrap: 'wrap' }}>
              <span className="muted" style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                <Users size={12} /> {group.memberCount}
              </span>
              <span className="muted" style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                <Coins size={12} /> {group.currency}
              </span>
            </div>
          </div>
        </div>
        <ChevronRight size={18} color="var(--text-muted)" />
      </div>

      {(hasOpen || hasPending) && (
        <div className="row wrap" style={{ gap: 8 }}>
          {hasOpen && (
            <Pill tone="open">
              <BellRing size={12} />
              {t('groupCard.openCount', { count: insights?.openPorras ?? 0 })}
            </Pill>
          )}
          {hasPending && (
            <Pill tone="pending">
              <AlertCircle size={12} />
              {t('groupCard.pendingAction')}
            </Pill>
          )}
        </div>
      )}
    </Link>
  )
}
