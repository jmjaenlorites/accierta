import type { ReactNode } from 'react'
import { useTranslation } from 'react-i18next'
import { useToasts } from '../../lib/toasts'

interface FormShellProps {
  onSubmit: (formData: FormData) => Promise<void>
  children: ReactNode
  className?: string
}

export function FormShell({ onSubmit, children, className = 'stack' }: FormShellProps) {
  const { pushToast } = useToasts()
  const { t } = useTranslation()

  return (
    <form
      className={className}
      onSubmit={(event) => {
        event.preventDefault()
        const formData = new FormData(event.currentTarget)
        void onSubmit(formData).catch((error: unknown) => {
          pushToast(t('common.error'), error instanceof Error ? error.message : String(error), 'error')
        })
      }}
    >
      {children}
    </form>
  )
}
