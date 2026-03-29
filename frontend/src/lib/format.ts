import i18n from '../i18n/i18n'
import type { MemberStats, PorraStatus } from './types'

export function shortEmail(email: string) {
  return email.split('@')[0] ?? email
}

export function avatarColor(seed: string) {
  const hue = seed.split('').reduce((sum, char) => sum + char.charCodeAt(0), 0) % 360
  return `hsl(${hue} 65% 52%)`
}

export function statusLabel(status: string) {
  return i18n.t(`status.${status}`, { defaultValue: status })
}

export function tagClass(status: PorraStatus | string) {
  return (
    {
      OPEN: 'open',
      CLOSED: 'closed',
      PENDING_RESOLUTION: 'pending',
      RESOLVED: 'resolved',
      CANCELLED: 'cancelled',
      WAITING: 'pending',
      VOTING: 'pending',
      ACCEPTED: 'resolved',
      REJECTED: 'loss',
    }[status] ?? 'neutral'
  )
}

export function formatDate(value: string) {
  return new Intl.DateTimeFormat(i18n.language, {
    day: '2-digit',
    month: 'short',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(value))
}

export function formatMoney(value: number | string | undefined, currency: string) {
  const amount = Number(value ?? 0)
  const formatted = new Intl.NumberFormat(i18n.language, {
    minimumFractionDigits: Number.isInteger(amount) ? 0 : 2,
    maximumFractionDigits: 2,
  }).format(amount)
  return `${formatted} ${currency}`
}

export function countdown(value: string) {
  const diff = new Date(value).getTime() - Date.now()
  if (diff <= 0) return i18n.t('status.CLOSED')
  const minutes = Math.floor(diff / 60000)
  const days = Math.floor(minutes / 1440)
  const hours = Math.floor((minutes % 1440) / 60)
  const remainder = minutes % 60
  const parts = [
    days > 0 ? i18n.t('time.daysShort', { count: days }) : null,
    i18n.t('time.hoursShort', { count: hours }),
    i18n.t('time.minutesShort', { count: remainder }),
  ].filter(Boolean)
  return parts.join(' ')
}

export function humanize(value: string) {
  return i18n.t(`porra.type.${value}`, { defaultValue: value })
}

export function metricForRank(stats: MemberStats, rankBy: string) {
  if (rankBy === 'PORRAS_WON') return stats.won
  if (rankBy === 'WIN_RATIO') return stats.winRatio
  if (rankBy === 'MONEY_RATIO') return stats.moneyRatio
  return stats.netMoney
}

export function debtStatusLabel(status: string) {
  return i18n.t(`debt.status.${status}`, { defaultValue: status })
}

export function debtDirectionLabel(direction: string) {
  return i18n.t(`debt.direction.${direction}`, { defaultValue: direction })
}

export function historyOutcomeLabel(outcome: string) {
  return i18n.t(`history.outcome.${outcome}`, { defaultValue: outcome })
}

export function toLocalDateTimeValue(date: Date) {
  const year = date.getFullYear()
  const month = `${date.getMonth() + 1}`.padStart(2, '0')
  const day = `${date.getDate()}`.padStart(2, '0')
  const hours = `${date.getHours()}`.padStart(2, '0')
  const minutes = `${date.getMinutes()}`.padStart(2, '0')
  return `${year}-${month}-${day}T${hours}:${minutes}`
}

export function initialDeadlines() {
  const now = new Date()
  const bettingDeadline = new Date(now.getTime() + 26 * 60 * 60 * 1000)
  const resolutionDate = new Date(now.getTime() + 50 * 60 * 60 * 1000)
  return {
    bettingDeadline: toLocalDateTimeValue(bettingDeadline),
    resolutionDate: toLocalDateTimeValue(resolutionDate),
  }
}
