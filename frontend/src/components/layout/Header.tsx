import { ArrowLeft } from 'lucide-react'
import type { ReactNode } from 'react'
import { useTranslation } from 'react-i18next'
import { useNavigate } from 'react-router-dom'

interface HeaderProps {
  title: string
  backTo?: string
  rightSlot?: ReactNode
}

export function Header({ title, backTo, rightSlot }: HeaderProps) {
  const navigate = useNavigate()
  const { t } = useTranslation()

  return (
    <header className="app-header">
      <div className="app-header-left">
        {backTo ? (
          <button
            className="icon-btn"
            onClick={() => navigate(backTo)}
            type="button"
            aria-label={t('common.back')}
          >
            <ArrowLeft size={20} />
          </button>
        ) : (
          <div className="brand-mark" aria-hidden="true">A</div>
        )}
        <span className="app-header-title">{title}</span>
      </div>
      {rightSlot && (
        <div className="app-header-right">{rightSlot}</div>
      )}
    </header>
  )
}
