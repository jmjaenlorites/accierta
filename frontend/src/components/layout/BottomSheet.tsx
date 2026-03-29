import { X } from 'lucide-react'
import type { ReactNode } from 'react'
import { useEffect } from 'react'
import { useTranslation } from 'react-i18next'

interface BottomSheetProps {
  title?: string
  subtitle?: string
  onClose: () => void
  children: ReactNode
}

export function BottomSheet({ title, subtitle, onClose, children }: BottomSheetProps) {
  const { t } = useTranslation()

  // Close on Escape
  useEffect(() => {
    const handler = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose()
    }
    document.addEventListener('keydown', handler)
    return () => document.removeEventListener('keydown', handler)
  }, [onClose])

  // Prevent body scroll
  useEffect(() => {
    document.body.style.overflow = 'hidden'
    return () => { document.body.style.overflow = '' }
  }, [])

  return (
    <>
      <div
        className="bottom-sheet-backdrop"
        onClick={onClose}
        aria-hidden="true"
      />
      <div className="bottom-sheet-wrapper">
        <div
          className="bottom-sheet"
          role="dialog"
          aria-modal="true"
          aria-label={title}
        >
        <div className="bottom-sheet-handle">
          <div className="bottom-sheet-handle-bar" />
        </div>
        {title && (
          <div className="bottom-sheet-header">
            <div style={{ minWidth: 0 }}>
              <h2 className="bottom-sheet-title">{title}</h2>
              {subtitle && (
                <p style={{ fontSize: 'var(--text-sm)', color: 'var(--text-secondary)', marginTop: 2 }}>
                  {subtitle}
                </p>
              )}
            </div>
            <button
              className="icon-btn"
              onClick={onClose}
              aria-label={t('common.close')}
              type="button"
              style={{ zIndex: 1, flexShrink: 0 }}
            >
              <X size={20} />
            </button>
          </div>
        )}
        <div className="bottom-sheet-body">{children}</div>
        </div>
      </div>
    </>
  )
}
