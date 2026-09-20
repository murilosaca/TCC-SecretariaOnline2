import { Link, useLocation } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { ApiError } from '../../api/client'
import { formativasApi } from '../../api/formativas'
import { useActions } from '../../hooks/useActions'
import { rotuloEstadoFormativa } from '../../lib/formativa'
import type { Formativa } from '../../models/formativa'

export function FilaRevisao() {
  const location = useLocation()
  const confirmacao = (location.state as { confirmacao?: string } | null)?.confirmacao

  const lista = useQuery({
    queryKey: ['formativas', 'revisao'],
    queryFn: () => formativasApi.listarParaRevisao(),
  })

  const forbidden = lista.isError && lista.error instanceof ApiError && lista.error.status === 403

  return (
    <section className="page">
      <header className="page-head">
        <div>
          <h1>Revisão de formativas</h1>
          <p className="muted">
            Fila da CAAF: um item por vez, com parecer. Os botões só aparecem quando a ação é
            permitida no estado atual.
          </p>
        </div>
      </header>

      {confirmacao && (
        <div className="banner success" role="status">
          {confirmacao}
        </div>
      )}

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
          Nenhuma formativa aguardando revisão da CAAF.
        </p>
      )}
      {lista.data && lista.data.content.length > 0 && (
        <table>
          <thead>
            <tr>
              <th scope="col">Aluno</th>
              <th scope="col">Atividade</th>
              <th scope="col">Horas</th>
              <th scope="col">Estado</th>
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

function Linha({ item }: { item: Formativa }) {
  const actions = useActions(item._links)
  return (
    <tr>
      <td>{item.alunoNome || '—'}</td>
      <td>{item.titulo}</td>
      <td>{item.cargaHoraria}h</td>
      <td>
        <span className={`badge estado-${item.estado.toLowerCase()}`}>{rotuloEstadoFormativa(item.estado)}</span>
      </td>
      <td className="actions">
        {actions.can('revisar') && (
          <Link to={`/formativas/${item.id}/revisar`} className="ghost-link">
            Revisar
          </Link>
        )}
        {!actions.can('revisar') && actions.can('self') && (
          <Link to={`/formativas/${item.id}/revisar`} className="ghost-link">
            Abrir
          </Link>
        )}
      </td>
    </tr>
  )
}
