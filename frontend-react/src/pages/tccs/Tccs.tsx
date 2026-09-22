import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { ApiError } from '../../api/client'
import { tccsApi } from '../../api/tccs'
import { useActions } from '../../hooks/useActions'
import { dataBr, rotuloEstadoTcc } from '../../lib/tcc'
import type { Tcc } from '../../models/tcc'

export function Tccs() {
  const [estado, setEstado] = useState('')
  const lista = useQuery({
    queryKey: ['tccs', 'me', estado],
    queryFn: () => tccsApi.listarMeus(estado),
  })
  const colecao = useActions(lista.data?._links)
  const forbidden = lista.isError && lista.error instanceof ApiError && lista.error.status === 403

  return (
    <section className="page">
      <header className="page-head">
        <div>
          <h1>TCCs</h1>
          <p className="muted">TCCs registrados pela secretaria. Envie a versão final quando a ação estiver disponível.</p>
        </div>
      </header>

      {colecao.can('novo') && (
        <p>
          <Link to="/tccs/nova">Novo TCC</Link>
        </p>
      )}

      <div className="filter-bar">
        <label>
          Situação
          <select value={estado} onChange={(event) => setEstado(event.target.value)} aria-label="Filtrar por situação">
            <option value="">Todas</option>
            <option value="EM_ELABORACAO">Em elaboração</option>
            <option value="SUBMETIDO">Submetido</option>
            <option value="CORRECOES_SOLICITADAS">Correções solicitadas</option>
            <option value="APROVADO">Aprovado</option>
            <option value="REPROVADO">Reprovado</option>
          </select>
        </label>
      </div>

      {forbidden && (
        <p className="empty" role="status">
          TCCs indisponíveis para esta sessão.
        </p>
      )}
      {lista.isError && !forbidden && (
        <div className="banner danger" role="alert">
          Não foi possível carregar os TCCs.{' '}
          <button type="button" onClick={() => lista.refetch()}>
            Tentar de novo
          </button>
        </div>
      )}
      {lista.isLoading && (
        <p className="muted" aria-busy="true">
          Carregando TCCs…
        </p>
      )}
      {lista.data && lista.data.content.length === 0 && (
        <p className="empty" role="status">
          {estado ? 'Nenhum TCC com essa situação.' : 'Nenhum TCC registrado. Consulte a secretaria.'}
        </p>
      )}
      {lista.data && lista.data.content.length > 0 && (
        <table>
          <thead>
            <tr>
              <th scope="col">Título</th>
              <th scope="col">Orientador</th>
              <th scope="col">Situação</th>
              <th scope="col">Defesa</th>
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
      <td>{item.titulo}</td>
      <td>{item.orientadorRotulo || '—'}</td>
      <td>
        <span className={`badge estado-${item.estado.toLowerCase()}`}>{rotuloEstadoTcc(item.estado)}</span>
      </td>
      <td>{dataBr(item.dataDefesa)}</td>
      <td className="actions">
        {actions.can('self') && (
          <Link to={`/tccs/${item.id}`} className="ghost-link">
            Abrir
          </Link>
        )}
      </td>
    </tr>
  )
}
