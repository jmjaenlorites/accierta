interface StepIndicatorProps {
  steps: number
  current: number
}

export function StepIndicator({ steps, current }: StepIndicatorProps) {
  return (
    <div className="step-indicator" aria-label={`Step ${current + 1} of ${steps}`}>
      {Array.from({ length: steps }, (_, i) => (
        <div
          key={i}
          className={`step-dot ${i === current ? 'active' : i < current ? 'done' : ''}`}
        />
      ))}
    </div>
  )
}
