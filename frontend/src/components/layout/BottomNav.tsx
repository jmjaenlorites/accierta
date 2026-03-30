import { Coins, Dices, Home, Trophy, User } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { NavLink, useLocation } from 'react-router-dom'

export function BottomNav() {
  const location = useLocation()
  const { t } = useTranslation()

  // Hide only on auth and invite pages
  const hide =
    location.pathname.startsWith('/landing') ||
    location.pathname.startsWith('/privacy') ||
    location.pathname.startsWith('/terms') ||
    location.pathname.startsWith('/welcome') ||
    location.pathname.startsWith('/login') ||
    location.pathname.startsWith('/register') ||
    location.pathname.startsWith('/verify') ||
    location.pathname.startsWith('/reset-password') ||
    location.pathname.startsWith('/invite/')

  if (hide) return null

  // Detect if we are inside a group and extract the groupId
  const groupMatch = location.pathname.match(/^\/groups\/([^/]+)/)
  const storedGroupId = localStorage.getItem('accierta.frontend.lastGroupId')
  const groupId = groupMatch
    ? groupMatch[1]
    : location.pathname.startsWith('/porras/')
      ? storedGroupId
      : null
  const groupBase = groupId ? `/groups/${groupId}` : null

  // "Inicio" tab:
  //   - When inside a group → link to /groups/:id (group home)
  //   - When on groups list → link to /groups
  // Active detection for "Inicio":
  //   - Active on /groups (exact) OR on /groups/:id (exact — group home, not sub-pages)
  const inicioTo = groupBase ?? '/groups'
  const isInicioActive = location.pathname === '/groups' || (groupBase !== null && location.pathname === groupBase)

  const secondaryItems = [
    { to: groupBase ? `${groupBase}/porras` : '/groups', label: t('nav.porras'), icon: Dices },
    { to: groupBase ? `${groupBase}/ranking` : '/groups', label: t('nav.ranking'), icon: Trophy },
    { to: groupBase ? `${groupBase}/debts` : '/groups', label: t('nav.debts'), icon: Coins },
    { to: groupBase ? `${groupBase}/profile` : '/groups', label: t('nav.profile'), icon: User },
  ]

  return (
    <nav className="bottom-nav" aria-label="Navigation principale">
      {/* Inicio tab — manual active detection to handle both /groups and /groups/:id */}
      <NavLink
        to={inicioTo}
        end
        className={() => `nav-item${isInicioActive ? ' active' : ''}`}
      >
        <Home size={20} aria-hidden="true" />
        <span>{t('nav.home')}</span>
      </NavLink>

      {secondaryItems.map((item) => {
        const Icon = item.icon
        return (
          <NavLink
            key={item.label}
            to={item.to}
            end={false}
            className={({ isActive }) => `nav-item${isActive ? ' active' : ''}`}
          >
            <Icon size={20} aria-hidden="true" />
            <span>{item.label}</span>
          </NavLink>
        )
      })}
    </nav>
  )
}
