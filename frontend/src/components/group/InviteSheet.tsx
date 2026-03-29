import { Copy, Link } from 'lucide-react'
import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import type { InvitationCreation } from '../../lib/types'
import { BottomSheet } from '../layout/BottomSheet'
import { Button } from '../ui/Button'
import { Field } from '../ui/Field'
import { FormShell } from '../ui/FormShell'

interface InviteSheetProps {
  onClose: () => void
  onInvite: (email?: FormDataEntryValue | null) => Promise<InvitationCreation>
}

export function InviteSheet({ onClose, onInvite }: InviteSheetProps) {
  const { t } = useTranslation()
  const [invitation, setInvitation] = useState<InvitationCreation | null>(null)
  const [copied, setCopied] = useState(false)

  async function copyLink() {
    if (!invitation?.invitationLink) return
    await navigator.clipboard.writeText(invitation.invitationLink)
    setCopied(true)
    setTimeout(() => setCopied(false), 2000)
  }

  return (
    <BottomSheet title={t('group.invitesTitle')} onClose={onClose}>
      <FormShell
        onSubmit={async (formData) => {
          const inv = await onInvite(formData.get('email'))
          setInvitation(inv)
        }}
      >
        <Field
          label={t('group.inviteEmailOptional')}
          name="email"
          type="email"
          placeholder="amiga@example.com"
        />
        <Button type="submit" fullWidth>
          {t('group.inviteButton')}
        </Button>
      </FormShell>

      {invitation && (
        <div className="invite-card">
          <div className="eyebrow">{t('group.latestInvite')}</div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <Link size={14} color="var(--primary)" />
            <span style={{ fontSize: 'var(--text-sm)', flex: 1, wordBreak: 'break-all' }}>
              {invitation.invitationLink}
            </span>
          </div>
          <Button
            variant="secondary"
            fullWidth
            onClick={copyLink}
          >
            <Copy size={14} />
            {copied ? '¡Copiado!' : 'Copiar enlace'}
          </Button>
        </div>
      )}
    </BottomSheet>
  )
}
