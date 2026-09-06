import { useMemo, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useMutation, useQuery } from '@tanstack/react-query'
import { ApiError } from '../../api/client'
import { solicitacoesApi } from '../../api/solicitacoes'
import { DynamicForm } from '../../components/DynamicForm'
import { fieldsFromSchema, validateAgainstSchema } from '../../lib/formSchema'
import type { RequestType } from '../../models/solicitacao'

export function NovaSolicitacao() {
  const navigate = useNavigate()
  const [passo, setPasso] = useState(1)
  const [tipoCodigo, setTipoCodigo] = useState<string | null>(null)
  const [values, setValues] = useState<Record<string, unknown>>({})
  const [errors, setErrors] = useState<Record<string, string>>({})

  const tipos = useQuery({
    queryKey: ['request-types'],
    queryFn: () => solicitacoesApi.listarTipos(),
  })

  const tipoSelecionado = useMemo(
    () => tipos.data?.content.find((tipo) => tipo.codigo === tipoCodigo) ?? null,
    [tipos.data, tipoCodigo],
  )
  const fields = fieldsFromSchema(tipoSelecionado?.formSchema)

  const criar = useMutation({
    mutationFn: () => solicitacoesApi.criar(tipoCodigo as string, values),
    onSuccess: (criada) => navigate(`/solicitacoes/${criada.id}`, { replace: true }),
  })

  function escolherTipo(tipo: RequestType) {
    setTipoCodigo(tipo.codigo)
    setValues({})
    setErrors({})
    setPasso(2)
  }

  function continuarFormulario() {
    if (!tipoSelecionado) {
      return
    }
    const atuais = validateAgainstSchema(tipoSelecionado.formSchema, values)
    setErrors(atuais)
    if (Object.keys(atuais).length === 0) {
      setPasso(3)
    }
  }

  return (
    <section className="page wizard">
      <header className="page-head">
        <div>
          <h1>Nova solicitação</h1>
          <p className="muted">Wizard genérico dirigido pelo form_schema do RequestType (RF-F1-005-b).</p>
        </div>
        <Link to="/solicitacoes" className="ghost-link">
          Voltar à lista
        </Link>
      </header>

      <ol className="stepper" aria-label="Passos do wizard">
        <li aria-current={passo === 1 ? 'step' : undefined} className={passo === 1 ? 'atual' : undefined}>
          1. Tipo
        </li>
        <li aria-current={passo === 2 ? 'step' : undefined} className={passo === 2 ? 'atual' : undefined}>
          2. Formulário
        </li>
        <li aria-current={passo === 3 ? 'step' : undefined} className={passo === 3 ? 'atual' : undefined}>
          3. Revisar
        </li>
      </ol>

      {tipos.isError && (
        <div className="banner danger" role="alert">
          Não foi possível carregar os tipos.{' '}
          <button type="button" onClick={() => tipos.refetch()}>
            Tentar de novo
          </button>
        </div>
      )}
      {criar.isError && (
        <div className="banner danger" role="alert">
          {criar.error instanceof ApiError ? criar.error.message : 'Falha ao abrir a solicitação.'}
        </div>
      )}

      {passo === 1 && (
        <div>
          {tipos.isLoading && <p className="muted">Carregando tipos…</p>}
          {tipos.data && tipos.data.content.length === 0 && (
            <p className="empty">Nenhum tipo de solicitação disponível para você.</p>
          )}
          <div className="cards">
            {tipos.data?.content.map((tipo) => (
              <button
                key={tipo.id}
                type="button"
                className="card tipo-card"
                onClick={() => escolherTipo(tipo)}
              >
                <h2>{tipo.nome}</h2>
                <p>{tipo.descricao ?? 'Sem descrição.'}</p>
                <p className="muted">Prazo: {tipo.prazoDias} dias</p>
              </button>
            ))}
          </div>
        </div>
      )}

      {passo === 2 && tipoSelecionado && (
        <div className="panel">
          <h2>{tipoSelecionado.nome}</h2>
          <DynamicForm
            fields={fields}
            values={values}
            errors={errors}
            onChange={(name, value) => setValues((atual) => ({ ...atual, [name]: value }))}
          />
          <div className="wizard-actions">
            <button type="button" className="ghost" onClick={() => setPasso(1)}>
              Voltar
            </button>
            <button type="button" onClick={continuarFormulario}>
              Continuar
            </button>
          </div>
        </div>
      )}

      {passo === 3 && tipoSelecionado && (
        <div className="panel">
          <h2>Revisar</h2>
          <p>
            <strong>Tipo:</strong> {tipoSelecionado.nome}
          </p>
          <dl className="resumo">
            {fields.map((field) => (
              <div key={field.name}>
                <dt>{field.title}</dt>
                <dd>{formatar(values[field.name])}</dd>
              </div>
            ))}
          </dl>
          <div className="wizard-actions">
            <button type="button" className="ghost" onClick={() => setPasso(2)}>
              Voltar
            </button>
            <button type="button" disabled={criar.isPending} onClick={() => criar.mutate()}>
              {criar.isPending ? 'Confirmando…' : 'Confirmar'}
            </button>
          </div>
        </div>
      )}
    </section>
  )
}

function formatar(valor: unknown): string {
  if (valor === undefined || valor === null || valor === '') {
    return '—'
  }
  if (typeof valor === 'boolean') {
    return valor ? 'Sim' : 'Não'
  }
  return String(valor)
}
