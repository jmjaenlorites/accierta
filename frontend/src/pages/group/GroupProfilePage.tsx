import { Copy, Link, LogOut, UserPlus } from 'lucide-react'
import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { useNavigate } from 'react-router-dom'
import { Avatar } from '../../components/ui/Avatar'
import { Button } from '../../components/ui/Button'
import { ConfirmDialog } from '../../components/ui/ConfirmDialog'
import { EmptyState } from '../../components/ui/EmptyState'
import { Field } from '../../components/ui/Field'
import { FormShell } from '../../components/ui/FormShell'
import { MetricCard } from '../../components/ui/MetricCard'
import { CardSkeleton } from '../../components/ui/Skeleton'
import { formatMoney, shortEmail } from '../../lib/format'
import { authService } from '../../lib/services'
import { useSession } from '../../lib/session'
import { useToasts } from '../../lib/toasts'
import type { CanLeaveGroup, InvitationCreation, MemberStats } from '../../lib/types'
import { useGroupContext } from './GroupLayout'

export function GroupProfilePage() {
  const { group, services } = useGroupContext()
  const { session, user, clearSession } = useSession()
  const { pushToast } = useToasts()
  const navigate = useNavigate()
  const { t, i18n } = useTranslation()

  const [stats, setStats] = useState<MemberStats | null>(null)
  const [canLeave, setCanLeave] = useState<CanLeaveGroup | null>(null)
  const [latestInvitation, setLatestInvitation] = useState<InvitationCreation | null>(null)
  const [showLeaveConfirm, setShowLeaveConfirm] = useState(false)
  const [copied, setCopied] = useState(false)
  const [loadingStats, setLoadingStats] = useState(true)

  useEffect(() => {
    if (!user?.sub) return
    setLoadingStats(true)
    void Promise.all([
      services.settlement.stats(group.groupId, user.sub).then(setStats).catch(() => null),
      services.groups.canLeave(group.groupId).then(setCanLeave).catch(() => null),
    ]).then(() => setLoadingStats(false))
  }, [group.groupId, user?.sub])

  async function handleLeave() {
    await services.groups.leave(group.groupId)
    pushToast(t('group.leftTitle'), t('group.leftMessage'), 'success')
    navigate('/groups')
  }

  async function handleLogout() {
    if (session?.refreshToken) {
      try { await authService.logout(session.refreshToken) } catch { /* ignore */ }
    }
    clearSession()
    navigate('/welcome')
  }

  async function copyLink() {
    if (!latestInvitation?.invitationLink) return
    await navigator.clipboard.writeText(latestInvitation.invitationLink)
    setCopied(true)
    setTimeout(() => setCopied(false), 2000)
  }

  return (
    <div className="group-screen anim-fade-in">
      {/* User card */}
      <div className="card card-body">
        <div className="row">
          <Avatar email={user?.email ?? 'anon@example.com'} userId={user?.sub ?? 'guest'} size="lg" />
          <div>
            <div className="card-title">{shortEmail(user?.email ?? t('common.anonymous'))}</div>
            <div className="muted">{user?.email}</div>
          </div>
        </div>

        {/* Language */}
        <div className="field">
          <label htmlFor="lang">{t('profilePage.language')}</label>
          <select
            className="select"
            id="lang"
            value={i18n.language}
            onChange={(e) => void i18n.changeLanguage(e.target.value)}
          >
            <option value="es-ES">Español</option>
            <option value="en-US">English</option>
          </select>
        </div>
      </div>

      {/* Stats */}
      {loadingStats ? (
        <CardSkeleton />
      ) : stats ? (
        <div className="card card-body">
          <div>
            <div className="eyebrow">{t('profilePage.statsEyebrow')}</div>
            <h2 className="section-title">{t('profilePage.statsTitle')}</h2>
          </div>
          <div className="grid-2">
            <MetricCard label={t('profilePage.netMoney')} value={formatMoney(stats.netMoney, stats.currency)} />
            <MetricCard label={t('profilePage.won')} value={String(stats.won)} />
            <MetricCard label={t('profilePage.ratio')} value={String(stats.winRatio)} />
            <MetricCard label={t('profilePage.pending')} value={formatMoney(stats.pendingDebtsOwing, stats.currency)} />
          </div>
        </div>
      ) : null}

      {/* Invite people */}
      <div className="card card-body">
        <div>
          <div className="eyebrow">{t('group.invitesEyebrow')}</div>
          <h2 className="section-title">{t('group.invitesTitle')}</h2>
        </div>
        <FormShell
          onSubmit={async (formData) => {
            const inv = await services.groups.createInvitation(group.groupId, formData.get('email'))
            setLatestInvitation(inv)
            pushToast(t('group.inviteCreatedTitle'), t('group.inviteCreatedMessage'), 'success')
          }}
        >
          <Field label={t('group.inviteEmailOptional')} name="email" type="email" placeholder="amiga@example.com" />
          <Button type="submit" fullWidth>
            <UserPlus size={16} /> {t('group.inviteButton')}
          </Button>
        </FormShell>

        {latestInvitation && (
          <div className="invite-card">
            <div className="eyebrow">{t('group.latestInvite')}</div>
            <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
              <Link size={14} color="var(--primary)" />
              <span style={{ fontSize: 'var(--text-sm)', flex: 1 }}>{latestInvitation.invitationLink}</span>
            </div>
            <Button variant="secondary" fullWidth onClick={copyLink}>
              <Copy size={14} /> {copied ? '¡Copiado!' : 'Copiar enlace'}
            </Button>
          </div>
        )}
      </div>

      {/* Members */}
      <div className="card card-body">
        <div>
          <div className="eyebrow">{t('common.members')}</div>
          <h2 className="section-title">{t('group.insideTitle')}</h2>
        </div>
        <div className="stack sm">
          {group.members.length ? (
            group.members.map((member) => (
              <div className="list-item" key={member.userId}>
                <Avatar email={member.email} userId={member.userId} size="sm" />
                <div className="list-item-content">
                  <div className="list-item-title">{shortEmail(member.email)}</div>
                  <div className="list-item-sub">{member.email}</div>
                </div>
                {member.userId === user?.sub && (
                  <span className="pill primary" style={{ fontSize: 10 }}>tú</span>
                )}
              </div>
            ))
          ) : (
            <EmptyState title={t('group.noMembersTitle')} subtitle={t('group.noMembersSubtitle')} />
          )}
        </div>
      </div>

      {/* Leave group */}
      <div className="card card-body">
        <div>
          <div className="eyebrow">{t('group.leaveEyebrow')}</div>
          <h2 className="section-title">{t('group.leaveTitle')}</h2>
          {canLeave && !canLeave.canLeave ? (
            <p className="muted" style={{ marginTop: 'var(--space-2)', fontSize: 'var(--text-sm)' }}>
              {t('group.cannotLeave', { blocking: canLeave.blockingPorras.join(', ') || t('common.pending') })}
            </p>
          ) : (
            <p className="muted" style={{ marginTop: 'var(--space-2)', fontSize: 'var(--text-sm)' }}>
              {t('group.leaveHint')}
            </p>
          )}
        </div>
        <Button
          variant="danger"
          disabled={Boolean(canLeave && !canLeave.canLeave)}
          onClick={() => setShowLeaveConfirm(true)}
        >
          {t('group.leaveButton')}
        </Button>
      </div>

      {/* Session */}
      <div className="card card-body">
        <div>
          <div className="eyebrow">{t('profilePage.sessionEyebrow')}</div>
          <h2 className="section-title">{t('profilePage.sessionTitle')}</h2>
          <p className="muted" style={{ marginTop: 'var(--space-2)', fontSize: 'var(--text-sm)' }}>
            {t('profilePage.sessionHint')}
          </p>
        </div>
        <Button variant="danger" onClick={handleLogout}>
          <LogOut size={14} /> {t('common.logout')}
        </Button>
      </div>

      {showLeaveConfirm && (
        <ConfirmDialog
          title={t('group.leaveButton')}
          message={t('group.leaveHint')}
          confirmLabel={t('group.leaveButton')}
          onConfirm={handleLeave}
          onCancel={() => setShowLeaveConfirm(false)}
        />
      )}
    </div>
  )
}
