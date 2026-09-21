import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { ApiError } from '../../api/client'
import { estagiosApi } from '../../api/estagios'
import { useActions } from '../../hooks/useActions'
import { rotuloSituacaoEstagio, vigencia } from '../../lib/estagio'
import type { Estagio } from '../../models/estagio'

export function Estagios() {
  const [situacao, setSituacao] = useState('')
  const lista = useQuery({
    queryKey: ['estagios', 'me', situacao],
    queryFn: () => estagiosApi.listarMeus(situacao),
  })
  const colecao = useActions(lista.data?._links)
  const forbidden = lista.isError && lista.error instanceof ApiError && lista.error.status === 403

  return (
    <section className="page">
      <header className="page-head">
        <div>
          <h1>Estágios</h1>
          <p className="muted">Estágios registrados pela secretaria. Envie os documentos quando a ação estiver disponível.</p>
        </div>
      </header>

      {colecao.can('novo') && (
        <p>
          <Link to="/estagios/nova">Novo estágio</Link>
        </p>
      )}

      <div className="filter-bar">
        <label>
          Situação
          <select
            value={situacao}
            onChange={(event) => setSituacao(event.target.value)}
            aria-label="Filtrar por situação"
          >
            <option value="">Todas</option>
            <option value="ATIVO">Ativo</option>
            <option value="CONCLUIDO">Concluído</option>
            <option value="PENDENTE">Pendente</option>
          </select>
        </label>
      </div>

      {forbidden && (
        <p className="empty" role="status">
          Estágios indisponíveis para esta sessão.
        </p>
      )}
      {lista.isError && !forbidden && (
        <div className="banner danger" role="alert">
          Não foi possível carregar os estágios.{' '}
          <button type="button" onClick={() => lista.refetch()}>
            Tentar de novo
          </button>
        </div>
      )}
      {lista.isLoading && (
        <p className="muted" aria-busy="true">
          Carregando estágios…
        </p>
      )}
      {lista.data && lista.data.content.length === 0 && (
        <p className="empty" role="status">
          {situacao
            ? 'Nenhum estágio com essa situação.'
            : 'Você não possui estágios registrados.'}
        </p>
      )}
      {lista.data && lista.data.content.length > 0 && (
        <table>
          <thead>
            <tr>
              <th scope="col">Empresa</th>
              <th scope="col">Supervisor</th>
              <th scope="col">Vigência</th>
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

function Linha({ item }: { item: Estagio }) {
  const actions = useActions(item._links)
  return (
    <tr>
      <td>{item.empresa}</td>
      <td>{item.supervisor}</td>
      <td>{vigencia(item.inicio, item.fim)}</td>
      <td>
        <span className={`badge estado-${item.situacao.toLowerCase()}`}>{rotuloSituacaoEstagio(item.situacao)}</span>
      </td>
      <td className="actions">
        {actions.can('self') && (
          <Link to={`/estagios/${item.id}`} className="ghost-link">
            Abrir
          </Link>
        )}
      </td>
    </tr>
  )
}
