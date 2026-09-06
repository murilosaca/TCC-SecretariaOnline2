import { Link, useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { solicitacoesApi } from '../../api/solicitacoes'
import { useActions } from '../../hooks/useActions'
import { fieldsFromSchema } from '../../lib/formSchema'

export function SolicitacaoDetalhe() {
  const { id } = useParams<{ id: string }>()
  const detalhe = useQuery({
    queryKey: ['solicitacao', id],
    queryFn: () => solicitacoesApi.obter(id as string),
    enabled: Boolean(id),
  })

  const actions = useActions(detalhe.data?._links)
  const item = detalhe.data
  const campos = fieldsFromSchema(item?.formSchema)

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
                <Link to="/solicitacoes/nova" className="button-link">
                  Editar
                </Link>
              )}
              {actions.can('gerar-protocolo') && (
                <button type="button" disabled>
                  Gerar protocolo
                </button>
              )}
              {actions.can('deliberar') && (
                <button type="button" disabled>
                  Deliberar
                </button>
              )}
            </div>
          </header>

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
