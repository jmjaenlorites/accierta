import { Navigate, Route, Routes, useLocation } from 'react-router-dom'
import { BottomNav } from './components/layout/BottomNav'
import { ToastViewport } from './components/ui/Toast'
import { useSession } from './lib/useSession'

// Landing
import { LandingPage } from './pages/LandingPage'

// Legal
import { PrivacyPage } from './pages/legal/PrivacyPage'
import { TermsPage } from './pages/legal/TermsPage'

// Auth pages
import { WelcomePage } from './pages/auth/WelcomePage'
import { LoginPage } from './pages/auth/LoginPage'
import { RegisterPage } from './pages/auth/RegisterPage'
import { VerifyEmailPage } from './pages/auth/VerifyEmailPage'
import { ResetPasswordPage } from './pages/auth/ResetPasswordPage'

// Group pages
import { GroupLayout } from './pages/group/GroupLayout'
import { GroupHomePage } from './pages/group/GroupHomePage'
import { GroupPorrasPage } from './pages/group/GroupPorrasPage'
import { GroupRankingPage } from './pages/group/GroupRankingPage'
import { GroupDebtsPage } from './pages/group/GroupDebtsPage'
import { GroupProfilePage } from './pages/group/GroupProfilePage'

// Main pages
import { GroupsPage } from './pages/GroupsPage'
import { InvitationPage } from './pages/InvitationPage'
import { PorraDetailPage } from './pages/PorraDetailPage'
import { ProfilePage } from './pages/ProfilePage'

function RequireAuth({ children }: { children: React.ReactNode }) {
  const { session, ready } = useSession()
  if (!ready) return null
  return session ? <>{children}</> : <Navigate to="/welcome" replace />
}

function RootRedirect() {
  const { session, ready } = useSession()
  if (!ready) return null
  return <Navigate to={session ? '/groups' : '/landing'} replace />
}

const NO_NAV_PREFIXES = ['/landing', '/privacy', '/terms', '/welcome', '/login', '/register', '/verify', '/reset-password', '/invite/']

export default function App() {
  const { pathname } = useLocation()
  const hideNav = NO_NAV_PREFIXES.some((p) => pathname.startsWith(p))

  return (
    <>
      <div className={`app-shell${hideNav ? ' no-nav' : ''}`}>
        <Routes>
          {/* Root redirect */}
          <Route path="/" element={<RootRedirect />} />

          {/* Landing page (public marketing) */}
          <Route path="/landing" element={<LandingPage />} />

          {/* Legal pages */}
          <Route path="/privacy" element={<PrivacyPage />} />
          <Route path="/terms" element={<TermsPage />} />

          {/* Auth flow (no bottom nav) */}
          <Route path="/welcome" element={<WelcomePage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/verify" element={<VerifyEmailPage />} />
          <Route path="/reset-password" element={<ResetPasswordPage />} />

          {/* Legacy /auth redirect */}
          <Route path="/auth" element={<Navigate to="/login" replace />} />

          {/* Invitation (standalone) */}
          <Route path="/invite/:token" element={<InvitationPage />} />

          {/* Main app */}
          <Route
            path="/groups"
            element={
              <RequireAuth>
                <GroupsPage />
              </RequireAuth>
            }
          />

          {/* Group with sub-tabs */}
          <Route
            path="/groups/:groupId"
            element={
              <RequireAuth>
                <GroupLayout />
              </RequireAuth>
            }
          >
            <Route index element={<GroupHomePage />} />
            <Route path="porras" element={<GroupPorrasPage />} />
            <Route path="ranking" element={<GroupRankingPage />} />
            <Route path="debts" element={<GroupDebtsPage />} />
            <Route path="profile" element={<GroupProfilePage />} />
            {/* Legacy members route */}
            <Route path="members" element={<GroupProfilePage />} />
          </Route>

          {/* Porra detail */}
          <Route
            path="/porras/:porraId"
            element={
              <RequireAuth>
                <PorraDetailPage />
              </RequireAuth>
            }
          />

          {/* Global profile */}
          <Route
            path="/profile"
            element={
              <RequireAuth>
                <ProfilePage />
              </RequireAuth>
            }
          />
        </Routes>

        <BottomNav />
      </div>
      {/* Toast OUTSIDE app-shell to avoid max-width offset issues */}
      <ToastViewport />
    </>
  )
}
