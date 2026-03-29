import { useTranslation } from 'react-i18next'
import { statusLabel } from '../../lib/format'
import type { PorraDetail, Resolution, ResolutionVotes } from '../../lib/types'
import { Button } from '../ui/Button'
import { Field, SelectField, TextAreaField } from '../ui/Field'
import { FormShell } from '../ui/FormShell'
import { MetricCard } from '../ui/MetricCard'

interface ResolutionPanelProps {
  porra: PorraDetail
  resolution: Resolution | null
  votes: ResolutionVotes | null
  onPropose: (result: string) => Promise<void>
  onDispute: (reason: string) => Promise<void>
  onVote: (result: string) => Promise<void>
  onCancelPorra: (reason: string) => Promise<void>
}

function ResultField({ porra, name, label }: { porra: PorraDetail; name: string; label: string }) {
  const { t } = useTranslation()
  if (porra.type === 'BINARY') {
    return <SelectField label={label} name={name} options={[['true', t('porra.binaryYes')], ['false', t('porra.binaryNo')]]} />
  }
  if (porra.type === 'MULTIPLE_CHOICE') {
    return <SelectField label={label} name={name} options={porra.options.map((o) => [o, o])} />
  }
  return <Field label={label} name={name} type="number" placeholder="42" required />
}

export function ResolutionPanel({ porra, resolution, votes, onPropose, onDispute, onVote, onCancelPorra }: ResolutionPanelProps) {
  const { t } = useTranslation()

  // Propose result + cancel (when no resolution yet)
  if (!resolution) {
    return (
      <div className="stack">
        <div>
          <div className="eyebrow">{t('porra.resolutionEyebrow')}</div>
          <h3 className="section-title">{t('porra.resolutionTitle')}</h3>
        </div>
        <FormShell
          onSubmit={async (formData) => {
            await onPropose(String(formData.get('proposedResult')))
          }}
        >
          <ResultField porra={porra} name="proposedResult" label={t('porra.proposedResult')} />
          <Button type="submit" fullWidth>{t('porra.proposeButton')}</Button>
        </FormShell>

        <hr style={{ border: 'none', borderTop: '1px solid var(--border)' }} />

        <div>
          <div className="eyebrow">{t('status.CANCELLED')}</div>
          <h3 className="section-title">{t('porra.cancellationTitle')}</h3>
        </div>
        <FormShell
          onSubmit={async (formData) => {
            await onCancelPorra(String(formData.get('reason') ?? ''))
          }}
        >
          <TextAreaField label={t('porra.reason')} name="reason" placeholder={t('porra.cancellationPlaceholder')} />
          <Button type="submit" variant="secondary" fullWidth>{t('porra.proposeCancellation')}</Button>
        </FormShell>
      </div>
    )
  }

  // Resolution exists: show status, dispute option, and votes
  return (
    <div className="stack">
      <div>
        <div className="eyebrow">{t('porra.consensusEyebrow')}</div>
        <h3 className="section-title">{statusLabel(resolution.status)}</h3>
      </div>

      <div className="invite-card">
        <div className="card-title">{resolution.proposedResult}</div>
        <div className="muted">{t('porra.proposedBy', { name: resolution.proposedBy.email })}</div>
      </div>

      {resolution.status === 'WAITING' && (
        <FormShell
          onSubmit={async (formData) => {
            await onDispute(String(formData.get('reason') ?? ''))
          }}
        >
          <TextAreaField label={t('porra.disputeLabel')} name="reason" placeholder={t('porra.disputePlaceholder')} />
          <Button type="submit" variant="ghost" fullWidth>{t('porra.disputeButton')}</Button>
        </FormShell>
      )}

      {votes && (
        <div className="stack sm">
          <div className="grid-2">
            <MetricCard label={t('porra.votesCount')} value={`${votes.votesCast}/${votes.eligibleVoters}`} />
            <MetricCard label={t('common.status')} value={statusLabel(votes.status)} />
          </div>

          {votes.status === 'VOTING' && (
            <FormShell
              onSubmit={async (formData) => {
                await onVote(String(formData.get('votedResult')))
              }}
            >
              <ResultField porra={porra} name="votedResult" label={t('porra.voteLabel')} />
              <Button type="submit" fullWidth>{t('porra.voteButton')}</Button>
            </FormShell>
          )}
        </div>
      )}
    </div>
  )
}
