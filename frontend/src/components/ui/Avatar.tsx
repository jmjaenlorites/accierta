import { avatarColor } from '../../lib/format'

type AvatarSize = 'sm' | 'md' | 'lg' | 'xl'

interface AvatarProps {
  email: string
  userId: string
  size?: AvatarSize
}

function getInitials(email: string): string {
  return email
    .split('@')[0]
    .split(/[.\-_]/)
    .map((chunk) => chunk[0]?.toUpperCase())
    .filter(Boolean)
    .slice(0, 2)
    .join('')
}

export function Avatar({ email, userId, size = 'md' }: AvatarProps) {
  const initials = getInitials(email)
  return (
    <div className={`avatar ${size}`} style={{ background: avatarColor(userId) }} aria-label={email}>
      {initials}
    </div>
  )
}
