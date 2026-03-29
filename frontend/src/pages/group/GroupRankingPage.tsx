import { useEffect, useRef, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { PodiumDisplay, MemberRow } from '../../components/ranking/PodiumDisplay'
import { EmptyState } from '../../components/ui/EmptyState'
import { CardSkeleton } from '../../components/ui/Skeleton'
import { formatMoney, metricForRank } from '../../lib/format'
import { subscribeToLeaderboard } from '../../lib/stomp'
import { useToasts } from '../../lib/toasts'
import type { Leaderboard } from '../../lib/types'
import { useGroupContext } from './GroupLayout'

export function GroupRankingPage() {
  const { group, services } = useGroupContext()
  const { pushToast } = useToasts()
  const { t } = useTranslation()
  const [rankBy, setRankBy] = useState('NET_MONEY')
  const [leaderboard, setLeaderboard] = useState<Leaderboard | null>(null)
  const [loading, setLoading] = useState(true)
  const subscriptionRef = useRef<{ disconnect: () => void } | null>(null)

  useEffect(() => {
    setLoading(true)
    void services.settlement.leaderboard(group.groupId, rankBy).then((data) => {
      setLeaderboard(data)
      setLoading(false)
    })
  }, [group.groupId, rankBy])

  useEffect(() => {
    subscriptionRef.current?.disconnect()
    subscriptionRef.current = subscribeToLeaderboard(group.groupId, (payload) => {
      try {
        setLeaderboard(JSON.parse(payload) as Leaderboard)
        pushToast(t('ranking.liveTitle'), t('ranking.liveMessage'), 'success')
      } catch {
        // ignore bad payloads
      }
    })
    return () => subscriptionRef.current?.disconnect()
  }, [group.groupId])

  const rankOptions = [
    ['NET_MONEY', t('ranking.net')],
    ['PORRAS_WON', t('ranking.won')],
    ['WIN_RATIO', t('ranking.ratio')],
    ['MONEY_RATIO', t('ranking.money')],
  ]

  return (
    <div className="group-screen anim-fade-in">
      <div className="card card-body">
        <div>
          <div className="eyebrow">{t('group.rankingEyebrow')}</div>
          <h2 className="section-title">{t('group.rankingTitle')}</h2>
        </div>

        {/* Metric switcher */}
        <div className="pill-row">
          {rankOptions.map(([value, label]) => (
            <button
              key={value}
              className={`pill-filter-btn${rankBy === value ? ' active' : ''}`}
              onClick={() => setRankBy(value)}
              type="button"
            >
              {label}
            </button>
          ))}
        </div>

        {loading ? (
          <CardSkeleton />
        ) : !leaderboard?.rankings.length ? (
          <EmptyState
            icon={<span style={{ fontSize: 24 }}>🏆</span>}
            title={t('ranking.emptyTitle')}
            subtitle={t('ranking.emptySubtitle')}
          />
        ) : (
          <>
            {/* Podium top 3 */}
            {leaderboard.rankings.length >= 2 && (
              <PodiumDisplay leaderboard={leaderboard} rankBy={rankBy} />
            )}

            {/* Full list */}
            <div className="stack sm">
              {leaderboard.rankings.map((entry) => (
                <MemberRow
                  key={entry.userId}
                  rank={entry.rank}
                  email={entry.email}
                  userId={entry.userId}
                  value={formatMoney(metricForRank(entry.stats, rankBy), leaderboard.currency)}
                  detail={`${t('ranking.porrasCount', { count: entry.stats.totalPorras })} · ${entry.left ? t('common.formerMember') : t('common.active')}`}
                  isFirst={entry.rank === 1}
                />
              ))}
            </div>
          </>
        )}
      </div>
    </div>
  )
}
