import { createContext } from 'react'
import type { CurrentUser, Session } from './types'

export type SessionContextValue = {
  session: Session | null
  user: CurrentUser | null
  ready: boolean
  setSession: (session: Session) => void
  clearSession: () => void
}

export const SessionContext = createContext<SessionContextValue | null>(null)
