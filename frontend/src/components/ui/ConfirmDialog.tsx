import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Button } from './Button'

interface ConfirmDialogProps {
  title: string
  message: string
  confirmLabel?: string
  cancelLabel?: string
  variant?: 'danger' | 'primary'
  onConfirm: () => void | Promise<void>
  onCancel: () => void
}

export function ConfirmDialog({
  title,
  message,
  confirmLabel,
  cancelLabel,
  variant = 'danger',
  onConfirm,
  onCancel,
}: ConfirmDialogProps) {
  const { t } = useTranslation()
  const [exiting, setExiting] = useState(false)

  function dismiss(callback: () => void) {
    setExiting(true)
    window.setTimeout(callback, 150)
  }

  return (
    <div
      className={`confirm-dialog-backdrop${exiting ? ' exiting' : ''}`}
      onClick={() => dismiss(onCancel)}
    >
      <div
        className={`confirm-dialog${exiting ? ' exiting' : ''}`}
        onClick={(e) => e.stopPropagation()}
        role="dialog"
        aria-modal="true"
        aria-labelledby="confirm-title"
      >
        <p className="confirm-dialog-title" id="confirm-title">{title}</p>
        <p className="confirm-dialog-message">{message}</p>
        <div className="confirm-dialog-actions">
          <Button variant="ghost" onClick={() => dismiss(onCancel)} fullWidth>
            {cancelLabel ?? t('common.cancel')}
          </Button>
          <Button
            variant={variant}
            onClick={async () => {
              await onConfirm()
            }}
            fullWidth
          >
            {confirmLabel ?? title}
          </Button>
        </div>
      </div>
    </div>
  )
}
