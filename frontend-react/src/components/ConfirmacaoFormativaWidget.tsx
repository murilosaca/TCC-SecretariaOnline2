import { useActions } from '../hooks/useActions'
import type { HateoasLinks } from '../models/academico'

type ConfirmacaoFormativaWidgetProps = {
  links?: HateoasLinks
  pending?: boolean
  onConfirm: () => void
  onCancel: () => void
}

export function ConfirmacaoFormativaWidget({
  links,
  pending = false,
  onConfirm,
  onCancel,
}: ConfirmacaoFormativaWidgetProps) {
  const actions = useActions(links)
  const podeConfirmar = actions.can('confirmar')
  const podeCancelar = actions.can('cancelar')

  if (!podeConfirmar && !podeCancelar) {
    return (
      <p className="muted" role="status">
        Nenhuma ação disponível nesta formativa.
      </p>
    )
  }

  return (
    <div className="formativa-widget attendance-card card">
      <p>
        A presença neste evento já foi validada pelo sistema. Confirme para enviar à CAAF ou cancele
        se não quiser registrar as horas.
      </p>
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
      </div>
    </div>
  )
}
