import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { ApiError } from '../../api/client'
import { solicitacoesApi } from '../../api/solicitacoes'
import { DynamicForm } from '../../components/DynamicForm'
import { useActions } from '../../hooks/useActions'
import { fieldsFromSchema, validateAgainstSchema } from '../../lib/formSchema'

export function SolicitacaoDetalhe() {
  const { id } = useParams<{ id: string }>()
  const detalhe = useQuery({
    queryKey: ['solicitacao', id],
    queryFn: () => solicitacoesApi.obter(id as string),
    enabled: Boolean(id),
  })

  const queryClient = useQueryClient()
  const actions = useActions(detalhe.data?._links)
  const item = detalhe.data
  const campos = fieldsFromSchema(item?.formSchema)
  const [editando, setEditando] = useState(false)
  const [valores, setValores] = useState<Record<string, unknown>>({})
  const [errosForm, setErrosForm] = useState<Record<string, string>>({})
  const [erroAcao, setErroAcao] = useState<string | null>(null)

  const salvar = useMutation({
    mutationFn: () => solicitacoesApi.editarPayload(actions.href('editar') ?? '', valores),
    onSuccess: (atual) => {
      setEditando(false)
      setErroAcao(null)
      queryClient.setQueryData(['solicitacao', id], atual)
    },
    onError: (error) => {
      setErroAcao(error instanceof ApiError ? error.message : 'Falha ao salvar o formulário.')
    },
  })
  const reenviar = useMutation({
    mutationFn: () => solicitacoesApi.reenviar(actions.href('reenviar') ?? ''),
    onSuccess: (atual) => {
      setErroAcao(null)
      queryClient.setQueryData(['solicitacao', id], atual)
    },
    onError: (error) => {
      setErroAcao(error instanceof ApiError ? error.message : 'Falha ao reenviar a solicitação.')
    },
  })
  const protocolo = useMutation({
    mutationFn: () => solicitacoesApi.gerarProtocolo(actions.href('gerar-protocolo') ?? ''),
    onSuccess: (emitido) => {
      setErroAcao(null)
      window.location.assign(emitido.downloadUrl)
    },
    onError: (error) => {
      setErroAcao(error instanceof ApiError ? error.message : 'Falha ao gerar o protocolo.')
    },
  })

  function abrirEdicao() {
    setValores({ ...(item?.payload ?? {}) })
    setErrosForm({})
    setErroAcao(null)
    setEditando(true)
  }

  function confirmarEdicao() {
    if (!item?.formSchema) {
      return
    }
    const atuais = validateAgainstSchema(item.formSchema, valores)
    setErrosForm(atuais)
    if (Object.keys(atuais).length === 0) {
      salvar.mutate()
    }
  }

  return (
    <section className="page detalhe">
      <p>
        <Link to="/solicitacoes" className="ghost-link">
          ← Solicitações
        </Link>
      </p>

      {detalhe.isLoading && <p className="muted">Carregando solicitação…</p>}
      {detalhe.isError && (
        <div className="banner danger" role="alert">
          Solicitação não encontrada ou indisponível.{' '}
          <button type="button" onClick={() => detalhe.refetch()}>
            Tentar de novo
          </button>
        </div>
      )}

      {item && (
        <>
          <header className="page-head">
            <div>
              <h1>{item.protocolo}</h1>
              <p className="muted">{item.tipoNome}</p>
            </div>
            <div className="action-bar">
              <span className={`badge estado-${item.estado.toLowerCase()}`}>{item.estado}</span>
              {actions.can('editar') && (
                <button type="button" onClick={abrirEdicao}>
                  Editar
                </button>
              )}
              {actions.can('reenviar') && (
                <button type="button" disabled={reenviar.isPending} onClick={() => reenviar.mutate()}>
                  {reenviar.isPending ? 'Reenviando…' : 'Reenviar'}
                </button>
              )}
              {actions.can('gerar-protocolo') && (
                <button type="button" disabled={protocolo.isPending} onClick={() => protocolo.mutate()}>
                  {protocolo.isPending ? 'Gerando protocolo…' : 'Gerar protocolo'}
                </button>
              )}
              {actions.can('deliberar') && (
                <Link to={`/solicitacoes/${item.id}/deliberar`} className="button-link">
                  Deliberar
                </Link>
              )}
            </div>
          </header>

          {erroAcao && (
            <div className="banner danger" role="alert">
              {erroAcao}
            </div>
          )}

          {editando && item.formSchema && (
            <div className="panel">
              <h2>Editar formulário</h2>
              <DynamicForm
                fields={campos}
                values={valores}
                errors={errosForm}
                onChange={(name, value) => setValores((atual) => ({ ...atual, [name]: value }))}
              />
              <div className="wizard-actions">
                <button type="button" disabled={salvar.isPending} onClick={confirmarEdicao}>
                  {salvar.isPending ? 'Salvando…' : 'Salvar'}
                </button>
                <button type="button" className="ghost" onClick={() => setEditando(false)}>
                  Cancelar
                </button>
              </div>
            </div>
          )}

          <div className="detalhe-grid">
            <article className="panel">
              <h2>Dados</h2>
              <dl className="resumo">
                {(campos.length > 0 ? campos : Object.keys(item.payload).map((name) => ({
                  name,
                  title: name,
                }))).map((campo) => (
                  <div key={campo.name}>
                    <dt>{campo.title}</dt>
                    <dd>
                      {item.payload[campo.name] == null || item.payload[campo.name] === ''
                        ? '—'
                        : String(item.payload[campo.name])}
                    </dd>
                  </div>
                ))}
              </dl>
            </article>
            <aside className="panel">
              <h2>Metadados</h2>
              <p>
                <strong>Prazo:</strong>{' '}
                <span className={item.prazoVencido ? 'sla-danger' : undefined}>
                  {new Date(item.prazoEm).toLocaleString('pt-BR')}
                </span>
              </p>
              <p>
                <strong>SLA:</strong> {item.sla}
              </p>
              <p>
                <strong>Aberta em:</strong> {new Date(item.createdAt).toLocaleString('pt-BR')}
              </p>
            </aside>
          </div>

          {(item.anexos?.length ?? 0) > 0 && (
            <section className="panel">
              <h2>Anexos</h2>
              <ul>
                {item.anexos?.map((anexo) => (
                  <li key={anexo.sha256}>
                    <a href={anexo.downloadUrl}>{anexo.nome}</a>
                  </li>
                ))}
              </ul>
            </section>
          )}

          <section className="panel">
            <h2>Linha do tempo</h2>
            {item.eventos.length === 0 && <p className="empty">Nenhum evento registrado.</p>}
            {item.eventos.length > 0 && (
              <ol className="timeline">
                {item.eventos.map((evento) => (
                  <li key={evento.id}>
                    <strong>{evento.tipo}</strong>
                    <span className="muted">
                      {' '}
                      {evento.estadoDe ? `${evento.estadoDe} → ` : ''}
                      {evento.estadoPara}
                    </span>
                    <div className="muted">{new Date(evento.createdAt).toLocaleString('pt-BR')}</div>
                    {evento.parecer && <p>{evento.parecer}</p>}
                  </li>
                ))}
              </ol>
            )}
          </section>
        </>
      )}
    </section>
  )
}
