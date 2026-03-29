import { Bell, Plus } from 'lucide-react'
import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { GroupCard } from '../components/group/GroupCard'
import { CreateGroupSheet } from '../components/group/CreateGroupSheet'
import { Header } from '../components/layout/Header'
import { NotificationPanel } from '../components/layout/NotificationPanel'
import { CardSkeleton } from '../components/ui/Skeleton'
import { EmptyState } from '../components/ui/EmptyState'
import { createAuthedServices } from '../lib/services'
import { useSession } from '../lib/session'
import { useToasts } from '../lib/toasts'
import type { GroupInsights, GroupSummary } from '../lib/types'

export function GroupsPage() {
  const { session, setSession, clearSession } = useSession()
  const { pushToast } = useToasts()
  const { t } = useTranslation()
  const [groups, setGroups] = useState<GroupSummary[]>([])
  const [insights, setInsights] = useState<Record<string, GroupInsights>>({})
  const [loading, setLoading] = useState(true)
  const [showCreate, setShowCreate] = useState(false)
  const [showNotifications, setShowNotifications] = useState(false)

  const services = createAuthedServices({
    getSession: () => session,
    setSession,
    clearSession,
  })

  async function load() {
    setLoading(true)
    const nextGroups = await services.groups.list()
    setGroups(nextGroups)
    const nextInsights = Object.fromEntries(
      await Promise.all(
        nextGroups.map(async (group) => [group.groupId, await services.groups.insights(group.groupId)] as const),
      ),
    )
    setInsights(nextInsights)
    setLoading(false)
  }

  useEffect(() => {
    void load()
  }, [])

  return (
    <>
      <Header
        title={t('groupsPage.title')}
        rightSlot={
          <button
            className="icon-btn"
            type="button"
            aria-label={t('common.notifications')}
            onClick={() => setShowNotifications(true)}
          >
            <Bell size={20} />
          </button>
        }
      />

      <div className="screen" style={{ paddingTop: 'var(--space-4)' }}>
        {loading ? (
          <div className="stack">
            <CardSkeleton />
            <CardSkeleton />
          </div>
        ) : groups.length ? (
          <div className="stack anim-fade-in stagger">
            {groups.map((group) => (
              <GroupCard key={group.groupId} group={group} insights={insights[group.groupId]} />
            ))}
          </div>
        ) : (
          <div className="card card-body anim-fade-in">
            <EmptyState
              icon={<span style={{ fontSize: 24 }}>🎲</span>}
              title={t('groupsPage.emptyTitle')}
              subtitle={t('groupsPage.emptySubtitle')}
              action={
                <button className="btn primary" type="button" onClick={() => setShowCreate(true)}>
                  {t('groupsPage.createButton')}
                </button>
              }
            />
          </div>
        )}
      </div>

      {/* FAB */}
      <button
        className="fab"
        type="button"
        onClick={() => setShowCreate(true)}
        aria-label={t('groupsPage.createTitle')}
      >
        <Plus size={24} />
      </button>

      {/* Notification panel */}
      {showNotifications && (
        <NotificationPanel onClose={() => setShowNotifications(false)} />
      )}

      {/* Create group sheet */}
      {showCreate && (
        <CreateGroupSheet
          onClose={() => setShowCreate(false)}
          onCreate={async (payload) => {
            await services.groups.create(payload)
            pushToast(t('groupsPage.createdTitle'), t('groupsPage.createdMessage'), 'success')
            await load()
          }}
        />
      )}
    </>
  )
}
