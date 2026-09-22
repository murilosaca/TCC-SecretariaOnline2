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
      {lista.data && lista.data.content.length === 0 && (
        <p className="empty" role="status">
          Nenhum TCC aguardando sua avaliação.
        </p>
      )}
      {lista.data && lista.data.content.length > 0 && (
        <table>
          <thead>
            <tr>
              <th scope="col">Aluno</th>
              <th scope="col">Título</th>
              <th scope="col">Papel</th>
              <th scope="col">Situação</th>
              <th scope="col">Ações</th>
            </tr>
          </thead>
          <tbody>
            {lista.data.content.map((item) => (
              <Linha key={item.id} item={item} />
            ))}
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
      <td className="actions">
        {actions.can('avaliar') && (
          <Link to={`/tccs/${item.id}`} className="ghost-link">
            Avaliar
          </Link>
        )}
        {!actions.can('avaliar') && actions.can('self') && (
          <Link to={`/tccs/${item.id}`} className="ghost-link">
            Abrir
          </Link>
        )}
      </td>
    </tr>
  )
}
