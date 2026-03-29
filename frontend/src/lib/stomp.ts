import { websocketUrl } from './runtimeConfig'

export type StompSubscription = {
  disconnect: () => void
}

export function subscribeToLeaderboard(
  groupId: string,
  onMessage: (payload: string) => void,
): StompSubscription {
  const socket = new WebSocket(websocketUrl())
  const subscriptionId = `sub-${Date.now()}`

  socket.addEventListener('open', () => {
    socket.send('CONNECT\naccept-version:1.2\nheart-beat:0,0\n\n\0')
    window.setTimeout(() => {
      socket.send(`SUBSCRIBE\nid:${subscriptionId}\ndestination:/group/${groupId}/leaderboard\n\n\0`)
    }, 100)
  })

  socket.addEventListener('message', (event) => {
    const frame = String(event.data).replace(/\0/g, '')
    const [header, ...rest] = frame.split('\n\n')
    if (!header.startsWith('MESSAGE')) return
    onMessage(rest.join('\n\n'))
  })

  return {
    disconnect: () => {
      if (socket.readyState === WebSocket.OPEN || socket.readyState === WebSocket.CONNECTING) {
        socket.close()
      }
    },
  }
}
