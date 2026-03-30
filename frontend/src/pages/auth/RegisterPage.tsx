import { ArrowLeft } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Link, Navigate, useNavigate, useSearchParams } from 'react-router-dom'
import { authService } from '../../lib/services'
import { useSession } from '../../lib/useSession'
import { useToasts } from '../../lib/useToasts'
import { Button } from '../../components/ui/Button'
import { Field } from '../../components/ui/Field'
import { FormShell } from '../../components/ui/FormShell'

export function RegisterPage() {
  const { session, ready } = useSession()
  const { pushToast } = useToasts()
  const { t } = useTranslation()
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const invitationToken = searchParams.get('token') ?? ''

  if (!ready) return null
  if (session) return <Navigate to="/groups" replace />

  return (
    <div className="auth-page anim-fade-in">
      <div style={{ width: '100%' }}>
        <button className="auth-back-btn" onClick={() => navigate('/welcome')} type="button">
          <ArrowLeft size={16} /> {t('common.back')}
        </button>
      </div>

      <div className="auth-card">
        <div>
          <div className="eyebrow">{t('auth.registerEyebrow')}</div>
          <h1 className="page-title">{t('auth.registerTitle')}</h1>
          {invitationToken && (
            <div
              style={{
                marginTop: 'var(--space-3)',
                padding: 'var(--space-3)',
                borderRadius: 'var(--radius-md)',
                background: 'rgba(253,203,110,0.15)',
                border: '1px solid rgba(253,203,110,0.4)',
                fontSize: 'var(--text-sm)',
                color: 'var(--text-primary)',
              }}
            >
              🎉 {t('invitation.title')} — {t('invitation.ready')}
            </div>
          )}
        </div>

        <FormShell
          onSubmit={async (formData) => {
            await authService.register({
              email: formData.get('email'),
              password: formData.get('password'),
              invitationToken: invitationToken || null,
            })
            pushToast(t('auth.registeredTitle'), t('auth.registeredMessage'), 'success')
            navigate('/verify')
          }}
        >
          <Field label={t('auth.email')} name="email" type="email" required autoComplete="email" />
          <Field
            label={t('auth.password')}
            name="password"
            type="password"
            required
            hint="Mínimo 8 caracteres"
            autoComplete="new-password"
          />
          <Button type="submit" fullWidth>{t('auth.register')}</Button>
        </FormShell>

        <div className="auth-links">
          <p className="auth-link">
            {t('auth.loginTab')}{' '}
            <Link to="/login">{t('auth.login')} →</Link>
          </p>
        </div>
      </div>
    </div>
  )
}
