import { useActions } from '../hooks/useActions'
import type { HateoasLinks } from '../models/academico'

type Props = {
  links?: HateoasLinks
  pending?: boolean
  onAbrirJanela?: () => void
  onAbrirJanelaSaida?: () => void
  onRenovarQr?: () => void
  onEncerrar?: () => void
}

export function HostActionBar({
  links,
  pending,
  onAbrirJanela,
  onAbrirJanelaSaida,
  onRenovarQr,
  onEncerrar,
}: Props) {
  const actions = useActions(links)
  const podeAbrir = actions.can('abrir-janela-entrada')
  const podeAbrirSaida = actions.can('abrir-janela-saida')
  const podeRenovar = actions.can('renovar-qr')
  const podeEncerrar = actions.can('encerrar-evento')
  if (!podeAbrir && !podeAbrirSaida && !podeRenovar && !podeEncerrar) {
    return null
  }
  return (
    <div className="wizard-actions">
      {podeAbrir && (
        <button type="button" onClick={onAbrirJanela} disabled={pending}>
          Abrir janela de entrada
        </button>
      )}
      {podeAbrirSaida && (
        <button type="button" onClick={onAbrirJanelaSaida} disabled={pending}>
          Abrir janela de saída
        </button>
      )}
      {podeRenovar && (
        <button type="button" className="ghost" onClick={onRenovarQr} disabled={pending}>
          Renovar QR
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
