import { Bell } from 'lucide-react'
import { useCallback, useEffect, useMemo, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Outlet, useParams } from 'react-router-dom'
import { Header } from '../../components/layout/Header'
import { NotificationPanel } from '../../components/layout/NotificationPanel'
import { Skeleton } from '../../components/ui/Skeleton'
import { createAuthedServices } from '../../lib/services'
import { useAsyncResource } from '../../lib/useAsyncResource'
import { useSession } from '../../lib/useSession'
import { useToasts } from '../../lib/useToasts'
import type { GroupDetail } from '../../lib/types'
import type { GroupContext } from './useGroupContext'

export function GroupLayout() {
  const { groupId = '' } = useParams()
  const { session, setSession, clearSession } = useSession()
  const { pushToast } = useToasts()
  const [showNotifications, setShowNotifications] = useState(false)
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

  const loadGroup = useCallback(async () => services.groups.get(groupId), [groupId, services])
  const { data: group, error, loading, reload } = useAsyncResource<GroupDetail>(
    `group:${groupId}`,
    loadGroup,
  )

  useEffect(() => {
    if (!error) return
    pushToast(t('common.error'), error.message, 'error')
  }, [error, pushToast, t])

  useEffect(() => {
    if (!group?.groupId) return
    localStorage.setItem('accierta.frontend.lastGroupId', group.groupId)
  }, [group?.groupId])

  return (
    <>
      {showNotifications && (
        <NotificationPanel onClose={() => setShowNotifications(false)} />
      )}
      <Header
        title={group?.name ?? ''}
        backTo="/groups"
        rightSlot={
          loading || !group ? null : (
            <button
              className="icon-btn"
              type="button"
              aria-label={t('common.notifications')}
              onClick={() => setShowNotifications(true)}
            >
              <Bell size={20} />
            </button>
          )
        }
      />

      {loading || !group ? (
        <div className="screen" style={{ paddingTop: 'var(--space-4)' }}>
          <div className="card card-body">
            <Skeleton variant="text" size="lg" width="60%" />
            <Skeleton variant="text" width="40%" />
          </div>
        </div>
      ) : (
        <Outlet context={{ group, services, refreshGroup: reload } satisfies GroupContext} />
      )}
    </>
  )
}
