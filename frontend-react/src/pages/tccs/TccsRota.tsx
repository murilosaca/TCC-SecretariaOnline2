import { useSearchParams } from 'react-router-dom'
import { FilaRevisaoTcc } from './FilaRevisaoTcc'
import { Tccs } from './Tccs'

export function TccsRota() {
  const [params] = useSearchParams()
  if (params.get('to') === 'me') {
    return <FilaRevisaoTcc />
  }
  return <Tccs />
}
