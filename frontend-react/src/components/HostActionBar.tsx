import { useActions } from '../hooks/useActions'
import type { HateoasLinks } from '../models/academico'

type Props = {
  links?: HateoasLinks
  pending?: boolean
  onAbrirJanela?: () => void
  onEncerrar?: () => void
}

export function HostActionBar({ links, pending, onAbrirJanela, onEncerrar }: Props) {
  const actions = useActions(links)
  const podeAbrir = actions.can('abrir-janela-entrada')
  const podeEncerrar = actions.can('encerrar-evento')
  if (!podeAbrir && !podeEncerrar) {
    return null
  }
  return (
    <div className="wizard-actions">
      {podeAbrir && (
        <button type="button" onClick={onAbrirJanela} disabled={pending}>
          Abrir janela de entrada
        </button>
      )}
      {podeEncerrar && (
        <button type="button" className="ghost" onClick={onEncerrar} disabled={pending}>
          Encerrar evento
        </button>
      )}
    </div>
  )
}
