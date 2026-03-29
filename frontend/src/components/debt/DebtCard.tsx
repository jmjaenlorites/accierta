import { ArrowDownLeft, ArrowUpRight, CheckCircle, Clock } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { debtStatusLabel, formatMoney, shortEmail } from '../../lib/format'
import { Button } from '../ui/Button'

interface IOwedDebt {
  debtId: string
  porraTitle: string
  toMember: { userId: string; email: string }
  amount: number | string
  currency: string
  status: string
}

interface OwedToMeDebt {
  debtId: string
  porraTitle: string
  fromMember: { userId: string; email: string }
  amount: number | string
  currency: string
  status: string
}

interface DebtCardIOweProps {
  debt: IOwedDebt
  onRequestPayment: () => Promise<void>
}

interface DebtCardOwedToMeProps {
  debt: OwedToMeDebt
  onSettle: () => Promise<void>
}

export function DebtCardIOwe({ debt, onRequestPayment }: DebtCardIOweProps) {
  const { t } = useTranslation()
  const isPaid = debt.status === 'PAID'

  return (
    <div className="card debt-card">
      <div className="row between" style={{ alignItems: 'flex-start' }}>
        <div className="row" style={{ gap: 8, flex: 1, minWidth: 0 }}>
          <ArrowUpRight size={18} color="var(--danger)" style={{ flexShrink: 0, marginTop: 2 }} />
          <div>
            <div className="list-item-title" style={{ color: 'var(--danger)' }}>
              {t('debt.iOwe', { amount: formatMoney(debt.amount, debt.currency) })}
            </div>
            <div className="list-item-sub">
              {shortEmail(debt.toMember.email)} · {debt.porraTitle}
            </div>
          </div>
        </div>
        <div style={{ flexShrink: 0 }}>
          {isPaid ? (
            <span className="muted" style={{ display: 'flex', alignItems: 'center', gap: 4, fontSize: 'var(--text-xs)' }}>
              <CheckCircle size={14} color="var(--win)" /> {debtStatusLabel(debt.status)}
            </span>
          ) : debt.status === 'PAYMENT_REQUESTED' ? (
            <span className="muted" style={{ display: 'flex', alignItems: 'center', gap: 4, fontSize: 'var(--text-xs)' }}>
              <Clock size={14} color="var(--accent)" /> {debtStatusLabel(debt.status)}
            </span>
          ) : (
            <Button variant="secondary" size="sm" onClick={onRequestPayment}>
              {t('debt.requestConfirmation')}
            </Button>
          )}
        </div>
      </div>
    </div>
  )
}

export function DebtCardOwedToMe({ debt, onSettle }: DebtCardOwedToMeProps) {
  const { t } = useTranslation()
  const isPaid = debt.status === 'PAID'

  return (
    <div className="card debt-card">
      <div className="row between" style={{ alignItems: 'flex-start' }}>
        <div className="row" style={{ gap: 8, flex: 1, minWidth: 0 }}>
          <ArrowDownLeft size={18} color="var(--win)" style={{ flexShrink: 0, marginTop: 2 }} />
          <div>
            <div className="list-item-title" style={{ color: 'var(--win)' }}>
              {t('debt.owedToMe', { amount: formatMoney(debt.amount, debt.currency) })}
            </div>
            <div className="list-item-sub">
              {shortEmail(debt.fromMember.email)} · {debt.porraTitle}
            </div>
          </div>
        </div>
        <div style={{ flexShrink: 0 }}>
          {isPaid ? (
            <span className="muted" style={{ display: 'flex', alignItems: 'center', gap: 4, fontSize: 'var(--text-xs)' }}>
              <CheckCircle size={14} color="var(--win)" /> {debtStatusLabel(debt.status)}
            </span>
          ) : (
            <Button size="sm" onClick={onSettle}>
              {t('debt.markPaid')}
            </Button>
          )}
        </div>
      </div>
    </div>
  )
}
