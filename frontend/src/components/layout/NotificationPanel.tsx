import { Bell, X } from 'lucide-react'
import { useEffect } from 'react'
import { useTranslation } from 'react-i18next'

interface NotificationPanelProps {
  onClose: () => void
}

export function NotificationPanel({ onClose }: NotificationPanelProps) {
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
    return () => {
      document.body.style.overflow = ''
    }
  }, [])

  return (
    <>
      {/* Backdrop */}
      <div
        style={{
          position: 'fixed',
          inset: 0,
          background: 'rgba(0, 0, 0, 0.4)',
          zIndex: 20,
          animation: 'fade-in 200ms ease both',
        }}
        onClick={onClose}
        aria-hidden="true"
      />

      {/* Panel — slides in from the right */}
      <div
        role="dialog"
        aria-modal="true"
        aria-label={t('common.notifications')}
        style={{
          position: 'fixed',
          top: 0,
          right: 0,
          bottom: 0,
          width: 'min(100%, 360px)',
          background: 'var(--surface)',
          zIndex: 21,
          display: 'flex',
          flexDirection: 'column',
          boxShadow: '-4px 0 24px rgba(0, 0, 0, 0.12)',
          animation: 'slide-in-right 250ms ease-out both',
        }}
      >
        {/* Header */}
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            padding: 'var(--space-4)',
            paddingTop: 'calc(var(--space-4) + env(safe-area-inset-top, 0px))',
            borderBottom: '1px solid var(--border)',
            flexShrink: 0,
          }}
        >
          <h2
            style={{
              fontFamily: '"Plus Jakarta Sans", sans-serif',
              fontSize: 'var(--text-xl)',
              fontWeight: 700,
              margin: 0,
            }}
          >
            {t('common.notifications')}
          </h2>
          <button
            type="button"
            className="icon-btn"
            onClick={onClose}
            aria-label={t('common.close')}
          >
            <X size={20} />
          </button>
        </div>

        {/* Body — empty state for now */}
        <div
          style={{
            flex: 1,
            overflowY: 'auto',
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center',
            padding: 'var(--space-6) var(--space-4)',
            paddingBottom: 'calc(var(--space-6) + env(safe-area-inset-bottom, 0px))',
            gap: 'var(--space-4)',
            textAlign: 'center',
          }}
        >
          <div
            style={{
              width: 64,
              height: 64,
              borderRadius: '50%',
              background: 'var(--surface-hover)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: 'var(--text-muted)',
            }}
          >
            <Bell size={28} />
          </div>
          <div>
            <p
              style={{
                fontFamily: '"Plus Jakarta Sans", sans-serif',
                fontWeight: 700,
                fontSize: 'var(--text-base)',
                margin: 0,
              }}
            >
              {t('common.notifications')}
            </p>
            <p
              className="muted"
              style={{ marginTop: 'var(--space-2)', fontSize: 'var(--text-sm)' }}
            >
              {t('common.comingSoon')}
            </p>
          </div>
        </div>
      </div>
    </>
  )
}
