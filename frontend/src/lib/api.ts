import type { Session, TokenPair } from './types'
import { apiUrl } from './runtimeConfig'

export type SessionAccess = {
  getSession: () => Session | null
  setSession: (session: Session) => void
  clearSession: () => void
}

let refreshPromise: Promise<Session> | null = null
let refreshPromiseToken: string | null = null

export async function apiRequest<T>(
  path: string,
  options: RequestInit = {},
  sessionAccess?: SessionAccess,
  retry = true,
): Promise<T> {
  const headers = new Headers(options.headers ?? {})
  const session = sessionAccess?.getSession()

  if (!headers.has('Content-Type') && options.body) {
    headers.set('Content-Type', 'application/json')
  }
  if (session?.accessToken) {
    headers.set('Authorization', `Bearer ${session.accessToken}`)
  }

  const response = await fetch(apiUrl(path), {
    ...options,
    headers,
    cache: 'no-store',
  })

  if (response.status === 401 && retry && sessionAccess && session?.refreshToken && !path.endsWith('/refresh')) {
    try {
      const refreshed = await refreshSession(session.refreshToken)
      sessionAccess.setSession(refreshed)
      return apiRequest<T>(path, options, sessionAccess, false)
    } catch (error) {
      sessionAccess.clearSession()
      throw error
    }
  }

  if (!response.ok) {
    let message = `HTTP ${response.status}`
    try {
      const rawBody = await response.text()
      if (rawBody) {
        try {
          const body = JSON.parse(rawBody) as { message?: string }
          message = body.message ?? rawBody
        } catch {
          message = rawBody
        }
      }
    } catch {
      // Ignore body parsing failures and keep the default status-based message.
    }
    if (response.status === 401 && sessionAccess) {
      sessionAccess.clearSession()
    }
    throw new Error(message)
  }

  if (response.status === 204) {
    return null as T
  }

  const contentType = response.headers.get('content-type') ?? ''
  if (contentType.includes('application/json')) {
    return response.json() as Promise<T>
  }

  return (await response.text()) as T
}

export function parseJwt(token: string | null): Record<string, unknown> | null {
  if (!token) return null
  try {
    const payload = token.split('.')[1]
    if (!payload) return null
    const normalized = payload.replace(/-/g, '+').replace(/_/g, '/')
    const padded = normalized.padEnd(normalized.length + ((4 - (normalized.length % 4)) % 4), '=')
    return JSON.parse(atob(padded))
  } catch {
    return null
  }
}

export function isJwtExpired(token: string | null, skewSeconds = 30): boolean {
  const payload = parseJwt(token)
  const expiresAt = typeof payload?.exp === 'number' ? payload.exp : null
  if (!expiresAt) return false
  return expiresAt <= Math.floor(Date.now() / 1000) + skewSeconds
}

export async function refreshSession(refreshToken: string): Promise<Session> {
  if (refreshPromise && refreshPromiseToken === refreshToken) {
    return refreshPromise
  }

  refreshPromiseToken = refreshToken
  refreshPromise = apiRequest<TokenPair>(
    '/api/auth/refresh',
    {
      method: 'POST',
      body: JSON.stringify({ refreshToken }),
    },
    undefined,
    false,
  )
    .then((refreshed) => ({
      accessToken: refreshed.accessToken,
      refreshToken: refreshed.refreshToken,
    }))
    .finally(() => {
      refreshPromise = null
      refreshPromiseToken = null
    })

  return refreshPromise
}
