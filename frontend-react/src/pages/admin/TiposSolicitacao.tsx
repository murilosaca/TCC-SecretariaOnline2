import { useMemo, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { ApiError } from '../../api/client'
import { erroJson, schemaDe, tiposSolicitacaoApi, type TipoEditor } from '../../api/tiposSolicitacao'
import { useActions } from '../../hooks/useActions'
import { fieldsFromSchema } from '../../lib/formSchema'

const FORM_VAZIO = '{\n  "type": "object",\n  "properties": {}\n}'
const FLUXO_VAZIO =
  '{\n  "initial": "EM_ANALISE",\n  "states": {\n    "EM_ANALISE": { "on": { "DEFER": "DELIBERADA" } },\n    "DELIBERADA": { "on": {} }\n  }\n}'

export function TiposSolicitacao() {
  const client = useQueryClient()
  const lista = useQuery({
    queryKey: ['admin-tipos-solicitacao'],
    queryFn: () => tiposSolicitacaoApi.listar(),
  })
  const [selecionado, setSelecionado] = useState<string | null>(null)
  const [formSchema, setFormSchema] = useState(FORM_VAZIO)
  const [workflowJson, setWorkflowJson] = useState(FLUXO_VAZIO)
  const [codigo, setCodigo] = useState('')
  const [nome, setNome] = useState('')
  const tipo = lista.data?.content.find((item) => item.id === selecionado) ?? null
  const actions = useActions(tipo?._links)
  const erroForm = erroJson(formSchema)
  const erroFluxo = erroJson(workflowJson)
  const preview = useMemo(() => schemaDe(formSchema), [formSchema])
  const grafo = useMemo(() => nosDoFluxo(workflowJson), [workflowJson])

  const publicar = useMutation({
    mutationFn: () => tiposSolicitacaoApi.publicar(tipo!.id, formSchema, workflowJson),
    onSuccess: () => client.invalidateQueries({ queryKey: ['admin-tipos-solicitacao'] }),
  })
  const criar = useMutation({
    mutationFn: () =>
      tiposSolicitacaoApi.criar({
        codigo,
        nome,
        formSchema,
        workflowJson,
      }),
    onSuccess: (criado) => {
      setSelecionado(criado.id)
      client.invalidateQueries({ queryKey: ['admin-tipos-solicitacao'] })
    },
  })
  const excluir = useMutation({
    mutationFn: (path: string) => tiposSolicitacaoApi.excluir(path),
    onSuccess: () => {
      setSelecionado(null)
      client.invalidateQueries({ queryKey: ['admin-tipos-solicitacao'] })
    },
  })

  function escolher(item: TipoEditor) {
    setSelecionado(item.id)
    setFormSchema(JSON.stringify(item.formSchema, null, 2))
    setWorkflowJson(JSON.stringify(item.workflowJson, null, 2))
  }

  const jsonInvalido = Boolean(erroForm || erroFluxo)

  return (
    <section className="page" aria-labelledby="tipos-titulo">
      <header className="page-head">
        <div>
          <h1 id="tipos-titulo">Tipos de solicitação</h1>
          <p className="muted">Preview e grafo acompanham o texto. Não há outro GET a cada tecla.</p>
        </div>
      </header>
      <div className="editor-3">
        <div>
          <h2>Tipos</h2>
          {lista.isLoading && <p className="muted">Carregando…</p>}
          {lista.isError && <p className="empty">Não foi possível carregar os tipos.</p>}
          <ul>
            {(lista.data?.content ?? []).map((item) => (
              <li key={item.id}>
                <button type="button" onClick={() => escolher(item)}>
                  {item.nome} <span className="badge">{item.status}</span>
                </button>
              </li>
            ))}
          </ul>
          <form
            onSubmit={(event) => {
              event.preventDefault()
              criar.mutate()
            }}
          >
            <label htmlFor="tipo-codigo">Código</label>
            <input id="tipo-codigo" value={codigo} onChange={(event) => setCodigo(event.target.value)} />
            <label htmlFor="tipo-nome">Nome</label>
            <input id="tipo-nome" value={nome} onChange={(event) => setNome(event.target.value)} />
            <button type="submit">Novo</button>
          </form>
        </div>
        <div>
          <h2>Editores</h2>
          <label htmlFor="editor-form">form_schema</label>
          <textarea
            id="editor-form"
            className={erroForm ? 'invalid' : undefined}
            rows={12}
            value={formSchema}
            onChange={(event) => setFormSchema(event.target.value)}
          />
          {erroForm && (
            <p className="banner danger" role="alert">
              {erroForm}
            </p>
          )}
          <label htmlFor="editor-fluxo">workflow_json</label>
          <textarea
            id="editor-fluxo"
            className={erroFluxo ? 'invalid' : undefined}
            rows={12}
            value={workflowJson}
            onChange={(event) => setWorkflowJson(event.target.value)}
          />
          {erroFluxo && (
            <p className="banner danger" role="alert">
              {erroFluxo}
            </p>
          )}
          {actions.can('publicar') && (
            <button type="button" disabled={jsonInvalido || publicar.isPending} onClick={() => publicar.mutate()}>
              Publicar
            </button>
          )}
          {actions.can('delete') && (
            <button type="button" className="danger" onClick={() => excluir.mutate(actions.href('delete')!)}>
              Excluir
            </button>
          )}
          {publicar.isError && (
            <p className="banner danger" role="alert">
              {publicar.error instanceof ApiError ? publicar.error.message : 'Falha ao publicar.'}
            </p>
          )}
        </div>
        <div>
          <h2>Preview</h2>
          {preview ? (
            <ul>
              {fieldsFromSchema(preview).map((campo) => (
                <li key={campo.name}>
                  {campo.title}
                  {campo.type === 'string' && (campo.maxLength ?? 0) > 200 ? ' (textarea)' : ''}
                </li>
              ))}
            </ul>
          ) : (
            <p className="muted">Schema inválido.</p>
          )}
          <h2>Workflow</h2>
          <ul aria-label="Grafo do workflow">
            {grafo.map((no) => (
              <li key={no.estado}>
                <strong>{no.estado}</strong>
                {no.arestas.map((aresta) => (
                  <span key={aresta}> → {aresta}</span>
                ))}
              </li>
            ))}
          </ul>
        </div>
      </div>
    </section>
  )
}

function nosDoFluxo(texto: string): { estado: string; arestas: string[] }[] {
  const schema = schemaDe(texto) as { states?: Record<string, { on?: Record<string, string> }> } | null
  if (!schema?.states) {
    return []
  }
  return Object.entries(schema.states).map(([estado, valor]) => ({
    estado,
    arestas: Object.entries(valor.on ?? {}).map(([acao, destino]) => `${acao} ${destino}`),
  }))
}
