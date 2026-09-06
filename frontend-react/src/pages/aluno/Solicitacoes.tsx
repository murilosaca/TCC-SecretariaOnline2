import { FormEvent, useState } from 'react'
import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { solicitacoesApi } from '../../api/solicitacoes'
import { useActions } from '../../hooks/useActions'
import type { Solicitacao } from '../../models/solicitacao'

export function Solicitacoes() {
  const [estado, setEstado] = useState('')
  const [ano, setAno] = useState('')

  const lista = useQuery({
    queryKey: ['solicitacoes', estado, ano],
    queryFn: () =>
      solicitacoesApi.listarMinhas({
        estado: estado || undefined,
        ano: ano ? Number(ano) : undefined,
      }),
  })

  const actions = useActions(lista.data?._links)

  function onFiltro(event: FormEvent) {
    event.preventDefault()
    void lista.refetch()
  }

  return (
    <section className="page">
      <header className="page-head">
        <div>
          <h1>Minhas solicitações</h1>
          <p className="muted">Acompanhe o andamento e o prazo de cada pedido (RF-F1-005-a).</p>
        </div>
        {actions.can('novaSolicitacao') && (
          <Link to="/solicitacoes/nova" className="button-link">
            Nova solicitação
          </Link>
        )}
      </header>

      {lista.isError && (
        <div className="banner danger" role="alert">
          Não foi possível carregar as solicitações.{' '}
          <button type="button" onClick={() => lista.refetch()}>
            Tentar de novo
          </button>
        </div>
      )}

      <form className="filter-bar" onSubmit={onFiltro}>
        <label>
          Estado
          <select value={estado} onChange={(event) => setEstado(event.target.value)}>
            <option value="">Todos</option>
            <option value="EM_ANALISE">Em análise</option>
            <option value="EM_AJUSTE">Em ajuste</option>
            <option value="DELIBERADA">Deliberada</option>
            <option value="INDEFERIDA">Indeferida</option>
            <option value="CONCLUIDA">Concluída</option>
          </select>
        </label>
        <label>
          Ano
          <input
            type="number"
            min="2000"
            max="2100"
            placeholder="2026"
            value={ano}
            onChange={(event) => setAno(event.target.value)}
          />
        </label>
        <button type="submit">Filtrar</button>
      </form>

      {lista.isLoading && (
        <p className="muted" aria-busy="true">
          Carregando solicitações…
        </p>
      )}
      {lista.data && lista.data.content.length === 0 && (
        <p className="empty">Nenhuma solicitação encontrada.</p>
      )}
      {lista.data && lista.data.content.length > 0 && (
        <table>
          <thead>
            <tr>
              <th scope="col">Número</th>
              <th scope="col">Tipo</th>
              <th scope="col">Estado</th>
              <th scope="col">Prazo</th>
              <th scope="col">SLA</th>
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

function Linha({ item }: { item: Solicitacao }) {
  const actions = useActions(item._links)
  const prazo = new Date(item.prazoEm).toLocaleDateString('pt-BR')
  return (
    <tr>
      <td>{item.protocolo}</td>
      <td>{item.tipoNome}</td>
      <td>
        <span className={`badge estado-${item.estado.toLowerCase()}`}>{item.estado}</span>
      </td>
      <td className={item.prazoVencido ? 'sla-danger' : undefined}>{prazo}</td>
      <td className={item.sla === 'ATRASADO' ? 'sla-danger' : undefined}>{item.sla}</td>
      <td className="actions">
        {actions.can('self') && (
          <Link to={`/solicitacoes/${item.id}`} className="ghost-link">
            Abrir
          </Link>
        )}
        {actions.can('deliberar') && (
          <Link to={`/solicitacoes/${item.id}`} className="ghost-link">
            Deliberar
          </Link>
        )}
      </td>
    </tr>
  )
}
