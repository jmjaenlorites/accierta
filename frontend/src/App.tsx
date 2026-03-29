import { Navigate, Route, Routes } from 'react-router-dom'
import { BottomNav } from './components/layout/BottomNav'
import { ToastViewport } from './components/ui/Toast'
import { useSession } from './lib/session'

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
  const { session } = useSession()
  return session ? <>{children}</> : <Navigate to="/welcome" replace />
}

function RootRedirect() {
  const { session } = useSession()
  return <Navigate to={session ? '/groups' : '/welcome'} replace />
}

export default function App() {
  return (
    <>
      <div className="app-shell">
        <Routes>
          {/* Root redirect */}
          <Route path="/" element={<RootRedirect />} />

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
