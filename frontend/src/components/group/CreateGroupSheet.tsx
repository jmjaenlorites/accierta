import { useTranslation } from 'react-i18next'
import { BottomSheet } from '../layout/BottomSheet'
import { Button } from '../ui/Button'
import { Field, SelectField } from '../ui/Field'
import { FormShell } from '../ui/FormShell'

const CURRENCY_OPTIONS: Array<[string, string]> = [
  ['€', '€  Euro'],
  ['$', '$  Dólar'],
  ['£', '£  Libra'],
  ['pts', 'pts  Puntos'],
  ['🍺', '🍺  Cervezas'],
  ['🍕', '🍕  Pizzas'],
  ['☕', '☕  Cafés'],
]

interface CreateGroupSheetProps {
  onClose: () => void
  onCreate: (payload: { name: FormDataEntryValue | null; currency: FormDataEntryValue | null }) => Promise<void>
}

export function CreateGroupSheet({ onClose, onCreate }: CreateGroupSheetProps) {
  const { t } = useTranslation()

  return (
    <BottomSheet title={t('groupsPage.createTitle')} onClose={onClose}>
      <FormShell
        onSubmit={async (formData) => {
          await onCreate({ name: formData.get('name'), currency: formData.get('currency') })
          onClose()
        }}
      >
        <Field
          label={t('groupsPage.groupName')}
          name="name"
          required
          placeholder={t('groupsPage.groupNamePlaceholder')}
        />
        <SelectField
          label={t('groupsPage.currency')}
          name="currency"
          options={CURRENCY_OPTIONS}
          defaultValue="€"
        />
        <Button type="submit" fullWidth>
          {t('groupsPage.createButton')}
        </Button>
      </FormShell>
    </BottomSheet>
  )
}
