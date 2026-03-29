import { formatMoney, metricForRank, shortEmail } from '../../lib/format'
import type { Leaderboard } from '../../lib/types'
import { Avatar } from '../ui/Avatar'

interface PodiumDisplayProps {
  leaderboard: Leaderboard
  rankBy: string
}

export function PodiumDisplay({ leaderboard, rankBy }: PodiumDisplayProps) {
  const top3 = leaderboard.rankings.filter((r) => r.rank <= 3)
  const first = top3.find((r) => r.rank === 1)
  const second = top3.find((r) => r.rank === 2)
  const third = top3.find((r) => r.rank === 3)

  if (!first) return null

  return (
    <div className="podium">
      {second && (
        <div className="podium-item second">
          <Avatar email={second.email} userId={second.userId} size="lg" />
          <div className="podium-name">{shortEmail(second.email)}</div>
          <div className="podium-value">
            {formatMoney(metricForRank(second.stats, rankBy), leaderboard.currency)}
          </div>
          <div className="podium-bar second">#2</div>
        </div>
      )}

      <div className="podium-item first">
        <span style={{ fontSize: 24 }}>🏆</span>
        <Avatar email={first.email} userId={first.userId} size="xl" />
        <div className="podium-name" style={{ fontWeight: 800 }}>{shortEmail(first.email)}</div>
        <div className="podium-value">
          {formatMoney(metricForRank(first.stats, rankBy), leaderboard.currency)}
        </div>
        <div className="podium-bar first">#1</div>
      </div>

      {third && (
        <div className="podium-item third">
          <Avatar email={third.email} userId={third.userId} size="lg" />
          <div className="podium-name">{shortEmail(third.email)}</div>
          <div className="podium-value">
            {formatMoney(metricForRank(third.stats, rankBy), leaderboard.currency)}
          </div>
          <div className="podium-bar third">#3</div>
        </div>
      )}
    </div>
  )
}

interface MemberRowProps {
  rank: number
  email: string
  userId: string
  value: string
  detail: string
  isFirst?: boolean
}

export function MemberRow({ rank, email, userId, value, detail, isFirst }: MemberRowProps) {
  return (
    <div className="list-item">
      <span
        style={{
          fontFamily: '"JetBrains Mono", monospace',
          fontWeight: 800,
          fontSize: 'var(--text-sm)',
          color: isFirst ? 'var(--accent)' : 'var(--text-muted)',
          width: 24,
          textAlign: 'center',
          flexShrink: 0,
        }}
      >
        #{rank}
      </span>
      <Avatar email={email} userId={userId} size="sm" />
      <div className="list-item-content">
        <div className="list-item-title">{shortEmail(email)}</div>
        <div className="list-item-sub">{detail}</div>
      </div>
      <div className="list-item-end">
        <span className="mono" style={{ fontSize: 'var(--text-sm)' }}>{value}</span>
      </div>
    </div>
  )
}
