import { Mail } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Link, useNavigate } from 'react-router-dom'
import { authService } from '../../lib/services'
import { useToasts } from '../../lib/toasts'
import { Button } from '../../components/ui/Button'
import { Field } from '../../components/ui/Field'
import { FormShell } from '../../components/ui/FormShell'

export function VerifyEmailPage() {
  const { pushToast } = useToasts()
  const { t } = useTranslation()
  const navigate = useNavigate()

  return (
    <div className="auth-page anim-fade-in">
      <div
        style={{
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          gap: 'var(--space-3)',
          textAlign: 'center',
        }}
      >
        <div
          style={{
            width: 72,
            height: 72,
            borderRadius: 'var(--radius-full)',
            background: 'rgba(108,92,231,0.1)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: 'var(--primary)',
          }}
        >
          <Mail size={32} />
        </div>
        <div>
          <h1 className="page-title">{t('auth.verifyEmail')}</h1>
          <p className="muted" style={{ marginTop: 'var(--space-2)' }}>
            {t('auth.heroSubtitle')}
          </p>
        </div>
      </div>

      <div className="auth-card">
        <FormShell
          onSubmit={async (formData) => {
            await authService.verifyEmail(formData.get('token'))
            pushToast(t('auth.verifiedTitle'), t('auth.verifiedMessage'), 'success')
            navigate('/login')
          }}
        >
          <Field
            label={t('auth.verificationToken')}
            name="token"
            autoComplete="one-time-code"
            required
            placeholder="abc123..."
          />
          <Button type="submit" fullWidth>{t('auth.verifyEmail')}</Button>
        </FormShell>

        <hr style={{ border: 'none', borderTop: '1px solid var(--border)' }} />

        <div>
          <p className="muted" style={{ fontSize: 'var(--text-sm)', marginBottom: 'var(--space-3)' }}>
            ¿No recibiste el código?
          </p>
          <FormShell
            onSubmit={async (formData) => {
              await authService.resendVerification(formData.get('email'))
              pushToast(t('auth.resentTitle'), t('auth.resentMessage'), 'success')
            }}
          >
            <Field label={t('auth.resendTo')} name="email" type="email" required />
            <Button type="submit" variant="ghost" fullWidth>{t('auth.resend')}</Button>
          </FormShell>
        </div>

        <div className="auth-links">
          <p className="auth-link">
            <Link to="/login">{t('auth.login')} →</Link>
          </p>
        </div>
      </div>
    </div>
  )
}
