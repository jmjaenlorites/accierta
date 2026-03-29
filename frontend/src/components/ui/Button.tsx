import type { ButtonHTMLAttributes, ReactNode } from 'react'
import { Link } from 'react-router-dom'

type ButtonVariant = 'primary' | 'secondary' | 'ghost' | 'danger'
type ButtonSize = 'sm' | 'md'

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: ButtonVariant
  size?: ButtonSize
  fullWidth?: boolean
  loading?: boolean
  children: ReactNode
}

export function Button({
  variant = 'primary',
  size = 'md',
  fullWidth = false,
  loading = false,
  children,
  disabled,
  ...props
}: ButtonProps) {
  const classes = [
    'btn',
    variant,
    size === 'sm' ? 'sm' : '',
    fullWidth ? 'full' : '',
  ]
    .filter(Boolean)
    .join(' ')

  return (
    <button className={classes} disabled={disabled || loading} {...props}>
      {loading ? (
        <span style={{ display: 'inline-flex', alignItems: 'center', gap: 6 }}>
          <span
            style={{
              width: 14,
              height: 14,
              border: '2px solid currentColor',
              borderTopColor: 'transparent',
              borderRadius: '50%',
              animation: 'spin 0.8s linear infinite',
              display: 'inline-block',
            }}
          />
          {children}
        </span>
      ) : (
        children
      )}
    </button>
  )
}

interface LinkButtonProps {
  to: string
  variant?: ButtonVariant
  size?: ButtonSize
  fullWidth?: boolean
  children: ReactNode
}

export function LinkButton({
  to,
  variant = 'primary',
  size = 'md',
  fullWidth = false,
  children,
}: LinkButtonProps) {
  const classes = ['btn', variant, size === 'sm' ? 'sm' : '', fullWidth ? 'full' : '']
    .filter(Boolean)
    .join(' ')
  return (
    <Link className={classes} to={to}>
      {children}
    </Link>
  )
}

export function IconButton({
  children,
  badge,
  badgeCount,
  ...props
}: ButtonHTMLAttributes<HTMLButtonElement> & {
  badge?: boolean
  badgeCount?: number
}) {
  return (
    <button
      className={`icon-btn${badge ? ' badge' : ''}`}
      data-count={badgeCount}
      {...props}
    >
      {children}
    </button>
  )
}
