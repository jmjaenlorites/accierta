import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { DebtCardIOwe, DebtCardOwedToMe } from '../../components/debt/DebtCard'
import { NetBalanceBanner } from '../../components/debt/NetBalanceBanner'
import { EmptyState } from '../../components/ui/EmptyState'
import { CardSkeleton } from '../../components/ui/Skeleton'
import { debtDirectionLabel, formatMoney, shortEmail } from '../../lib/format'
import { useToasts } from '../../lib/toasts'
import type { DebtsView } from '../../lib/types'
import { useGroupContext } from './GroupLayout'

type DebtTab = 'iOwe' | 'owedToMe' | 'consolidated'

export function GroupDebtsPage() {
  const { group, services } = useGroupContext()
  const { pushToast } = useToasts()
  const { t } = useTranslation()
  const [debts, setDebts] = useState<DebtsView | null>(null)
  const [loading, setLoading] = useState(true)
  const [tab, setTab] = useState<DebtTab>('iOwe')

  async function load() {
    setLoading(true)
    const data = await services.settlement.debts(group.groupId)
    setDebts(data)
    setLoading(false)
  }

  useEffect(() => {
    void load()
  }, [group.groupId])

  return (
    <div className="group-screen anim-fade-in">
      {loading ? (
        <CardSkeleton />
      ) : !debts ? null : (
        <>
          {/* Net balance banner */}
          <NetBalanceBanner debts={debts} />

          {/* Tab switcher */}
          <div className="pill-row">
            {([
              ['iOwe', t('group.settlementEyebrow')],
              ['owedToMe', t('group.owedToMeTab')],
              ['consolidated', t('group.consolidatedTitle')],
            ] as Array<[DebtTab, string]>).map(([value, label]) => (
              <button
                key={value}
                className={`pill-filter-btn${tab === value ? ' active' : ''}`}
                onClick={() => setTab(value)}
                type="button"
              >
                {label}
              </button>
            ))}
          </div>

          {/* I owe tab */}
          {tab === 'iOwe' && (
            <div className="stack sm">
              {debts.iOwe.length ? (
                debts.iOwe.map((debt) => (
                  <DebtCardIOwe
                    key={debt.debtId}
                    debt={debt}
                    onRequestPayment={async () => {
                      await services.settlement.requestPayment(debt.debtId)
                      pushToast(t('debt.requestTitle'), t('debt.requestMessage'), 'success')
                      await load()
                    }}
                  />
                ))
              ) : (
                <div className="card card-body">
                  <EmptyState
                    icon={<span style={{ fontSize: 24 }}>✅</span>}
                    title={t('debt.emptyTitle')}
                    subtitle={t('debt.emptySubtitle')}
                  />
                </div>
              )}
            </div>
          )}

          {/* Owed to me tab */}
          {tab === 'owedToMe' && (
            <div className="stack sm">
              {debts.owedToMe.length ? (
                debts.owedToMe.map((debt) => (
                  <DebtCardOwedToMe
                    key={debt.debtId}
                    debt={debt}
                    onSettle={async () => {
                      await services.settlement.settle(debt.debtId)
                      pushToast(t('debt.paidTitle'), t('debt.paidMessage'), 'success')
                      await load()
                    }}
                  />
                ))
              ) : (
                <div className="card card-body">
                  <EmptyState
                    icon={<span style={{ fontSize: 24 }}>💰</span>}
                    title={t('debt.emptyTitle')}
                    subtitle={t('debt.emptySubtitle')}
                  />
                </div>
              )}
            </div>
          )}

          {/* Consolidated tab */}
          {tab === 'consolidated' && (
            <div className="card card-body">
              <div>
                <div className="eyebrow">{t('group.consolidatedEyebrow')}</div>
                <h2 className="section-title">{t('group.consolidatedTitle')}</h2>
              </div>
              {debts.consolidated.length ? (
                <div className="stack sm">
                  {debts.consolidated.map((entry) => (
                    <div className="list-item" key={entry.withMember.userId}>
                      <div className="list-item-content">
                        <div className="list-item-title">
                          {debtDirectionLabel(entry.direction)} · {shortEmail(entry.withMember.email)}
                        </div>
                        <div className="list-item-sub">{t('debt.consolidatedHint')}</div>
                      </div>
                      <div className="list-item-end">
                        <span className="mono" style={{ fontSize: 'var(--text-sm)' }}>
                          {formatMoney(entry.netAmount, entry.currency)}
                        </span>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <EmptyState
                  icon={<span style={{ fontSize: 24 }}>✅</span>}
                  title={t('debt.emptyTitle')}
                  subtitle={t('debt.emptySubtitle')}
                />
              )}
            </div>
          )}
        </>
      )}
    </div>
  )
}
