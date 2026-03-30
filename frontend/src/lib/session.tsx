import { useEffect, useMemo, useState, type ReactNode } from 'react'
import { isJwtExpired, parseJwt, refreshSession } from './api'
import { SessionContext, type SessionContextValue } from './sessionContext'
import type { Session } from './types'

function readStoredSession(): Session | null {
  const accessToken = localStorage.getItem('accierta.frontend.accessToken')
  const refreshToken = localStorage.getItem('accierta.frontend.refreshToken')
  return accessToken && refreshToken ? { accessToken, refreshToken } : null
}

export function SessionProvider({ children }: { children: ReactNode }) {
  const [session, setSessionState] = useState<Session | null>(readStoredSession)
  const [bootstrapResolved, setBootstrapResolved] = useState(() => {
    const storedSession = readStoredSession()
    return !storedSession || !isJwtExpired(storedSession.accessToken)
  })

  useEffect(() => {
    if (!session || !isJwtExpired(session.accessToken)) {
      return
    }

    let active = true

    void refreshSession(session.refreshToken)
      .then((nextSession) => {
        if (!active) return
        localStorage.setItem('accierta.frontend.accessToken', nextSession.accessToken)
        localStorage.setItem('accierta.frontend.refreshToken', nextSession.refreshToken)
        setSessionState(nextSession)
      })
      .catch(() => {
        if (!active) return
        localStorage.removeItem('accierta.frontend.accessToken')
        localStorage.removeItem('accierta.frontend.refreshToken')
        setSessionState(null)
      })
      .finally(() => {
        if (active) setBootstrapResolved(true)
      })

    return () => {
      active = false
    }
  }, [session])

  const ready = bootstrapResolved

  const value = useMemo<SessionContextValue>(() => {
    const user = parseJwt(session?.accessToken ?? null)
    return {
      session,
      user,
      ready,
      setSession: (nextSession: Session) => {
        localStorage.setItem('accierta.frontend.accessToken', nextSession.accessToken)
        localStorage.setItem('accierta.frontend.refreshToken', nextSession.refreshToken)
        setBootstrapResolved(true)
        setSessionState(nextSession)
      },
      clearSession: () => {
        localStorage.removeItem('accierta.frontend.accessToken')
        localStorage.removeItem('accierta.frontend.refreshToken')
        setBootstrapResolved(true)
        setSessionState(null)
      },
    }
  }, [ready, session])

  return <SessionContext.Provider value={value}>{children}</SessionContext.Provider>
}
