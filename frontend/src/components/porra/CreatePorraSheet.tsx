import { HelpCircle, ChevronDown, ChevronUp } from 'lucide-react'
import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { initialDeadlines } from '../../lib/format'
import { BottomSheet } from '../layout/BottomSheet'
import { Button } from '../ui/Button'
import { ConfirmDialog } from '../ui/ConfirmDialog'
import { Field, SelectField, TextAreaField } from '../ui/Field'
import { StepIndicator } from '../ui/StepIndicator'
import { Toggle } from '../ui/Toggle'

type PorraType = 'BINARY' | 'NUMERIC' | 'MULTIPLE_CHOICE'

interface CreatePorraPayload {
  title: string
  description: string
  type: PorraType
  options: string[]
  tolerance?: number
  stake: number
  bettingDeadline: string
  resolutionDate: string
  visibility: string
  betModifiable: boolean
}

interface CreatePorraSheetProps {
  currency: string
  onClose: () => void
  onCreate: (payload: CreatePorraPayload) => Promise<void>
}

const STEPS = 4
const deadlines = initialDeadlines()

export function CreatePorraSheet({ currency, onClose, onCreate }: CreatePorraSheetProps) {
  const { t } = useTranslation()
  const [step, setStep] = useState(0)
  const [type, setType] = useState<PorraType>('BINARY')
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [stake, setStake] = useState('10')
  const [optionsList, setOptionsList] = useState<string[]>([])
  const [newOption, setNewOption] = useState('')
  const [tolerance, setTolerance] = useState('15')
  const [bettingDeadline, setBettingDeadline] = useState(deadlines.bettingDeadline)
  const [resolutionDate, setResolutionDate] = useState(deadlines.resolutionDate)
  const [visibility, setVisibility] = useState('HIDDEN')
  const [betModifiable, setBetModifiable] = useState(true)
  const [loading, setLoading] = useState(false)
  const [showAdvanced, setShowAdvanced] = useState(false)
  const [showToleranceHelp, setShowToleranceHelp] = useState(false)

  const typeOptions: Array<{ value: PorraType; label: string; desc: string }> = [
    { value: 'BINARY', label: t('porra.type.BINARY'), desc: t('porra.typeDesc.binary') },
    { value: 'MULTIPLE_CHOICE', label: t('porra.type.MULTIPLE_CHOICE'), desc: t('porra.typeDesc.multipleChoice') },
    { value: 'NUMERIC', label: t('porra.type.NUMERIC'), desc: t('porra.typeDesc.numeric') },
  ]

  async function handleCreate() {
    setLoading(true)
    try {
      const resolvedOptions =
        type === 'MULTIPLE_CHOICE'
          ? optionsList
          : type === 'BINARY'
            ? ['true', 'false']
            : []
      const payload: Record<string, unknown> = {
        title,
        description,
        type,
        options: resolvedOptions,
        stake: Number(stake),
        bettingDeadline: new Date(bettingDeadline).toISOString(),
        resolutionDate: new Date(resolutionDate).toISOString(),
        visibility,
        betModifiable,
      }
      if (type === 'NUMERIC') {
        payload.tolerance = Number(tolerance) || 15
      }
      await onCreate(payload as unknown as CreatePorraPayload)
      onClose()
    } finally {
      setLoading(false)
    }
  }

  return (
    <BottomSheet
      title={t('group.newPorra')}
      subtitle={t('group.newPorraStep', { current: step + 1, total: STEPS })}
      onClose={onClose}
    >
      <StepIndicator steps={STEPS} current={step} />

      {/* Step 0: Type */}
      {step === 0 && (
        <div className="stack">
          <p className="muted">{t('common.type')}</p>
          {typeOptions.map((opt) => (
            <button
              key={opt.value}
              type="button"
              onClick={() => setType(opt.value)}
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                padding: 'var(--space-4)',
                borderRadius: 'var(--radius-md)',
                border: `2px solid ${type === opt.value ? 'var(--primary)' : 'var(--border)'}`,
                background: type === opt.value ? 'rgba(108,92,231,0.06)' : 'var(--surface)',
                cursor: 'pointer',
                textAlign: 'left',
                width: '100%',
                minHeight: 56,
                transition: 'border-color 150ms ease',
              }}
            >
              <div>
                <div style={{ fontWeight: 700, fontFamily: '"Plus Jakarta Sans",sans-serif' }}>{opt.label}</div>
                <div style={{ fontSize: 'var(--text-sm)', color: 'var(--text-secondary)' }}>{opt.desc}</div>
              </div>
              {type === opt.value && (
                <div
                  style={{
                    width: 20,
                    height: 20,
                    borderRadius: '50%',
                    background: 'var(--primary)',
                    flexShrink: 0,
                  }}
                />
              )}
            </button>
          ))}
          <Button fullWidth onClick={() => setStep(1)}>{t('common.next')}</Button>
        </div>
      )}

      {/* Step 1: Details */}
      {step === 1 && (
        <div className="stack">
          <Field
            label={t('porra.title')}
            name="title"
            required
            placeholder={t('porra.titlePlaceholder')}
            value={title}
            onChange={setTitle}
          />
          <Field
            label={t('porra.stakeLabel')}
            name="stake"
            type="number"
            value={stake}
            onChange={setStake}
            required
            suffix={currency}
          />
          <TextAreaField
            label={t('porra.description')}
            name="description"
            placeholder={t('porra.descriptionPlaceholder')}
            value={description}
            onChange={setDescription}
          />
          {type === 'MULTIPLE_CHOICE' && (
            <div className="field">
              <label>{t('porra.options')}</label>

              {/* Listed options as removable pills */}
              {optionsList.length > 0 && (
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: 'var(--space-2)' }}>
                  {optionsList.map((opt, i) => (
                    <span
                      key={i}
                      style={{
                        display: 'inline-flex',
                        alignItems: 'center',
                        gap: 'var(--space-1)',
                        padding: '4px var(--space-3)',
                        borderRadius: 'var(--radius-full)',
                        background: 'rgba(108,92,231,0.1)',
                        color: 'var(--primary)',
                        fontSize: 'var(--text-sm)',
                        fontWeight: 600,
                      }}
                    >
                      {opt}
                      <button
                        type="button"
                        onClick={() => setOptionsList(optionsList.filter((_, j) => j !== i))}
                        style={{
                          background: 'none',
                          border: 'none',
                          color: 'var(--primary)',
                          cursor: 'pointer',
                          padding: 0,
                          lineHeight: 1,
                          fontSize: 16,
                          display: 'inline-flex',
                        }}
                        aria-label={t('common.delete')}
                      >
                        &times;
                      </button>
                    </span>
                  ))}
                </div>
              )}

              {/* Input + add button */}
              <div style={{ display: 'flex', gap: 'var(--space-2)' }}>
                <input
                  className="input"
                  type="text"
                  placeholder={t('porra.optionPlaceholder')}
                  value={newOption}
                  onChange={(e) => setNewOption(e.target.value)}
                  onKeyDown={(e) => {
                    if (e.key === 'Enter') {
                      e.preventDefault()
                      const val = newOption.trim()
                      if (val && !optionsList.includes(val)) {
                        setOptionsList([...optionsList, val])
                        setNewOption('')
                      }
                    }
                  }}
                  style={{ flex: 1 }}
                />
                <Button
                  type="button"
                  variant="secondary"
                  size="sm"
                  onClick={() => {
                    const val = newOption.trim()
                    if (val && !optionsList.includes(val)) {
                      setOptionsList([...optionsList, val])
                      setNewOption('')
                    }
                  }}
                  disabled={!newOption.trim()}
                >
                  {t('porra.addOption')}
                </Button>
              </div>

              {optionsList.length < 2 && (
                <p className="field-hint">{t('porra.minOptionsHint')}</p>
              )}
            </div>
          )}

          {/* Advanced section for NUMERIC tolerance */}
          {type === 'NUMERIC' && (
            <>
              <button
                type="button"
                onClick={() => setShowAdvanced(!showAdvanced)}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: 'var(--space-2)',
                  background: 'none',
                  border: 'none',
                  color: 'var(--text-secondary)',
                  fontSize: 'var(--text-sm)',
                  fontWeight: 600,
                  cursor: 'pointer',
                  padding: 'var(--space-2) 0',
                }}
              >
                {showAdvanced ? <ChevronUp size={16} /> : <ChevronDown size={16} />}
                {t('porra.advanced')}
              </button>
              {showAdvanced && (
                <div style={{
                  display: 'flex',
                  alignItems: 'flex-start',
                  gap: 'var(--space-2)',
                }}>
                  <div style={{ flex: 1 }}>
                    <Field
                      label={t('porra.tolerance')}
                      name="tolerance"
                      type="number"
                      value={tolerance}
                      onChange={setTolerance}
                      hint={t('porra.toleranceDefault')}
                    />
                  </div>
                  <button
                    type="button"
                    onClick={() => setShowToleranceHelp(true)}
                    style={{
                      marginTop: 28,
                      background: 'none',
                      border: 'none',
                      color: 'var(--text-muted)',
                      cursor: 'pointer',
                      padding: 'var(--space-1)',
                      flexShrink: 0,
                    }}
                    aria-label={t('porra.toleranceHelpLabel')}
                  >
                    <HelpCircle size={20} />
                  </button>
                </div>
              )}
            </>
          )}

          <div className="row" style={{ justifyContent: 'space-between' }}>
            <Button variant="ghost" onClick={() => setStep(0)}>{t('common.back')}</Button>
            <Button onClick={() => setStep(2)} disabled={!title || (type === 'MULTIPLE_CHOICE' && optionsList.length < 2)}>{t('common.next')}</Button>
          </div>
        </div>
      )}

      {/* Step 2: Dates */}
      {step === 2 && (
        <div className="stack">
          <Field
            label={t('porra.bettingDeadline')}
            name="bettingDeadline"
            type="datetime-local"
            value={bettingDeadline}
            onChange={setBettingDeadline}
            required
          />
          <Field
            label={t('porra.resolutionDate')}
            name="resolutionDate"
            type="datetime-local"
            value={resolutionDate}
            onChange={setResolutionDate}
            required
          />
          <div className="row" style={{ justifyContent: 'space-between' }}>
            <Button variant="ghost" onClick={() => setStep(1)}>{t('common.back')}</Button>
            <Button onClick={() => setStep(3)}>{t('common.next')}</Button>
          </div>
        </div>
      )}

      {/* Step 3: Settings + Review */}
      {step === 3 && (
        <div className="stack">
          <SelectField
            label={t('porra.visibility')}
            name="visibility"
            options={[
              ['HIDDEN', t('porra.visibilityOptions.HIDDEN')],
              ['VISIBLE', t('porra.visibilityOptions.VISIBLE')],
            ]}
            value={visibility}
            onChange={setVisibility}
          />
          <Toggle
            name="betModifiable"
            label={t('porra.modifiable')}
            description={t('porra.modifiableHint')}
            checked={betModifiable}
            onChange={setBetModifiable}
          />

          <div
            className="card card-body"
            style={{ background: 'rgba(108,92,231,0.04)', marginTop: 8 }}
          >
            <div className="eyebrow">{t('common.overview')}</div>
            <div className="stack sm">
              <div className="row between">
                <span className="muted">{t('porra.title')}</span>
                <span style={{ fontWeight: 600 }}>{title}</span>
              </div>
              <div className="row between">
                <span className="muted">{t('common.type')}</span>
                <span style={{ fontWeight: 600 }}>{t(`porra.type.${type}`)}</span>
              </div>
              <div className="row between">
                <span className="muted">{t('porra.stakeLabel')}</span>
                <span style={{ fontWeight: 600, fontFamily: '"JetBrains Mono", monospace' }}>{stake} {currency}</span>
              </div>
              {type === 'NUMERIC' && (
                <div className="row between">
                  <span className="muted">{t('porra.tolerance')}</span>
                  <span style={{ fontWeight: 600, fontFamily: '"JetBrains Mono", monospace' }}>{tolerance}%</span>
                </div>
              )}
            </div>
          </div>

          <div className="row" style={{ justifyContent: 'space-between' }}>
            <Button variant="ghost" onClick={() => setStep(2)}>{t('common.back')}</Button>
            <Button onClick={handleCreate} loading={loading}>{t('porra.createButton')}</Button>
          </div>
        </div>
      )}

      {/* Tolerance help modal */}
      {showToleranceHelp && (
        <ConfirmDialog
          title={t('porra.toleranceHelpTitle')}
          message={t('porra.toleranceHelpBody')}
          confirmLabel={t('common.understood')}
          onConfirm={() => setShowToleranceHelp(false)}
          onCancel={() => setShowToleranceHelp(false)}
        />
      )}
    </BottomSheet>
  )
}
