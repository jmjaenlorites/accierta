import { Plus } from 'lucide-react'
import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { CreatePorraSheet } from '../../components/porra/CreatePorraSheet'
import { PorraCard } from '../../components/porra/PorraCard'
import { EmptyState } from '../../components/ui/EmptyState'
import { CardSkeleton } from '../../components/ui/Skeleton'
import { useToasts } from '../../lib/toasts'
import type { PorraSummary } from '../../lib/types'
import { useGroupContext } from './GroupLayout'

type Filter = '' | 'OPEN' | 'PENDING_RESOLUTION' | 'RESOLVED'

export function GroupPorrasPage() {
  const { group, services } = useGroupContext()
  const { pushToast } = useToasts()
  const { t } = useTranslation()
  const [porras, setPorras] = useState<PorraSummary[]>([])
  const [filter, setFilter] = useState<Filter>('')
  const [loading, setLoading] = useState(true)
  const [showCreate, setShowCreate] = useState(false)

  async function load() {
    setLoading(true)
    const data = await services.porras.list(group.groupId, filter)
    setPorras(data)
    setLoading(false)
  }

  useEffect(() => {
    void load()
  }, [filter, group.groupId])

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
            await load()
          }}
        />
      )}
    </>
  )
}
