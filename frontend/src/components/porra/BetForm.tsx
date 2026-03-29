import { useTranslation } from 'react-i18next'
import type { PorraDetail } from '../../lib/types'
import { Button } from '../ui/Button'
import { Field, SelectField } from '../ui/Field'
import { FormShell } from '../ui/FormShell'

interface BetFormProps {
  porra: PorraDetail
  onBet: (prediction: unknown) => Promise<void>
  loading?: boolean
}

export function BetForm({ porra, onBet, loading }: BetFormProps) {
  const { t } = useTranslation()

  return (
    <FormShell
      onSubmit={async (formData) => {
        const raw = String(formData.get('prediction'))
        const prediction =
          porra.type === 'BINARY'
            ? raw === 'true'
            : porra.type === 'NUMERIC'
              ? Number(raw)
              : raw
        await onBet(prediction)
      }}
    >
      {porra.type === 'BINARY' ? (
        <SelectField
          label={t('porra.prediction')}
          name="prediction"
          options={[
            ['true', t('porra.binaryYes')],
            ['false', t('porra.binaryNo')],
          ]}
        />
      ) : porra.type === 'MULTIPLE_CHOICE' ? (
        <SelectField
          label={t('porra.prediction')}
          name="prediction"
          options={porra.options.map((opt) => [opt, opt])}
        />
      ) : (
        <Field
          label={t('porra.prediction')}
          name="prediction"
          type="number"
          placeholder="42"
          required
        />
      )}
      <Button type="submit" fullWidth loading={loading}>
        {t('porra.placeBet')}
      </Button>
    </FormShell>
  )
}
