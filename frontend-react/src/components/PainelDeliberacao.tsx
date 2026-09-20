import { FormEvent, useState } from 'react'
import { useActions } from '../hooks/useActions'
import type { HateoasLinks } from '../models/academico'

const PARECER_INDEFER_MIN = 20

type PainelDeliberacaoProps = {
  links?: HateoasLinks
  pending?: boolean
  onDeliberar: (action: string, parecer: string) => void
}

export function PainelDeliberacao({ links, pending = false, onDeliberar }: PainelDeliberacaoProps) {
  const actions = useActions(links)
  const podeDeferir = actions.can('deferir')
  const podeIndeferir = actions.can('indeferir')
  const podeAjustes = actions.can('solicitar-ajustes')
  const [parecer, setParecer] = useState('')
  const [erroParecer, setErroParecer] = useState<string | null>(null)

  if (!podeDeferir && !podeIndeferir && !podeAjustes) {
    return (
      <p className="muted" role="status">
        Nenhuma ação de deliberação disponível neste estado.
      </p>
    )
  }

  function disparar(action: string) {
    const texto = parecer.trim()
    if (action === 'INDEFER' && texto.length < PARECER_INDEFER_MIN) {
      setErroParecer(`Informe o parecer para indeferimento (mín. ${PARECER_INDEFER_MIN} caracteres).`)
      document.getElementById('parecer-deliberacao')?.focus()
      return
    }
    if (!texto) {
      setErroParecer('Informe o parecer.')
      document.getElementById('parecer-deliberacao')?.focus()
      return
    }
    setErroParecer(null)
    onDeliberar(action, texto)
  }

  function onSubmit(event: FormEvent) {
    event.preventDefault()
  }

  return (
    <form className="painel-decisao panel" onSubmit={onSubmit}>
      <h2>Decisão</h2>
      <label htmlFor="parecer-deliberacao">
        Parecer
        <textarea
          id="parecer-deliberacao"
          name="parecer"
          rows={5}
          value={parecer}
          onChange={(event) => {
            setParecer(event.target.value)
            if (erroParecer) {
              setErroParecer(null)
            }
          }}
          className={erroParecer ? 'invalid' : undefined}
          aria-invalid={Boolean(erroParecer)}
          aria-describedby={erroParecer ? 'parecer-erro' : undefined}
          disabled={pending}
        />
      </label>
      {erroParecer && (
        <p id="parecer-erro" className="field-error" role="alert">
          {erroParecer}
        </p>
      )}
      <div className="action-bar">
        {podeDeferir && (
          <button type="button" className="success" onClick={() => disparar('DEFER')} disabled={pending}>
            {pending ? 'Enviando…' : 'Deferir'}
          </button>
        )}
        {podeIndeferir && (
          <button type="button" className="danger" onClick={() => disparar('INDEFER')} disabled={pending}>
            Indeferir
          </button>
        )}
        {podeAjustes && (
          <button type="button" className="warning" onClick={() => disparar('REQUEST_ADJUST')} disabled={pending}>
            Solicitar ajustes
          </button>
        )}
      </div>
    </form>
  )
}
