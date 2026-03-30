import { createContext } from 'react'
import type { Toast } from './types'

export type ToastContextValue = {
  toasts: Toast[]
  pushToast: (title: string, message: string, kind: Toast['kind']) => void
  dismissToast: (id: string) => void
}

export const ToastContext = createContext<ToastContextValue | null>(null)
