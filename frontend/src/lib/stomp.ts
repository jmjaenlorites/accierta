import { websocketUrl } from './runtimeConfig'

export type StompSubscription = {
  disconnect: () => void
}

type Frame = {
  command: string
  headers: Record<string, string>
  body: string
}

function buildFrame(command: string, headers: Record<string, string>, body = '') {
  const headerLines = Object.entries(headers).map(([key, value]) => `${key}:${value}`)
  return `${command}\n${headerLines.join('\n')}\n\n${body}\0`
}

function parseFrame(raw: string): Frame {
  const [head, ...bodyParts] = raw.split('\n\n')
  const [command, ...headerLines] = head.split('\n')
  const headers = Object.fromEntries(
    headerLines
      .map((line) => {
        const separator = line.indexOf(':')
        if (separator === -1) return null
        return [line.slice(0, separator), line.slice(separator + 1)] as const
      })
      .filter((entry): entry is readonly [string, string] => entry !== null),
  )

  return {
    command,
    headers,
    body: bodyParts.join('\n\n'),
  }
}

export function subscribeToLeaderboard(
  groupId: string,
  accessToken: string | undefined,
  onMessage: () => void,
  onError?: (error: Error) => void,
): StompSubscription {
  const subscriptionId = `sub-${Date.now()}`
  let socket: WebSocket | null = null
  let reconnectTimer: number | null = null
  let reconnectAttempt = 0
  let closedManually = false

  const connect = () => {
    socket = new WebSocket(websocketUrl())
    let buffer = ''

    socket.addEventListener('open', () => {
      socket?.send(
        buildFrame('CONNECT', {
          'accept-version': '1.2',
          'heart-beat': '10000,10000',
          ...(accessToken ? { Authorization: `Bearer ${accessToken}` } : {}),
        }),
      )
    })

    socket.addEventListener('message', (event) => {
      buffer += String(event.data)

      while (buffer.includes('\0')) {
        const delimiterIndex = buffer.indexOf('\0')
        const rawFrame = buffer.slice(0, delimiterIndex)
        buffer = buffer.slice(delimiterIndex + 1)
        if (!rawFrame.trim()) continue

        const frame = parseFrame(rawFrame)

        if (frame.command === 'CONNECTED') {
          reconnectAttempt = 0
          socket?.send(
            buildFrame('SUBSCRIBE', {
              id: subscriptionId,
              destination: `/group/${groupId}/leaderboard`,
            }),
          )
          continue
        }

        if (frame.command === 'MESSAGE') {
          onMessage()
          continue
        }

        if (frame.command === 'ERROR') {
          onError?.(new Error(frame.body || frame.headers.message || 'WebSocket subscription failed'))
        }
      }
    })

    socket.addEventListener('error', () => {
      onError?.(new Error('Leaderboard connection failed'))
    })

    socket.addEventListener('close', () => {
      if (closedManually) return
      reconnectAttempt += 1
      const delay = Math.min(1_000 * 2 ** (reconnectAttempt - 1), 10_000)
      reconnectTimer = window.setTimeout(connect, delay)
    })
  }

  connect()

  return {
    disconnect: () => {
      closedManually = true
      if (reconnectTimer !== null) {
        window.clearTimeout(reconnectTimer)
      }
      if (socket?.readyState === WebSocket.OPEN || socket?.readyState === WebSocket.CONNECTING) {
        socket.close()
      }
    },
  }
}
