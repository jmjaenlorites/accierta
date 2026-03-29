import { Clock } from 'lucide-react'
import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Link } from 'react-router-dom'
import { PorraCard } from '../../components/porra/PorraCard'
import { Avatar } from '../../components/ui/Avatar'
import { EmptyState } from '../../components/ui/EmptyState'
import { CardSkeleton } from '../../components/ui/Skeleton'
import { shortEmail } from '../../lib/format'
import { useSession } from '../../lib/session'
import type { PorraSummary } from '../../lib/types'
import { useGroupContext } from './GroupLayout'

export function GroupHomePage() {
  const { group, services } = useGroupContext()
  const { user } = useSession()
  const [porras, setPorras] = useState<PorraSummary[]>([])
  const [loading, setLoading] = useState(true)
  const { t } = useTranslation()

  useEffect(() => {
    setLoading(true)
    void services.porras.list(group.groupId).then((data) => {
      setPorras(data)
      setLoading(false)
    })
  }, [group.groupId])

  const open = porras.filter((p) => p.status === 'OPEN').slice(0, 3)

  return (
    <div className="group-screen anim-fade-in">
      {/* Quick actions */}
      <div className="card card-body hero">
        <div>
          <div className="eyebrow">{t('group.actionsEyebrow')}</div>
          <h2 className="section-title">{group.name}</h2>
          <p className="muted" style={{ marginTop: 4 }}>
            {group.members.length} {t('group.memberCount').toLowerCase()} · {group.currency}
          </p>
        </div>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 'var(--space-2)' }}>
          <Link className="btn secondary sm" to={`/groups/${group.groupId}/porras`}>
            {t('group.viewOpenPorras')}
          </Link>
          <Link className="btn ghost sm" to={`/groups/${group.groupId}/ranking`}>
            {t('group.viewRanking')}
          </Link>
        </div>
      </div>

      {/* Open porras */}
      <div className="card card-body">
        <div className="row between" style={{ alignItems: 'flex-start' }}>
          <div>
            <div className="eyebrow">{t('group.openEyebrow')}</div>
            <h2 className="section-title">{t('group.openTitle')}</h2>
          </div>
          <Link
            to={`/groups/${group.groupId}/porras`}
            style={{ fontSize: 'var(--text-sm)', color: 'var(--primary)', fontWeight: 600, whiteSpace: 'nowrap' }}
          >
            {t('group.porrasTitle')} →
          </Link>
        </div>

        {loading ? (
          <CardSkeleton />
        ) : open.length ? (
          <div className="stack anim-fade-in stagger">
            {open.map((porra) => (
              <PorraCard key={porra.porraId} porra={porra} currency={group.currency} />
            ))}
          </div>
        ) : (
          <EmptyState
            icon={<Clock size={24} />}
            title={t('group.noOpenTitle')}
            subtitle={t('group.noOpenSubtitle')}
          />
        )}
      </div>

      {/* Members preview */}
      <div className="card card-body">
        <div className="row between">
          <div>
            <div className="eyebrow">{t('group.membersEyebrow')}</div>
            <h2 className="section-title">{t('group.membersTitle')}</h2>
          </div>
          <Link
            to={`/groups/${group.groupId}/profile`}
            style={{ fontSize: 'var(--text-sm)', color: 'var(--primary)', fontWeight: 600 }}
          >
            {t('group.invitesTitle')} →
          </Link>
        </div>
        <div className="stack sm">
          {group.members.slice(0, 5).map((member) => (
            <div className="list-item" key={member.userId}>
              <Avatar email={member.email} userId={member.userId} size="sm" />
              <div className="list-item-content">
                <div className="list-item-title">{shortEmail(member.email)}</div>
              </div>
              {member.userId === user?.sub && (
                <span className="pill primary" style={{ fontSize: 10 }}>tú</span>
              )}
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}
