import { useSearchParams } from 'react-router-dom'
import { Solicitacoes } from '../aluno/Solicitacoes'
import { FilaDeliberacao } from './FilaDeliberacao'

export function SolicitacoesRota() {
  const [params] = useSearchParams()
  if (params.get('to') === 'me') {
    return <FilaDeliberacao />
  }
  return <Solicitacoes />
}
