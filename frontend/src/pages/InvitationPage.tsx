import { CheckCircle, XCircle } from 'lucide-react'
import { useCallback, useEffect, useMemo } from 'react'
import { useTranslation } from 'react-i18next'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { Header } from '../components/layout/Header'
import { Button } from '../components/ui/Button'
import { CardSkeleton } from '../components/ui/Skeleton'
import { createAuthedServices } from '../lib/services'
import { useAsyncResource } from '../lib/useAsyncResource'
import { useSession } from '../lib/useSession'
import { useToasts } from '../lib/useToasts'
import type { InvitationInfo } from '../lib/types'

export function InvitationPage() {
  const { token = '' } = useParams()
  const navigate = useNavigate()
  const { session, setSession, clearSession } = useSession()
  const { pushToast } = useToasts()
  const { t } = useTranslation()

  const services = useMemo(
    () =>
      createAuthedServices({
        getSession: () => session,
        setSession,
        clearSession,
      }),
    [clearSession, session, setSession],
  )

  const loadInvitation = useCallback(async () => services.invitations.info(token), [services, token])
  const { data: info, error, loading } = useAsyncResource<InvitationInfo>(`invitation:${token}`, loadInvitation)

  useEffect(() => {
    if (!error) return
    pushToast(t('common.error'), error.message, 'error')
  }, [error, pushToast, t])

  const isInvalid = info?.used || info?.expired

  return (
    <>
      <Header title={t('invitation.title')} backTo={session ? '/groups' : '/welcome'} />

      <div className="screen" style={{ paddingTop: 'var(--space-6)' }}>
        {loading ? (
          <CardSkeleton />
        ) : info ? (
          <div className="card card-body anim-fade-in">
            <div
              style={{
                display: 'flex',
                justifyContent: 'center',
                paddingBottom: 'var(--space-2)',
              }}
            >
              {isInvalid ? (
                <XCircle size={48} color="var(--danger)" />
              ) : (
                <CheckCircle size={48} color="var(--win)" />
              )}
            </div>

            <div style={{ textAlign: 'center' }}>
              <div className="eyebrow">{t('invitation.groupEyebrow')}</div>
              <h1 className="page-title">{info.groupName}</h1>
              <p className="muted" style={{ marginTop: 'var(--space-2)' }}>
                {t('invitation.createdBy', { name: info.invitedBy })}
              </p>
            </div>

            <div
              style={{
                padding: 'var(--space-3)',
                borderRadius: 'var(--radius-md)',
                background: isInvalid ? 'rgba(225,112,85,0.08)' : 'rgba(0,184,148,0.08)',
                border: `1px solid ${isInvalid ? 'rgba(225,112,85,0.2)' : 'rgba(0,184,148,0.2)'}`,
                textAlign: 'center',
              }}
            >
              <div className="card-title">
                {info.used ? t('invitation.used') : info.expired ? t('invitation.expired') : t('invitation.ready')}
              </div>
              {!isInvalid && (
                <div className="muted" style={{ marginTop: 4, fontSize: 'var(--text-sm)' }}>
                  {t('invitation.helper')}
                </div>
              )}
            </div>

            {!isInvalid && (
              <>
                {session ? (
                  <Button
                    fullWidth
                    onClick={async () => {
                      const response = await services.invitations.accept(token)
                      pushToast(t('invitation.acceptedTitle'), t('invitation.acceptedMessage'), 'success')
                      navigate(`/groups/${response.groupId}`)
                    }}
                  >
                    {t('invitation.accept')}
                  </Button>
                ) : (
                  <div className="stack">
                    <Link className="btn primary full" to={`/register?token=${token}`}>
                      {t('invitation.createAccount')}
                    </Link>
                    <Link className="btn secondary full" to="/login">
                      {t('invitation.alreadyHaveAccount')}
                    </Link>
                  </div>
                )}
              </>
            )}
          </div>
        ) : null}
      </div>
    </>
  )
}
