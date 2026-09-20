import { FormEvent, useState } from 'react'
import { useActions } from '../hooks/useActions'
import type { HateoasLinks } from '../models/academico'

const PARECER_INDEFER_MIN = 20

type PainelRevisaoFormativaProps = {
  links?: HateoasLinks
  pending?: boolean
  onRevisar: (action: 'APROVAR' | 'INDEFERIR', parecer: string) => void
}

export function PainelRevisaoFormativa({
  links,
  pending = false,
  onRevisar,
}: PainelRevisaoFormativaProps) {
  const actions = useActions(links)
  const podeAprovar = actions.can('aprovar')
  const podeIndeferir = actions.can('indeferir')
  const [parecer, setParecer] = useState('')
  const [erroParecer, setErroParecer] = useState<string | null>(null)

  if (!podeAprovar && !podeIndeferir) {
    return (
      <p className="muted" role="status">
        Nenhuma ação de revisão disponível neste estado.
      </p>
    )
  }

  function disparar(action: 'APROVAR' | 'INDEFERIR') {
    const texto = parecer.trim()
    if (action === 'INDEFERIR' && texto.length < PARECER_INDEFER_MIN) {
      setErroParecer(`Informe o parecer para indeferimento (mín. ${PARECER_INDEFER_MIN} caracteres).`)
      document.getElementById('parecer-revisao-formativa')?.focus()
      return
    }
    if (!texto) {
      setErroParecer('Informe o parecer.')
      document.getElementById('parecer-revisao-formativa')?.focus()
      return
    }
    setErroParecer(null)
    onRevisar(action, texto)
  }

  function onSubmit(event: FormEvent) {
    event.preventDefault()
  }

  return (
    <form className="painel-decisao panel" onSubmit={onSubmit}>
      <h2>Parecer da CAAF</h2>
      <label htmlFor="parecer-revisao-formativa">
        Parecer
        <textarea
          id="parecer-revisao-formativa"
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
          aria-describedby={erroParecer ? 'parecer-revisao-erro' : undefined}
          disabled={pending}
        />
      </label>
      {erroParecer && (
        <p id="parecer-revisao-erro" className="field-error" role="alert">
          {erroParecer}
        </p>
      )}
      <div className="action-bar">
        {podeAprovar && (
          <button type="button" className="success" onClick={() => disparar('APROVAR')} disabled={pending}>
            {pending ? 'Enviando…' : 'Aprovar'}
          </button>
        )}
        {podeIndeferir && (
          <button type="button" className="danger" onClick={() => disparar('INDEFERIR')} disabled={pending}>
            Indeferir
          </button>
        )}
      </div>
    </form>
  )
}
