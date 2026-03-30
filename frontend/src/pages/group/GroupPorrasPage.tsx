import { Plus } from 'lucide-react'
import { useCallback, useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { CreatePorraSheet } from '../../components/porra/CreatePorraSheet'
import { PorraCard } from '../../components/porra/PorraCard'
import { EmptyState } from '../../components/ui/EmptyState'
import { CardSkeleton } from '../../components/ui/Skeleton'
import { useAsyncResource } from '../../lib/useAsyncResource'
import { useToasts } from '../../lib/useToasts'
import type { PorraSummary } from '../../lib/types'
import { useGroupContext } from './useGroupContext'

type Filter = '' | 'OPEN' | 'PENDING_RESOLUTION' | 'RESOLVED'

export function GroupPorrasPage() {
  const { group, services } = useGroupContext()
  const { pushToast } = useToasts()
  const { t } = useTranslation()
  const [filter, setFilter] = useState<Filter>('')
  const [showCreate, setShowCreate] = useState(false)

  const loadPorras = useCallback(
    async () => services.porras.list(group.groupId, filter),
    [filter, group.groupId, services],
  )
  const { data: porrasData, error, loading, reload } = useAsyncResource<PorraSummary[]>(
    `group-porras:${group.groupId}:${filter}`,
    loadPorras,
  )

  useEffect(() => {
    if (!error) return
    pushToast(t('common.error'), error.message, 'error')
  }, [error, pushToast, t])

  const porras = porrasData ?? []

  const filterOptions: Array<[Filter, string]> = [
    ['', t('group.all')],
    ['OPEN', t('group.open')],
    ['PENDING_RESOLUTION', t('group.resolution')],
    ['RESOLVED', t('status.RESOLVED')],
  ]

  return (
    <>
      <div className="group-screen anim-fade-in">
        {/* Filter pills */}
        <div className="pill-row" style={{ marginBottom: 'var(--space-2)' }}>
          {filterOptions.map(([value, label]) => (
            <button
              key={label}
              className={`pill-filter-btn${filter === value ? ' active' : ''}`}
              onClick={() => setFilter(value)}
              type="button"
            >
              {label}
            </button>
          ))}
        </div>

        {/* List */}
        {loading ? (
          <div className="stack">
            <CardSkeleton />
            <CardSkeleton />
            <CardSkeleton />
          </div>
        ) : porras.length ? (
          <div className="stack anim-fade-in stagger">
            {porras.map((porra) => (
              <PorraCard key={porra.porraId} porra={porra} currency={group.currency} />
            ))}
          </div>
        ) : (
          <div className="card card-body">
            <EmptyState
              icon={<span style={{ fontSize: 24 }}>🎲</span>}
              title={t('group.noPorrasTitle')}
              subtitle={t('group.noPorrasSubtitle')}
              action={
                <button className="btn primary" type="button" onClick={() => setShowCreate(true)}>
                  {t('group.newPorraButton')}
                </button>
              }
            />
          </div>
        )}
      </div>

      {/* FAB */}
      <button
        className="fab"
        type="button"
        onClick={() => setShowCreate(true)}
        aria-label={t('group.newPorraButton')}
      >
        <Plus size={24} />
      </button>

      {showCreate && (
        <CreatePorraSheet
          currency={group.currency}
          onClose={() => setShowCreate(false)}
          onCreate={async (payload) => {
            await services.porras.create(group.groupId, payload)
            pushToast(t('porra.createdTitle'), t('porra.createdMessage'), 'success')
            reload()
          }}
        />
      )}
    </>
  )
}
