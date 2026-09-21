import { useSearchParams } from 'react-router-dom'
import { Estagios } from './Estagios'
import { FilaRevisaoEstagio } from './FilaRevisaoEstagio'

export function EstagiosRota() {
  const [params] = useSearchParams()
  if (params.get('to') === 'me') {
    return <FilaRevisaoEstagio />
  }
  return <Estagios />
}
