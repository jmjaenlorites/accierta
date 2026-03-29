import { useTranslation } from 'react-i18next'
import { Link, Navigate } from 'react-router-dom'
import { useSession } from '../../lib/session'

export function WelcomePage() {
  const { session } = useSession()
  const { t } = useTranslation()

  if (session) return <Navigate to="/groups" replace />

  return (
    <div
      style={{
        minHeight: '100dvh',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        padding: 'var(--space-8) var(--space-6)',
        maxWidth: 400,
        margin: '0 auto',
        gap: 'var(--space-8)',
      }}
    >
      {/* Brand */}
      <div
        className="anim-scale-in"
        style={{
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          gap: 'var(--space-4)',
          textAlign: 'center',
        }}
      >
        <div className="brand-mark xl" aria-label="Accierta">A</div>
        <div>
          <h1
            style={{
              fontFamily: '"Plus Jakarta Sans", sans-serif',
              fontSize: 'var(--text-3xl)',
              fontWeight: 800,
              margin: 0,
              letterSpacing: '-0.02em',
            }}
          >
            Accierta
          </h1>
          <p style={{ color: 'var(--text-secondary)', marginTop: 'var(--space-2)', fontSize: 'var(--text-lg)' }}>
            {t('auth.heroTitle')}
          </p>
          <p style={{ color: 'var(--text-muted)', marginTop: 'var(--space-2)', fontSize: 'var(--text-sm)' }}>
            {t('auth.heroSubtitle')}
          </p>
        </div>
      </div>

      {/* Actions */}
      <div
        className="anim-fade-in"
        style={{
          width: '100%',
          display: 'grid',
          gap: 'var(--space-3)',
        }}
      >
        <Link className="btn primary full" to="/login">
          {t('auth.login')}
        </Link>
        <Link className="btn secondary full" to="/register">
          {t('auth.register')}
        </Link>
        <Link
          to="/reset-password"
          style={{
            textAlign: 'center',
            fontSize: 'var(--text-sm)',
            color: 'var(--text-secondary)',
            marginTop: 'var(--space-2)',
          }}
        >
          {t('auth.requestResetEyebrow')} →
        </Link>
      </div>
    </div>
  )
}
