export type Session = {
  accessToken: string
  refreshToken: string
}

export type TokenPair = {
  accessToken: string
  refreshToken: string
  expiresIn: number
}

export type CurrentUser = {
  sub?: string
  email?: string
}

export type GroupSummary = {
  groupId: string
  name: string
  currency: string
  memberCount: number
  createdAt: string
}

export type GroupMember = {
  userId: string
  email: string
  joinedAt: string
}

export type GroupDetail = {
  groupId: string
  name: string
  currency: string
  members: GroupMember[]
  createdAt: string
}

export type InvitationInfo = {
  groupName: string
  invitedBy: string
  expired: boolean
  used: boolean
}

export type InvitationCreation = {
  invitationToken: string
  invitationLink: string
  expiresAt: string
}

export type PorraStatus = 'OPEN' | 'CLOSED' | 'PENDING_RESOLUTION' | 'RESOLVED' | 'CANCELLED'

export type PorraSummary = {
  porraId: string
  title: string
  type: 'BINARY' | 'NUMERIC' | 'MULTIPLE_CHOICE'
  status: PorraStatus
  stake: number
  bettingDeadline: string
  totalBets: number
}

export type PorraDetail = {
  porraId: string
  groupId: string
  creatorId: string
  title: string
  description: string | null
  type: 'BINARY' | 'NUMERIC' | 'MULTIPLE_CHOICE'
  options: string[]
  tolerance: number | null
  stake: number
  bettingDeadline: string
  resolutionDate: string
  visibility: 'VISIBLE' | 'HIDDEN'
  betModifiable: boolean
  status: PorraStatus
  totalBets?: number
  bets: Array<{ memberId: string; prediction: string; placedAt: string }>
  createdAt: string
}

export type Resolution = {
  resolutionId: string
  proposedResult: string
  proposedBy: { userId: string; email: string }
  waitingEndsAt: string
  status: string
  timeRemaining?: string
}

export type ResolutionVotes = {
  status: string
  votingEndsAt: string | null
  eligibleVoters: number
  votesCast: number
  timeRemaining?: string
  votes: Array<{ voterId: string; votedResult: string }>
}

export type MemberStats = {
  totalPorras: number
  won: number
  lost: number
  cancelled: number
  winRatio: number | string
  currentStreak: number
  totalMoneyWon: number | string
  totalMoneyLost: number | string
  netMoney: number | string
  moneyRatio: number | string
  pendingDebtsOwed: number | string
  pendingDebtsOwing: number | string
  currency: string
}

export type MemberHistory = {
  content: Array<{
    porraId: string
    title: string
    type: string
    myBet: string | null
    result: string | null
    outcome: string
    moneyChange: number | string
    currency: string
    resolvedAt: string
  }>
  page: number
  totalPages: number
}

export type Leaderboard = {
  rankBy: string
  currency: string
  rankings: Array<{
    rank: number
    userId: string
    email: string
    left: boolean
    stats: MemberStats
  }>
}

export type DebtsView = {
  iOwe: Array<{
    debtId: string
    porraId: string
    porraTitle: string
    toMember: { userId: string; email: string }
    amount: number | string
    currency: string
    status: string
    createdAt: string
  }>
  owedToMe: Array<{
    debtId: string
    porraId: string
    porraTitle: string
    fromMember: { userId: string; email: string }
    amount: number | string
    currency: string
    status: string
    createdAt: string
  }>
  consolidated: Array<{
    withMember: { userId: string; email: string }
    netAmount: number | string
    currency: string
    direction: string
  }>
}

export type CanLeaveGroup = {
  canLeave: boolean
  blockingPorras: string[]
}

export type GroupInsights = {
  groupId: string
  openPorras: number
  pendingResolution: number
  pendingDebts: number
}

export type Toast = {
  id: string
  title: string
  message: string
  kind: 'success' | 'error'
  dismissing?: boolean
}
