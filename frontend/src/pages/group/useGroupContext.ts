import { useOutletContext } from 'react-router-dom'
import type { createAuthedServices } from '../../lib/services'
import type { GroupDetail } from '../../lib/types'

export type GroupContext = {
  group: GroupDetail
  services: ReturnType<typeof createAuthedServices>
  refreshGroup: () => void
}

export function useGroupContext() {
  return useOutletContext<GroupContext>()
}
