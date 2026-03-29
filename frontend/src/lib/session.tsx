import { createContext, useContext, useMemo, useState, type ReactNode } from 'react'
import { parseJwt } from './api'
import type { CurrentUser, Session } from './types'

type SessionContextValue = {
  session: Session | null
  user: CurrentUser | null
  currentGroupId: string
  setCurrentGroupId: (groupId: string) => void
  setSession: (session: Session) => void
  clearSession: () => void
}

const SessionContext = createContext<SessionContextValue | null>(null)

export function SessionProvider({ children }: { children: ReactNode }) {
  const [session, setSessionState] = useState<Session | null>(() => {
    const accessToken = localStorage.getItem('accierta.frontend.accessToken')
    const refreshToken = localStorage.getItem('accierta.frontend.refreshToken')
    return accessToken && refreshToken ? { accessToken, refreshToken } : null
  })
  const [currentGroupId, setCurrentGroupIdState] = useState(
    () => localStorage.getItem('accierta.frontend.currentGroupId') ?? '',
  )

  const value = useMemo<SessionContextValue>(() => {
    const user = parseJwt(session?.accessToken ?? null) as CurrentUser | null
    return {
      session,
      user,
      currentGroupId,
      setCurrentGroupId: (groupId: string) => {
        localStorage.setItem('accierta.frontend.currentGroupId', groupId)
        setCurrentGroupIdState(groupId)
      },
      setSession: (nextSession: Session) => {
        localStorage.setItem('accierta.frontend.accessToken', nextSession.accessToken)
        localStorage.setItem('accierta.frontend.refreshToken', nextSession.refreshToken)
        setSessionState(nextSession)
      },
      clearSession: () => {
        localStorage.removeItem('accierta.frontend.accessToken')
        localStorage.removeItem('accierta.frontend.refreshToken')
        localStorage.removeItem('accierta.frontend.currentGroupId')
        setSessionState(null)
        setCurrentGroupIdState('')
      },
    }
  }, [currentGroupId, session])

  return <SessionContext.Provider value={value}>{children}</SessionContext.Provider>
}

export function useSession() {
  const context = useContext(SessionContext)
  if (!context) {
    throw new Error('useSession must be used within SessionProvider')
  }
  return context
}
