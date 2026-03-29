interface ToggleProps {
  name: string
  label: string
  description?: string
  checked?: boolean
  defaultChecked?: boolean
  onChange?: (checked: boolean) => void
}

export function Toggle({ name, label, description, checked, defaultChecked, onChange }: ToggleProps) {
  return (
    <div className="toggle-wrapper">
      <div className="toggle-label">
        <span className="toggle-label-text">{label}</span>
        {description && <span className="toggle-label-desc">{description}</span>}
      </div>
      <label className="toggle">
        <input
          type="checkbox"
          name={name}
          checked={checked}
          defaultChecked={defaultChecked}
          onChange={onChange ? (e) => onChange(e.target.checked) : undefined}
        />
        <span className="toggle-track" />
      </label>
    </div>
  )
}
