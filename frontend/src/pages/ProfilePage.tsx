import { LogOut } from 'lucide-react'
import { useEffect, useMemo, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { useNavigate } from 'react-router-dom'
import { Header } from '../components/layout/Header'
import { Avatar } from '../components/ui/Avatar'
import { Button } from '../components/ui/Button'
import { EmptyState } from '../components/ui/EmptyState'
import { MetricCard } from '../components/ui/MetricCard'
import { CardSkeleton } from '../components/ui/Skeleton'
import { formatDate, formatMoney, historyOutcomeLabel, shortEmail } from '../lib/format'
import { authService, createAuthedServices } from '../lib/services'
import { useSession } from '../lib/session'
import type { GroupSummary, MemberHistory, MemberStats } from '../lib/types'

export function ProfilePage() {
  const { session, setSession, clearSession, currentGroupId, user } = useSession()
  const navigate = useNavigate()
  const [groups, setGroups] = useState<GroupSummary[]>([])
  const [selectedGroupId, setSelectedGroupId] = useState(currentGroupId)
  const [stats, setStats] = useState<MemberStats | null>(null)
  const [history, setHistory] = useState<MemberHistory | null>(null)
  const [loading, setLoading] = useState(false)
  const { t, i18n } = useTranslation()

  const services = useMemo(
    () =>
      createAuthedServices({
        getSession: () => session,
        setSession,
        clearSession,
      }),
    [clearSession, session, setSession],
  )

  useEffect(() => {
    void services.groups.list().then((nextGroups) => {
      setGroups(nextGroups)
      if (!selectedGroupId && nextGroups[0]?.groupId) {
        setSelectedGroupId(nextGroups[0].groupId)
      }
    })
  }, [])

  useEffect(() => {
    if (!selectedGroupId || !user?.sub) return
    setLoading(true)
    void Promise.all([
      services.settlement.stats(selectedGroupId, user.sub).then(setStats).catch(() => setStats(null)),
      services.settlement.history(selectedGroupId, user.sub).then(setHistory).catch(() => setHistory(null)),
    ]).then(() => setLoading(false))
  }, [selectedGroupId, user?.sub])

  async function handleLogout() {
    if (session?.refreshToken) {
      try { await authService.logout(session.refreshToken) } catch { /* ignore */ }
    }
    clearSession()
    navigate('/welcome')
  }

  return (
    <>
      <Header
        title={t('profilePage.eyebrow')}
        rightSlot={
          <Button variant="ghost" size="sm" onClick={handleLogout}>
            <LogOut size={14} /> {t('common.logout')}
          </Button>
        }
      />

      <div className="screen" style={{ paddingTop: 'var(--space-4)' }}>
        {/* User card */}
        <div className="card card-body anim-fade-in">
          <div className="row" style={{ gap: 'var(--space-3)' }}>
            <Avatar email={user?.email ?? 'anon@example.com'} userId={user?.sub ?? 'guest'} size="lg" />
            <div>
              <div className="card-title">{shortEmail(user?.email ?? t('common.anonymous'))}</div>
              <div className="muted">{user?.email}</div>
            </div>
          </div>

          {/* Language selector */}
          <div className="field">
            <label htmlFor="profile-lang">{t('profilePage.language')}</label>
            <select
              className="select"
              id="profile-lang"
              value={i18n.language}
              onChange={(e) => void i18n.changeLanguage(e.target.value)}
            >
              <option value="es-ES">Español</option>
              <option value="en-US">English</option>
            </select>
          </div>
        </div>

        {/* Group context picker */}
        {groups.length > 0 && (
          <div className="card card-body">
            <div>
              <div className="eyebrow">{t('profilePage.groupsEyebrow')}</div>
              <h2 className="section-title">{t('profilePage.groupsTitle')}</h2>
            </div>
            <div className="pill-row">
              {groups.map((group) => (
                <button
                  key={group.groupId}
                  className={`pill-filter-btn${selectedGroupId === group.groupId ? ' active' : ''}`}
                  onClick={() => setSelectedGroupId(group.groupId)}
                  type="button"
                >
                  {group.name}
                </button>
              ))}
            </div>
          </div>
        )}

        {/* Stats */}
        {loading ? (
          <CardSkeleton />
        ) : stats ? (
          <div className="card card-body">
            <div>
              <div className="eyebrow">{t('profilePage.statsEyebrow')}</div>
              <h2 className="section-title">{t('profilePage.statsTitle')}</h2>
            </div>
            <div className="grid-2">
              <MetricCard label={t('profilePage.netMoney')} value={formatMoney(stats.netMoney, stats.currency)} />
              <MetricCard label={t('profilePage.won')} value={String(stats.won)} />
              <MetricCard label={t('profilePage.ratio')} value={String(stats.winRatio)} />
              <MetricCard label={t('profilePage.pending')} value={formatMoney(stats.pendingDebtsOwing, stats.currency)} />
            </div>
          </div>
        ) : selectedGroupId ? null : (
          <div className="card card-body">
            <EmptyState
              icon={<span style={{ fontSize: 24 }}>👤</span>}
              title={t('profilePage.noGroupTitle')}
              subtitle={t('profilePage.noGroupSubtitle')}
            />
          </div>
        )}

        {/* History */}
        {history ? (
          <div className="card card-body">
            <div>
              <div className="eyebrow">{t('profilePage.historyEyebrow')}</div>
              <h2 className="section-title">{t('profilePage.historyTitle')}</h2>
            </div>
            {history.content.length ? (
              <div className="stack sm">
                {history.content.map((entry) => (
                  <div className="history-card" key={entry.porraId}>
                    <div className="row between">
                      <div style={{ flex: 1, minWidth: 0 }}>
                        <div className="list-item-title">{entry.title}</div>
                        <div className="list-item-sub">
                          {historyOutcomeLabel(entry.outcome)} · {formatDate(entry.resolvedAt)}
                        </div>
                      </div>
                      <div className="mono" style={{ fontSize: 'var(--text-sm)', flexShrink: 0 }}>
                        {formatMoney(entry.moneyChange, entry.currency)}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <EmptyState
                title={t('profilePage.noHistoryTitle')}
                subtitle={t('profilePage.noHistorySubtitle')}
              />
            )}
          </div>
        ) : null}
      </div>
    </>
  )
}
