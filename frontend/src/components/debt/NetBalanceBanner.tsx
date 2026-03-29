import { formatMoney } from '../../lib/format'
import type { DebtsView } from '../../lib/types'

interface NetBalanceBannerProps {
  debts: DebtsView
}

export function NetBalanceBanner({ debts }: NetBalanceBannerProps) {
  const currency = debts.iOwe[0]?.currency ?? debts.owedToMe[0]?.currency ?? ''

  const totalOwe = debts.iOwe
    .filter((d) => d.status !== 'PAID')
    .reduce((sum, d) => sum + Number(d.amount), 0)

  const totalOwed = debts.owedToMe
    .filter((d) => d.status !== 'PAID')
    .reduce((sum, d) => sum + Number(d.amount), 0)

  const net = totalOwed - totalOwe
  const isPositive = net >= 0

  return (
    <div className={`balance-banner ${isPositive ? 'positive' : 'negative'}`}>
      <div className="eyebrow">{isPositive ? 'Balance neto' : 'Balance neto'}</div>
      <div className={`balance-amount ${isPositive ? 'positive' : 'negative'}`}>
        {isPositive ? '+' : ''}{formatMoney(net, currency)}
      </div>
      <div className="muted" style={{ fontSize: 'var(--text-xs)' }}>
        {isPositive
          ? `Te deben ${formatMoney(totalOwed, currency)} · Debes ${formatMoney(totalOwe, currency)}`
          : `Debes ${formatMoney(totalOwe, currency)} · Te deben ${formatMoney(totalOwed, currency)}`}
      </div>
    </div>
  )
}
