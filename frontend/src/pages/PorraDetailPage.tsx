import { Trash2 } from 'lucide-react'
import { useCallback, useEffect, useMemo, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { useNavigate, useParams } from 'react-router-dom'
import { BetForm } from '../components/porra/BetForm'
import { ResolutionPanel } from '../components/porra/ResolutionPanel'
import { Header } from '../components/layout/Header'
import { Button } from '../components/ui/Button'
import { ConfirmDialog } from '../components/ui/ConfirmDialog'
import { CountdownTimer } from '../components/ui/CountdownTimer'
import { EmptyState } from '../components/ui/EmptyState'
import { MetricCard } from '../components/ui/MetricCard'
import { Pill } from '../components/ui/Pill'
import { CardSkeleton } from '../components/ui/Skeleton'
import { Avatar } from '../components/ui/Avatar'
import { formatDate, formatMoney, humanize, statusLabel, tagClass } from '../lib/format'
import { createAuthedServices } from '../lib/services'
import { useAsyncResource } from '../lib/useAsyncResource'
import { useSession } from '../lib/useSession'
import { useToasts } from '../lib/useToasts'

export function PorraDetailPage() {
  const { porraId = '' } = useParams()
  const navigate = useNavigate()
  const { session, setSession, clearSession } = useSession()
  const { pushToast } = useToasts()
  const { t } = useTranslation()
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false)

  const services = useMemo(
    () =>
      createAuthedServices({
        getSession: () => session,
        setSession,
        clearSession,
      }),
    [clearSession, session, setSession],
  )

  const loadPorra = useCallback(async () => {
    const detail = await services.porras.get(porraId)
    if (detail.status === 'OPEN' || detail.status === 'CLOSED') {
      return { porra: detail, resolution: null, votes: null }
    }

    const [resolutionResult, votesResult] = await Promise.allSettled([
      services.resolution.get(porraId),
      services.resolution.votes(porraId),
    ])

    return {
      porra: detail,
      resolution: resolutionResult.status === 'fulfilled' ? resolutionResult.value : null,
      votes: votesResult.status === 'fulfilled' ? votesResult.value : null,
    }
  }, [porraId, services])

  const { data, error, loading, reload } = useAsyncResource(
    `porra:${porraId}`,
    loadPorra,
  )

  useEffect(() => {
    if (!error) return
    pushToast(t('common.error'), error.message, 'error')
  }, [error, pushToast, t])

  const porra = data?.porra ?? null
  const resolution = data?.resolution ?? null
  const votes = data?.votes ?? null

  const groupLink = porra ? `/groups/${porra.groupId}` : '/groups'

  return (
    <>
      <Header
        title={porra?.title ?? t('common.loading')}
        backTo={porra ? `${groupLink}/porras` : '/groups'}
      />

      <div className="screen" style={{ paddingTop: 'var(--space-4)' }}>
        {loading || !porra ? (
          <>
            <CardSkeleton />
            <CardSkeleton />
          </>
        ) : (
          <>
            {/* Info card */}
            <div className="card card-body anim-fade-in">
              <div className="row between" style={{ alignItems: 'flex-start' }}>
                <div>
                  <div className="eyebrow">{t('porra.detailEyebrow')}</div>
                  <h1 className="page-title" style={{ fontSize: 'var(--text-xl)', marginTop: 2 }}>
                    {porra.title}
                  </h1>
                  {porra.description && (
                    <p className="muted" style={{ marginTop: 'var(--space-2)' }}>{porra.description}</p>
                  )}
                </div>
                <Pill tone={tagClass(porra.status)}>{statusLabel(porra.status)}</Pill>
              </div>

              <div className="grid-2">
                <MetricCard label={t('porra.stake')} value={formatMoney(porra.stake, 'pts')} />
                <MetricCard
                  label={t('porra.betsCount')}
                  value={
                    porra.visibility === 'HIDDEN' && porra.status === 'OPEN'
                      ? t('porra.hiddenBetsCount')
                      : String(porra.totalBets ?? porra.bets.length)
                  }
                />
                <MetricCard label={t('porra.deadline')} value="" />
                <MetricCard label={t('porra.resolutionDateShort')} value={formatDate(porra.resolutionDate)} />
              </div>
              {/* Overlay countdown on 3rd metric slot */}
              <div style={{ marginTop: -40, marginLeft: 0, paddingLeft: 'var(--space-3)' }}>
                <CountdownTimer deadline={porra.bettingDeadline} />
              </div>

              <div className="row" style={{ gap: 8, flexWrap: 'wrap' }}>
                <Pill tone="neutral">{humanize(porra.type)}</Pill>
                {porra.visibility === 'HIDDEN' && <Pill tone="neutral">{t('porra.visibilityOptions.HIDDEN')}</Pill>}
              </div>
            </div>

            {/* Bet form for OPEN porras */}
            {porra.status === 'OPEN' && (
              <div className="card card-body anim-slide-in">
                <div>
                  <div className="eyebrow">{t('porra.betEyebrow')}</div>
                  <h2 className="section-title">{t('porra.betTitle')}</h2>
                </div>
                <BetForm
                  porra={porra}
                  onBet={async (prediction) => {
                    await services.porras.placeBet(porra.porraId, prediction)
                    pushToast(t('porra.betPlacedTitle'), t('porra.betPlacedMessage'), 'success')
                    reload()
                  }}
                />
              </div>
            )}

            {/* Resolution panel for PENDING_RESOLUTION */}
            {porra.status === 'PENDING_RESOLUTION' && (
              <div className="card card-body anim-slide-in">
                <ResolutionPanel
                  porra={porra}
                  resolution={resolution}
                  votes={votes}
                  onPropose={async (result) => {
                    await services.resolution.propose(porra.porraId, result)
                    pushToast(t('porra.resolutionProposedTitle'), t('porra.resolutionProposedMessage'), 'success')
                    reload()
                  }}
                  onDispute={async (reason) => {
                    await services.resolution.dispute(porra.porraId, reason)
                    pushToast(t('porra.disputedTitle'), t('porra.disputedMessage'), 'success')
                    reload()
                  }}
                  onVote={async (result) => {
                    await services.resolution.vote(porra.porraId, result)
                    pushToast(t('porra.votedTitle'), t('porra.votedMessage'), 'success')
                    reload()
                  }}
                  onCancelPorra={async (reason) => {
                    await services.resolution.proposeCancellation(porra.porraId, reason)
                    pushToast(t('porra.cancellationProposedTitle'), t('porra.cancellationProposedMessage'), 'success')
                    reload()
                  }}
                />
              </div>
            )}

            {/* Bets list */}
            <div className="card card-body">
              <div>
                <div className="eyebrow">{t('porra.betsEyebrow')}</div>
                <h2 className="section-title">{t('porra.betsTitle')}</h2>
              </div>
              {porra.visibility === 'HIDDEN' && porra.status === 'OPEN' ? (
                <EmptyState
                  icon={<span style={{ fontSize: 24 }}>🔒</span>}
                  title={t('porra.noVisibleBetsTitle')}
                  subtitle={
                    porra.totalBets !== undefined
                      ? t('porra.hiddenBetsInfo', { placed: porra.totalBets, total: porra.totalBets })
                      : t('porra.noVisibleBetsSubtitle')
                  }
                />
              ) : porra.bets.length ? (
                <div className="stack sm">
                  {porra.bets.map((bet) => (
                    <div className="list-item" key={`${bet.memberId}-${bet.placedAt}`}>
                      <Avatar email={bet.memberId} userId={bet.memberId} size="sm" />
                      <div className="list-item-content">
                        <div className="list-item-title mono" style={{ fontWeight: 700 }}>
                          {bet.prediction}
                        </div>
                        <div className="list-item-sub">{bet.memberId}</div>
                      </div>
                      <div className="list-item-end">
                        <span className="muted" style={{ fontSize: 'var(--text-xs)' }}>{formatDate(bet.placedAt)}</span>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <EmptyState title={t('porra.noVisibleBetsTitle')} subtitle={t('porra.noVisibleBetsSubtitle')} />
              )}
            </div>

            {/* Creator management */}
            <div className="card card-body">
              <div>
                <div className="eyebrow">{t('porra.manageEyebrow')}</div>
                <h2 className="section-title">{t('porra.manageTitle')}</h2>
              </div>
              <p className="muted" style={{ fontSize: 'var(--text-sm)' }}>{t('porra.deleteHint')}</p>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 'var(--space-3)' }}>
                <Button
                  variant="danger"
                  onClick={() => setShowDeleteConfirm(true)}
                >
                  <Trash2 size={14} /> {t('porra.deleteButton')}
                </Button>
                <Button variant="ghost" onClick={() => navigate(`${groupLink}/porras`)}>
                  {t('porra.backToGroup')}
                </Button>
              </div>
            </div>
          </>
        )}
      </div>

      {showDeleteConfirm && (
        <ConfirmDialog
          title={t('porra.deleteButton')}
          message={t('porra.deleteHint')}
          confirmLabel={t('porra.deleteButton')}
          onConfirm={async () => {
            await services.porras.delete(porra!.porraId)
            pushToast(t('porra.deletedTitle'), t('porra.deletedMessage'), 'success')
            navigate(`${groupLink}/porras`)
          }}
          onCancel={() => setShowDeleteConfirm(false)}
        />
      )}
    </>
  )
}
