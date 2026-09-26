import { useSearchParams } from 'react-router-dom'
import { useAuth } from '../../auth/AuthContext'
import { useActions } from '../../hooks/useActions'
import { Solicitacoes } from '../aluno/Solicitacoes'
import { FilaCentral } from '../secretaria/FilaCentral'
import { FilaDeliberacao } from './FilaDeliberacao'

export function SolicitacoesRota() {
  const [params] = useSearchParams()
  const { links } = useAuth()
  const actions = useActions(links)

  if (params.get('to') === 'me') {
    return <FilaDeliberacao />
  }
  if (actions.can('fila-solicitacoes')) {
    return <FilaCentral />
  }
  return <Solicitacoes />
}
