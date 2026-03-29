import { apiRequest, type SessionAccess } from './api'
import type {
  CanLeaveGroup,
  DebtsView,
  GroupDetail,
  GroupInsights,
  GroupSummary,
  InvitationCreation,
  InvitationInfo,
  Leaderboard,
  MemberHistory,
  MemberStats,
  PorraDetail,
  PorraSummary,
  Resolution,
  ResolutionVotes,
  TokenPair,
} from './types'

export const authService = {
  login: (email: FormDataEntryValue | null, password: FormDataEntryValue | null) =>
    apiRequest<TokenPair>('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    }),
  register: (payload: { email: FormDataEntryValue | null; password: FormDataEntryValue | null; invitationToken?: string | null }) =>
    apiRequest('/api/auth/register', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),
  verifyEmail: (token: FormDataEntryValue | null) =>
    apiRequest('/api/auth/verify-email', {
      method: 'POST',
      body: JSON.stringify({ token }),
    }),
  resendVerification: (email: FormDataEntryValue | null) =>
    apiRequest('/api/auth/resend-verification', {
      method: 'POST',
      body: JSON.stringify({ email }),
    }),
  requestReset: (email: FormDataEntryValue | null) =>
    apiRequest('/api/auth/request-password-reset', {
      method: 'POST',
      body: JSON.stringify({ email }),
    }),
  resetPassword: (token: FormDataEntryValue | null, newPassword: FormDataEntryValue | null) =>
    apiRequest('/api/auth/reset-password', {
      method: 'POST',
      body: JSON.stringify({ token, newPassword }),
    }),
  logout: (refreshToken: string) =>
    apiRequest('/api/auth/logout', {
      method: 'POST',
      body: JSON.stringify({ refreshToken }),
    }),
}

export function createAuthedServices(sessionAccess: SessionAccess) {
  const request = <T,>(path: string, options?: RequestInit) => apiRequest<T>(path, options, sessionAccess)

  return {
    groups: {
      list: () => request<GroupSummary[]>('/api/groups'),
      get: (groupId: string) => request<GroupDetail>(`/api/groups/${groupId}`),
      create: (payload: { name: FormDataEntryValue | null; currency: FormDataEntryValue | null }) =>
        request(`/api/groups`, {
          method: 'POST',
          body: JSON.stringify(payload),
        }),
      canLeave: (groupId: string) => request<CanLeaveGroup>(`/api/groups/${groupId}/can-leave`),
      leave: (groupId: string) =>
        request(`/api/groups/${groupId}/leave`, {
          method: 'POST',
        }),
      createInvitation: (groupId: string, email?: FormDataEntryValue | null) =>
        request<InvitationCreation>(`/api/groups/${groupId}/invitations`, {
          method: 'POST',
          body: JSON.stringify(email ? { email } : {}),
        }),
      insights: async (groupId: string): Promise<GroupInsights> => {
        const [porras, debts] = await Promise.all([
          request<PorraSummary[]>(`/api/groups/${groupId}/porras`),
          request<DebtsView>(`/api/groups/${groupId}/debts/mine`),
        ])
        return {
          groupId,
          openPorras: porras.filter((item) => item.status === 'OPEN').length,
          pendingResolution: porras.filter((item) => item.status === 'PENDING_RESOLUTION').length,
          pendingDebts:
            debts.iOwe.filter((item) => item.status !== 'PAID').length +
            debts.owedToMe.filter((item) => item.status !== 'PAID').length,
        }
      },
    },
    invitations: {
      info: (token: string) => apiRequest<InvitationInfo>(`/api/invitations/${token}`),
      accept: (token: string) =>
        request<{ groupId: string; message: string }>(`/api/invitations/${token}/accept`, {
          method: 'POST',
        }),
    },
    porras: {
      list: (groupId: string, status = '') =>
        request<PorraSummary[]>(`/api/groups/${groupId}/porras${status ? `?status=${status}` : ''}`),
      get: (porraId: string) => request<PorraDetail>(`/api/porras/${porraId}`),
      create: (groupId: string, payload: unknown) =>
        request(`/api/groups/${groupId}/porras`, {
          method: 'POST',
          body: JSON.stringify(payload),
        }),
      placeBet: (porraId: string, prediction: unknown) =>
        request(`/api/porras/${porraId}/bets`, {
          method: 'POST',
          body: JSON.stringify({ prediction }),
        }),
      delete: (porraId: string) =>
        request(`/api/porras/${porraId}`, {
          method: 'DELETE',
        }),
    },
    resolution: {
      get: (porraId: string) => request<Resolution>(`/api/porras/${porraId}/resolution`),
      votes: (porraId: string) => request<ResolutionVotes>(`/api/porras/${porraId}/resolution/votes`),
      propose: (porraId: string, proposedResult: string) =>
        request(`/api/porras/${porraId}/resolution`, {
          method: 'POST',
          body: JSON.stringify({ proposedResult }),
        }),
      dispute: (porraId: string, reason: string) =>
        request(`/api/porras/${porraId}/resolution/dispute`, {
          method: 'POST',
          body: JSON.stringify({ reason }),
        }),
      vote: (porraId: string, votedResult: string) =>
        request(`/api/porras/${porraId}/resolution/vote`, {
          method: 'POST',
          body: JSON.stringify({ votedResult }),
        }),
      proposeCancellation: (porraId: string, reason: string) =>
        request(`/api/porras/${porraId}/cancellation`, {
          method: 'POST',
          body: JSON.stringify({ reason }),
        }),
    },
    settlement: {
      leaderboard: (groupId: string, rankBy: string) =>
        request<Leaderboard>(`/api/groups/${groupId}/leaderboard?rankBy=${rankBy}`),
      stats: (groupId: string, userId: string) => request<MemberStats>(`/api/groups/${groupId}/members/${userId}/stats`),
      history: (groupId: string, userId: string) =>
        request<MemberHistory>(`/api/groups/${groupId}/members/${userId}/history`),
      debts: (groupId: string) => request<DebtsView>(`/api/groups/${groupId}/debts/mine`),
      requestPayment: (debtId: string) =>
        request(`/api/debts/${debtId}/request-payment`, {
          method: 'POST',
        }),
      settle: (debtId: string) =>
        request(`/api/debts/${debtId}/settle`, {
          method: 'POST',
        }),
    },
  }
}
