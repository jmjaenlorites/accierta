import { X } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { useToasts } from '../../lib/toasts'

export function ToastViewport() {
  const { toasts, dismissToast } = useToasts()
  const { t } = useTranslation()

  return (
    <div className="toast-viewport-wrapper">
      <div className="toast-viewport" aria-live="polite" aria-atomic="true">
        {toasts.map((toast) => (
          <div
            className={`toast ${toast.kind}${toast.dismissing ? ' dismissing' : ''}`}
            key={toast.id}
            role="alert"
          >
            <div className="toast-content">
              <div className="toast-title">{toast.title}</div>
              {toast.message && <div className="toast-message">{toast.message}</div>}
            </div>
            <button
              type="button"
              className="toast-close"
              onClick={() => dismissToast(toast.id)}
              aria-label={t('common.close')}
            >
              <X size={14} />
            </button>
          </div>
        ))}
      </div>
    </div>
  )
}
