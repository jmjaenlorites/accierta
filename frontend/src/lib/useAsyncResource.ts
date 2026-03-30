import { useCallback, useEffect, useMemo, useState } from 'react'

type AsyncResourceState<T> = {
  resolvedKey: string
  data: T | null
  error: Error | null
}

export function useAsyncResource<T>(
  key: string,
  loader: (signal: AbortSignal) => Promise<T>,
) {
  const [reloadCount, setReloadCount] = useState(0)
  const requestKey = useMemo(() => `${key}::${reloadCount}`, [key, reloadCount])
  const [state, setState] = useState<AsyncResourceState<T>>({
    resolvedKey: '',
    data: null,
    error: null,
  })

  useEffect(() => {
    const controller = new AbortController()

    void loader(controller.signal)
      .then((data) => {
        if (controller.signal.aborted) return
        setState({
          resolvedKey: requestKey,
          data,
          error: null,
        })
      })
      .catch((error: unknown) => {
        if (controller.signal.aborted) return
        setState({
          resolvedKey: requestKey,
          data: null,
          error: error instanceof Error ? error : new Error(String(error)),
        })
      })

    return () => controller.abort()
  }, [loader, requestKey])

  const reload = useCallback(() => {
    setReloadCount((current) => current + 1)
  }, [])

  const isCurrent = state.resolvedKey === requestKey

  return {
    data: isCurrent ? state.data : null,
    error: isCurrent ? state.error : null,
    loading: !isCurrent,
    reload,
  }
}
