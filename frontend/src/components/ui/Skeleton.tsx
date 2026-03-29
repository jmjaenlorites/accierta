interface SkeletonProps {
  variant?: 'text' | 'card' | 'avatar'
  size?: 'sm' | 'md' | 'lg'
  width?: string
}

export function Skeleton({ variant = 'text', size = 'md', width }: SkeletonProps) {
  const cls = ['skeleton', variant, size !== 'md' ? size : ''].filter(Boolean).join(' ')
  return <div className={cls} style={width ? { width } : undefined} />
}

export function CardSkeleton() {
  return (
    <div className="card card-body">
      <div className="row between">
        <div className="stack sm" style={{ flex: 1 }}>
          <Skeleton variant="text" size="lg" width="60%" />
          <Skeleton variant="text" size="sm" width="40%" />
        </div>
        <Skeleton variant="avatar" />
      </div>
      <div className="grid-2">
        <Skeleton variant="card" />
        <Skeleton variant="card" />
      </div>
    </div>
  )
}
