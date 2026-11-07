import { useActions } from '../hooks/useActions'
import type { HateoasLinks } from '../models/academico'

type ConfirmacaoFormativaWidgetProps = {
  links?: HateoasLinks
  pending?: boolean
  onConfirm: () => void
  onCancel: () => void
  onComprovante?: () => void
  comprovantePending?: boolean
}

export function ConfirmacaoFormativaWidget({
  links,
  pending = false,
  onConfirm,
  onCancel,
  onComprovante,
  comprovantePending = false,
}: ConfirmacaoFormativaWidgetProps) {
  const actions = useActions(links)
  const podeConfirmar = actions.can('confirmar')
  const podeCancelar = actions.can('cancelar')
  const podeComprovante = Boolean(onComprovante) && actions.can('comprovante')

  if (!podeConfirmar && !podeCancelar && !podeComprovante) {
    return (
      <p className="muted" role="status">
        Nenhuma ação disponível nesta formativa.
      </p>
    )
  }

  return (
    <div className="formativa-widget attendance-card card">
      {(podeConfirmar || podeCancelar) && (
        <p>
          A presença neste evento já foi validada pelo sistema. Confirme para enviar à CAAF ou cancele
          se não quiser registrar as horas.
        </p>
      )}
      {podeComprovante && !podeConfirmar && !podeCancelar && (
        <p>Visualize o comprovante enviado nesta atividade.</p>
      )}
      <div className="action-bar">
        {podeConfirmar && (
          <button type="button" onClick={onConfirm} disabled={pending}>
            {pending ? 'Confirmando…' : 'Confirmar'}
          </button>
        )}
        {podeCancelar && (
          <button type="button" className="ghost" onClick={onCancel} disabled={pending}>
            {pending ? 'Cancelando…' : 'Cancelar'}
          </button>
        )}
        {podeComprovante && (
          <button type="button" onClick={onComprovante} disabled={comprovantePending || pending}>
            {comprovantePending ? 'Abrindo…' : 'Ver comprovante'}
          </button>
        )}
      </div>
    </div>
  )
}
