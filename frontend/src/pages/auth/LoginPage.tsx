import { ArrowLeft } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { authService } from '../../lib/services'
import { useSession } from '../../lib/session'
import { useToasts } from '../../lib/toasts'
import { Button } from '../../components/ui/Button'
import { Field } from '../../components/ui/Field'
import { FormShell } from '../../components/ui/FormShell'

export function LoginPage() {
  const { session, setSession } = useSession()
  const { pushToast } = useToasts()
  const { t } = useTranslation()
  const navigate = useNavigate()

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
          <div className="eyebrow">{t('auth.accessEyebrow')}</div>
          <h1 className="page-title">{t('auth.accessTitle')}</h1>
        </div>

        <FormShell
          onSubmit={async (formData) => {
            const tokens = await authService.login(formData.get('email'), formData.get('password'))
            setSession({ accessToken: tokens.accessToken, refreshToken: tokens.refreshToken })
            pushToast(t('auth.loggedInTitle'), t('auth.loggedInMessage'), 'success')
            navigate('/groups', { replace: true })
          }}
        >
          <Field label={t('auth.email')} name="email" type="email" required autoComplete="email" />
          <Field label={t('auth.password')} name="password" type="password" required />
          <Button type="submit" fullWidth>{t('auth.login')}</Button>
        </FormShell>

        <div className="auth-links">
          <p className="auth-link">
            {t('auth.registerEyebrow')}{' '}
            <Link to="/register">{t('auth.register')} →</Link>
          </p>
          <p className="auth-link">
            <Link to="/reset-password">{t('auth.requestResetEyebrow')} →</Link>
          </p>
          <p className="auth-link">
            <Link to="/verify">{t('auth.verifyEmail')} →</Link>
          </p>
        </div>
      </div>
    </div>
  )
}
