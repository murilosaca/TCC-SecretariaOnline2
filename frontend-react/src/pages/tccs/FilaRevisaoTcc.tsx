import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { ApiError } from '../../api/client'
import { tccsApi } from '../../api/tccs'
import { useActions } from '../../hooks/useActions'
import { rotuloEstadoTcc, rotuloPapelBanca } from '../../lib/tcc'
import type { Tcc } from '../../models/tcc'

export function FilaRevisaoTcc() {
  const lista = useQuery({
    queryKey: ['tccs', 'revisao'],
    queryFn: () => tccsApi.listarParaRevisao(),
  })
  const forbidden = lista.isError && lista.error instanceof ApiError && lista.error.status === 403

  return (
    <section className="page">
      <header className="page-head">
        <div>
          <h1>Revisão de TCCs</h1>
          <p className="muted">Nota e parecer um TCC por vez. Não há avaliação em lote.</p>
        </div>
      </header>

      {forbidden && (
        <p className="empty" role="status">
          Fila de revisão indisponível para esta sessão.
        </p>
      )}
      {lista.isError && !forbidden && (
        <div className="banner danger" role="alert">
          Não foi possível carregar a fila.{' '}
          <button type="button" onClick={() => lista.refetch()}>
            Tentar de novo
          </button>
        </div>
      )}
      {lista.isLoading && (
        <p className="muted" aria-busy="true">
          Carregando fila…
        </p>
      )}
      {lista.data && (
        <table>
          <thead>
            <tr>
              <th scope="col">Aluno</th>
              <th scope="col">Título</th>
              <th scope="col">Papel</th>
              <th scope="col">Situação</th>
              <th scope="col" className="col-acoes">
                Ações
              </th>
            </tr>
          </thead>
          <tbody>
            {lista.data.content.length === 0 ? (
              <tr>
                <td colSpan={5} className="tabela-vazio" role="status">
                  Nenhum TCC aguardando sua avaliação.
                </td>
              </tr>
            ) : (
              lista.data.content.map((item) => <Linha key={item.id} item={item} />)
            )}
          </tbody>
        </table>
      )}
    </section>
  )
}

function Linha({ item }: { item: Tcc }) {
  const actions = useActions(item._links)
  return (
    <tr>
      <td>{item.alunoNome || '—'}</td>
      <td>{item.titulo}</td>
      <td>{item.papel ? rotuloPapelBanca(item.papel) : '—'}</td>
      <td>
        <span className={`badge estado-${item.estado.toLowerCase()}`}>{rotuloEstadoTcc(item.estado)}</span>
      </td>
      <td className="col-acoes">
        <div className="actions actions-centered">
          {actions.can('avaliar') && (
            <Link to={`/tccs/${item.id}`} className="button-link ghost">
              Avaliar
            </Link>
          )}
          {!actions.can('avaliar') && actions.can('self') && (
            <Link to={`/tccs/${item.id}`} className="button-link ghost">
              Detalhes
            </Link>
          )}
        </div>
      </td>
    </tr>
  )
}
