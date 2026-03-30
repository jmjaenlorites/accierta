import { useCallback, useEffect, useRef, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { PodiumDisplay, MemberRow } from '../../components/ranking/PodiumDisplay'
import { EmptyState } from '../../components/ui/EmptyState'
import { CardSkeleton } from '../../components/ui/Skeleton'
import { formatMoney, metricForRank } from '../../lib/format'
import { subscribeToLeaderboard } from '../../lib/stomp'
import { useAsyncResource } from '../../lib/useAsyncResource'
import { useSession } from '../../lib/useSession'
import { useToasts } from '../../lib/useToasts'
import type { Leaderboard } from '../../lib/types'
import { useGroupContext } from './useGroupContext'

export function GroupRankingPage() {
  const { group, services } = useGroupContext()
  const { session } = useSession()
  const { pushToast } = useToasts()
  const { t } = useTranslation()
  const [rankBy, setRankBy] = useState('NET_MONEY')
  const subscriptionRef = useRef<{ disconnect: () => void } | null>(null)

  const loadLeaderboard = useCallback(
    async () => services.settlement.leaderboard(group.groupId, rankBy),
    [group.groupId, rankBy, services],
  )
  const { data: leaderboard, error, loading, reload } = useAsyncResource<Leaderboard>(
    `leaderboard:${group.groupId}:${rankBy}`,
    loadLeaderboard,
  )

  useEffect(() => {
    if (!error) return
    pushToast(t('common.error'), error.message, 'error')
  }, [error, pushToast, t])

  useEffect(() => {
    subscriptionRef.current?.disconnect()
    subscriptionRef.current = subscribeToLeaderboard(
      group.groupId,
      session?.accessToken,
      reload,
      (socketError) => pushToast(t('common.error'), socketError.message, 'error'),
    )
    return () => subscriptionRef.current?.disconnect()
  }, [group.groupId, pushToast, reload, session?.accessToken, t])

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
