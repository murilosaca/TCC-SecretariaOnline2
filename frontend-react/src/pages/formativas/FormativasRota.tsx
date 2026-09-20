import { useSearchParams } from 'react-router-dom'
import { Formativas } from '../aluno/Formativas'
import { FilaRevisao } from './FilaRevisao'

export function FormativasRota() {
  const [params] = useSearchParams()
  if (params.get('to') === 'me') {
    return <FilaRevisao />
  }
  return <Formativas />
}
