import { ArrowLeft } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { authService } from '../../lib/services'
import { useToasts } from '../../lib/useToasts'
import { Button } from '../../components/ui/Button'
import { Field } from '../../components/ui/Field'
import { FormShell } from '../../components/ui/FormShell'

export function ResetPasswordPage() {
  const { pushToast } = useToasts()
  const { t } = useTranslation()
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token') ?? ''
  const mode = token ? 'reset' : 'request'

  return (
    <div className="auth-page anim-fade-in">
      <div style={{ width: '100%' }}>
        <button className="auth-back-btn" onClick={() => navigate('/login')} type="button">
          <ArrowLeft size={16} /> {t('common.back')}
        </button>
      </div>

      <div className="auth-card">
        {mode === 'request' ? (
          <>
            <div>
              <div className="eyebrow">{t('auth.requestResetEyebrow')}</div>
              <h1 className="page-title">{t('auth.requestResetTitle')}</h1>
              <p className="muted" style={{ marginTop: 'var(--space-2)', fontSize: 'var(--text-sm)' }}>
                {t('auth.requestResetHelp')}
              </p>
            </div>
            <FormShell
              onSubmit={async (formData) => {
                await authService.requestReset(formData.get('email'))
                pushToast(t('auth.resetRequestedTitle'), t('auth.resetRequestedMessage'), 'success')
              }}
            >
              <Field label={t('auth.email')} name="email" type="email" required />
              <Button type="submit" fullWidth>{t('auth.requestReset')}</Button>
            </FormShell>
          </>
        ) : (
          <>
            <div>
              <div className="eyebrow">{t('auth.resetEyebrow')}</div>
              <h1 className="page-title">{t('auth.resetTitle')}</h1>
            </div>
            <FormShell
              onSubmit={async (formData) => {
                await authService.resetPassword(formData.get('token') ?? token, formData.get('newPassword'))
                pushToast(t('auth.passwordUpdatedTitle'), t('auth.passwordUpdatedMessage'), 'success')
                navigate('/login')
              }}
            >
              {!token && (
                <Field label={t('auth.verificationToken')} name="token" autoComplete="one-time-code" required />
              )}
              <Field
                label={t('auth.newPassword')}
                name="newPassword"
                type="password"
                autoComplete="new-password"
                required
                hint="Mínimo 8 caracteres"
              />
              <Button type="submit" fullWidth>{t('auth.updatePassword')}</Button>
            </FormShell>
          </>
        )}

        <div className="auth-links">
          <p className="auth-link">
            <Link to="/login">{t('auth.login')} →</Link>
          </p>
        </div>
      </div>
    </div>
  )
}
