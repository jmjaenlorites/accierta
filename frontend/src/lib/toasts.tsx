import { useCallback, useMemo, useState, type ReactNode } from 'react'
import { ToastContext, type ToastContextValue } from './toastContext'
import type { Toast } from './types'

const DISMISS_DELAY = 4000   // ms before auto-dismiss starts
const EXIT_DURATION = 300    // ms for exit animation (matches toast-out)

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
    [toasts, dismissToast],
  )

  return <ToastContext.Provider value={value}>{children}</ToastContext.Provider>
}
