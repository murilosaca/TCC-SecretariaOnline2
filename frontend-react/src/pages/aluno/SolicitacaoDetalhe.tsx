import { Link, useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { solicitacoesApi } from '../../api/solicitacoes'
import { useActions } from '../../hooks/useActions'
import { fieldsFromSchema } from '../../lib/formSchema'
import { rotuloCodigo } from '../../lib/rotulos'

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
  const camposPayload =
    campos.length > 0
      ? campos
      : Object.keys(item?.payload ?? {}).map((name) => ({ name, title: name }))
  const camposFormulario = [
    ...camposPayload.filter((campo) => campo.name !== 'observacao'),
    ...camposPayload.filter((campo) => campo.name === 'observacao'),
  ]

  return (
    <section className="page detalhe">
      <p>
        <Link to="/solicitacoes" className="ghost-link">
          ← Solicitações
        </Link>
      </p>

      {detalhe.isLoading && (
        <p className="muted" aria-busy="true">
          Carregando solicitação…
        </p>
      )}
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
            <h1>{item.protocolo}</h1>
            <span className={`badge estado-${item.estado.toLowerCase()}`}>
              {rotuloCodigo(item.estado)}
            </span>
          </header>

          {(actions.can('editar') || actions.can('gerar-protocolo') || actions.can('deliberar')) && (
            <div className="action-bar">
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
                <Link to={`/solicitacoes/${item.id}/deliberar`} className="button-link">
                  Deliberar
                </Link>
              )}
            </div>
          )}

          <article className="panel solicitacao-detalhes">
            <h2>Detalhes</h2>
            <dl className="resumo resumo-horizontal">
              <div>
                <dt>Tipo</dt>
                <dd>{item.tipoNome}</dd>
              </div>
              <div>
                <dt>Aberta em</dt>
                <dd>{new Date(item.createdAt).toLocaleString('pt-BR')}</dd>
              </div>
              <div>
                <dt>Prazo</dt>
                <dd className={item.prazoVencido ? 'sla-danger' : undefined}>
                  {new Date(item.prazoEm).toLocaleString('pt-BR')}
                </dd>
              </div>
              <div>
                <dt>SLA</dt>
                <dd className={item.sla === 'ATRASADO' ? 'sla-danger' : undefined}>
                  {rotuloCodigo(item.sla)}
                </dd>
              </div>
              <div>
                <dt>Estado</dt>
                <dd>{rotuloCodigo(item.estado)}</dd>
              </div>
            </dl>
            {camposFormulario.length > 0 && (
              <dl className="resumo resumo-horizontal solicitacao-detalhes-formulario">
                {camposFormulario.map((campo) => (
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
            )}
          </article>

          <section className="estagio-secao" aria-labelledby="timeline-titulo">
            <h2 id="timeline-titulo">Linha do tempo</h2>
            <div className="estagio-tabela-wrap estagio-tabela-wrap-scroll">
              <table className="estagio-tabela">
                <thead>
                  <tr>
                    <th scope="col">Evento</th>
                    <th scope="col">Transição</th>
                    <th scope="col">Data</th>
                    <th scope="col">Parecer</th>
                  </tr>
                </thead>
                <tbody>
                  {item.eventos.length === 0 ? (
                    <tr>
                      <td colSpan={4} className="tabela-vazio" role="status">
                        Nenhum evento registrado ainda.
                      </td>
                    </tr>
                  ) : (
                    item.eventos.map((evento) => (
                      <tr key={evento.id}>
                        <td>{rotuloCodigo(evento.tipo)}</td>
                        <td>
                          {evento.estadoDe
                            ? `${rotuloCodigo(evento.estadoDe)} → ${rotuloCodigo(evento.estadoPara)}`
                            : rotuloCodigo(evento.estadoPara)}
                        </td>
                        <td>{new Date(evento.createdAt).toLocaleString('pt-BR')}</td>
                        <td>{evento.parecer || '—'}</td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </section>
        </>
      )}
    </section>
  )
}
