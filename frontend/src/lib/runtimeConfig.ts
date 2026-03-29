const apiBaseUrl = normalizeHttpBaseUrl(import.meta.env.VITE_API_BASE_URL)
const wsBaseUrl = normalizeSocketUrl(import.meta.env.VITE_WS_BASE_URL)

function normalizeHttpBaseUrl(value: string | undefined): string | null {
  if (!value) return null
  const trimmed = value.trim()
  if (!trimmed) return null
  return trimmed.endsWith('/') ? trimmed.slice(0, -1) : trimmed
}

function normalizeSocketUrl(value: string | undefined): string | null {
  if (!value) return null
  const trimmed = value.trim()
  return trimmed || null
}

export function apiUrl(path: string): string {
  if (!apiBaseUrl) return path
  if (/^https?:\/\//.test(path)) return path
  const normalizedPath = path.startsWith('/') ? path : `/${path}`
  return `${apiBaseUrl}${normalizedPath}`
}

export function websocketUrl(): string {
  if (wsBaseUrl) return wsBaseUrl
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  return `${protocol}//${window.location.host}/ws`
}
