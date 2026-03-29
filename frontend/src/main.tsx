import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import App from './App'
import './i18n/i18n'
import { SessionProvider } from './lib/session'
import { ToastProvider } from './lib/toasts'
import './styles/tokens.css'
import './styles/base.css'
import './styles/animations.css'
import './styles/app.css'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <BrowserRouter>
      <SessionProvider>
        <ToastProvider>
          <App />
        </ToastProvider>
      </SessionProvider>
    </BrowserRouter>
  </StrictMode>,
)
