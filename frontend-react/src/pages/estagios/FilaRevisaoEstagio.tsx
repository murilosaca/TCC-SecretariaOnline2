import { Link, useLocation } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { ApiError } from '../../api/client'
import { estagiosApi } from '../../api/estagios'
import { useActions } from '../../hooks/useActions'
import { rotuloTipoDocumento } from '../../lib/estagio'
import type { Estagio } from '../../models/estagio'

export function FilaRevisaoEstagio() {
  const location = useLocation()
  const confirmacao = (location.state as { confirmacao?: string } | null)?.confirmacao
  const lista = useQuery({
    queryKey: ['estagios', 'revisao'],
    queryFn: () => estagiosApi.listarParaRevisao(),
  })
  const forbidden = lista.isError && lista.error instanceof ApiError && lista.error.status === 403

  return (
    <section className="page">
      <header className="page-head">
        <div>
          <h1>Revisão de estágios</h1>
          <p className="muted">Parecer um documento por vez. Não há aprovação em lote.</p>
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
          Nenhum estágio aguardando sua revisão.
        </p>
      )}
      {lista.data && lista.data.content.length > 0 && (
        <table>
          <thead>
            <tr>
              <th scope="col">Aluno</th>
              <th scope="col">Empresa</th>
              <th scope="col">Documento pendente</th>
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

function Linha({ item }: { item: Estagio }) {
  const actions = useActions(item._links)
  return (
    <tr>
      <td>{item.alunoNome || '—'}</td>
      <td>{item.empresa}</td>
      <td>{item.documentoPendente ? rotuloTipoDocumento(item.documentoPendente) : '—'}</td>
      <td className="actions">
        {actions.can('revisar') && (
          <Link to={`/estagios/${item.id}`} className="ghost-link">
            Revisar
          </Link>
        )}
        {!actions.can('revisar') && actions.can('self') && (
          <Link to={`/estagios/${item.id}`} className="ghost-link">
            Abrir
          </Link>
        )}
      </td>
    </tr>
  )
}
