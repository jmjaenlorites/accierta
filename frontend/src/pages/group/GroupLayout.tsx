import { Bell } from 'lucide-react'
import { useEffect, useMemo, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Outlet, useOutletContext, useParams } from 'react-router-dom'
import { Header } from '../../components/layout/Header'
import { NotificationPanel } from '../../components/layout/NotificationPanel'
import { Skeleton } from '../../components/ui/Skeleton'
import { createAuthedServices } from '../../lib/services'
import { useSession } from '../../lib/session'
import type { GroupDetail } from '../../lib/types'

type GroupContext = {
  group: GroupDetail
  services: ReturnType<typeof createAuthedServices>
  refreshGroup: () => Promise<void>
}

export function GroupLayout() {
  const { groupId = '' } = useParams()
  const { session, setSession, clearSession, setCurrentGroupId } = useSession()
  const [group, setGroup] = useState<GroupDetail | null>(null)
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

  const refreshGroup = async () => {
    const nextGroup = await services.groups.get(groupId)
    setGroup(nextGroup)
    setCurrentGroupId(groupId)
  }

  useEffect(() => {
    void refreshGroup()
  }, [groupId])

  return (
    <>
      {showNotifications && (
        <NotificationPanel onClose={() => setShowNotifications(false)} />
      )}
      <Header
        title={group?.name ?? ''}
        backTo="/groups"
        rightSlot={
          !group ? null : (
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

      {!group ? (
        <div className="screen" style={{ paddingTop: 'var(--space-4)' }}>
          <div className="card card-body">
            <Skeleton variant="text" size="lg" width="60%" />
            <Skeleton variant="text" width="40%" />
          </div>
        </div>
      ) : (
        <Outlet context={{ group, services, refreshGroup } satisfies GroupContext} />
      )}
    </>
  )
}

export function useGroupContext() {
  return useOutletContext<GroupContext>()
}
