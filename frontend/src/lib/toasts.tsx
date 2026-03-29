import { createContext, useCallback, useContext, useMemo, useState, type ReactNode } from 'react'
import type { Toast } from './types'

const DISMISS_DELAY = 4000   // ms before auto-dismiss starts
const EXIT_DURATION = 300    // ms for exit animation (matches toast-out)

type ToastContextValue = {
  toasts: Toast[]
  pushToast: (title: string, message: string, kind: Toast['kind']) => void
  dismissToast: (id: string) => void
}

const ToastContext = createContext<ToastContextValue | null>(null)

export function ToastProvider({ children }: { children: ReactNode }) {
  const [toasts, setToasts] = useState<Toast[]>([])

  const dismissToast = useCallback((id: string) => {
    // Mark as dismissing to trigger exit animation
    setToasts((current) =>
      current.map((toast) => (toast.id === id ? { ...toast, dismissing: true } : toast)),
    )
    // Remove after exit animation completes
    window.setTimeout(() => {
      setToasts((current) => current.filter((toast) => toast.id !== id))
    }, EXIT_DURATION)
  }, [])

  const value = useMemo<ToastContextValue>(
    () => ({
      toasts,
      pushToast: (title, message, kind) => {
        const id = `${Date.now()}-${Math.random()}`
        setToasts((current) => [...current.slice(-3), { id, title, message, kind }])
        window.setTimeout(() => {
          dismissToast(id)
        }, DISMISS_DELAY)
      },
      dismissToast,
    }),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [toasts, dismissToast],
  )

  return <ToastContext.Provider value={value}>{children}</ToastContext.Provider>
}

export function useToasts() {
  const context = useContext(ToastContext)
  if (!context) {
    throw new Error('useToasts must be used within ToastProvider')
  }
  return context
}
