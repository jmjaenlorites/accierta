import { useState } from 'react'
import { Eye, EyeOff } from 'lucide-react'

interface FieldProps {
  label: string
  name: string
  type?: string
  autoComplete?: string
  required?: boolean
  defaultValue?: string
  placeholder?: string
  value?: string
  onChange?: (value: string) => void
  error?: string
  hint?: string
  suffix?: string
}

export function Field({
  label,
  name,
  type = 'text',
  autoComplete,
  required,
  defaultValue,
  placeholder,
  value,
  onChange,
  error,
  hint,
  suffix,
}: FieldProps) {
  const [showPassword, setShowPassword] = useState(false)
  const isPassword = type === 'password'
  const resolvedType = isPassword ? (showPassword ? 'text' : 'password') : type
  const resolvedAutoComplete =
    autoComplete ?? (type === 'email' ? 'email' : type === 'password' ? 'current-password' : 'off')

  return (
    <div className="field">
      <label htmlFor={name}>{label}</label>
      {isPassword ? (
        <div className="password-wrapper">
          <input
            className={`input${error ? ' error' : ''}`}
            id={name}
            name={name}
            type={resolvedType}
            autoComplete={resolvedAutoComplete}
            required={required}
            defaultValue={defaultValue}
            placeholder={placeholder}
            value={value}
            onChange={onChange ? (e) => onChange(e.target.value) : undefined}
          />
          <button
            type="button"
            className="password-toggle"
            onClick={() => setShowPassword((v) => !v)}
            aria-label={showPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'}
          >
            {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
          </button>
        </div>
      ) : suffix ? (
        <div className="input-suffix-wrapper">
          <input
            className={`input${error ? ' error' : ''}`}
            id={name}
            name={name}
            type={resolvedType}
            autoComplete={resolvedAutoComplete}
            required={required}
            defaultValue={defaultValue}
            placeholder={placeholder}
            value={value}
            onChange={onChange ? (e) => onChange(e.target.value) : undefined}
          />
          <span className="input-suffix">{suffix}</span>
        </div>
      ) : (
        <input
          className={`input${error ? ' error' : ''}`}
          id={name}
          name={name}
          type={resolvedType}
          autoComplete={resolvedAutoComplete}
          required={required}
          defaultValue={defaultValue}
          placeholder={placeholder}
          value={value}
          onChange={onChange ? (e) => onChange(e.target.value) : undefined}
        />
      )}
      {hint && !error && <p className="field-hint">{hint}</p>}
      {error && <p className="field-error">{error}</p>}
    </div>
  )
}

interface SelectFieldProps {
  label: string
  name: string
  options: Array<[string, string]>
  defaultValue?: string
  value?: string
  onChange?: (value: string) => void
}

export function SelectField({ label, name, options, defaultValue, value, onChange }: SelectFieldProps) {
  return (
    <div className="field">
      <label htmlFor={name}>{label}</label>
      <select
        className="select"
        id={name}
        name={name}
        defaultValue={defaultValue ?? options[0]?.[0]}
        value={value}
        onChange={onChange ? (e) => onChange(e.target.value) : undefined}
      >
        {options.map(([val, text]) => (
          <option key={val} value={val}>
            {text}
          </option>
        ))}
      </select>
    </div>
  )
}

interface TextAreaFieldProps {
  label: string
  name: string
  placeholder?: string
  required?: boolean
  value?: string
  onChange?: (value: string) => void
}

export function TextAreaField({ label, name, placeholder, required, value, onChange }: TextAreaFieldProps) {
  return (
    <div className="field">
      <label htmlFor={name}>{label}</label>
      <textarea
        className="textarea"
        id={name}
        name={name}
        placeholder={placeholder}
        required={required}
        value={value}
        onChange={onChange ? (e) => onChange(e.target.value) : undefined}
      />
    </div>
  )
}
