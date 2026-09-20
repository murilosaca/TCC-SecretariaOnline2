import { FormEvent, useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { ApiError } from '../../api/client'
import { solicitacoesApi } from '../../api/solicitacoes'
import { useActions } from '../../hooks/useActions'
import type { Solicitacao } from '../../models/solicitacao'

export function FilaDeliberacao() {
  const location = useLocation()
  const confirmacao = (location.state as { confirmacao?: string } | null)?.confirmacao
  const [tipo, setTipo] = useState('')
  const [atraso, setAtraso] = useState(false)

  const lista = useQuery({
    queryKey: ['solicitacoes', 'deliberar', tipo, atraso],
    queryFn: () =>
      solicitacoesApi.listarParaDeliberar({
        tipo: tipo || undefined,
        atraso: atraso || undefined,
      }),
  })

  const forbidden = lista.isError && lista.error instanceof ApiError && lista.error.status === 403

  function onFiltro(event: FormEvent) {
    event.preventDefault()
    void lista.refetch()
  }

  return (
    <section className="page">
      <header className="page-head">
        <div>
          <h1>Fila de deliberação</h1>
          <p className="muted">
            Pedidos que aguardam sua decisão, com prazo e SLA. Os botões só aparecem quando a ação é
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
          Fila de deliberação indisponível para esta sessão.
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

      {!forbidden && (
        <form className="filter-bar" onSubmit={onFiltro}>
          <label>
            Tipo
            <select value={tipo} onChange={(event) => setTipo(event.target.value)}>
              <option value="">Todos</option>
              <option value="DECLARACAO_SIMPLES">Declaração simples</option>
            </select>
          </label>
          <label className="checkbox-row">
            <input
              type="checkbox"
              checked={atraso}
              onChange={(event) => setAtraso(event.target.checked)}
            />
            Somente atrasadas
          </label>
          <button type="submit">Filtrar</button>
        </form>
      )}

      {lista.isLoading && (
        <p className="muted" aria-busy="true">
          Carregando fila…
        </p>
      )}
      {lista.data && lista.data.content.length === 0 && (
        <p className="empty" role="status">
          Nenhuma solicitação aguardando deliberação.
        </p>
      )}
      {lista.data && lista.data.content.length > 0 && (
        <table>
          <thead>
            <tr>
              <th scope="col">Número</th>
              <th scope="col">Aluno</th>
              <th scope="col">Tipo</th>
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
      <td>{item.solicitanteNome || '—'}</td>
      <td>{item.tipoNome}</td>
      <td className={item.prazoVencido ? 'sla-danger' : undefined}>{prazo}</td>
      <td className={item.sla === 'ATRASADO' ? 'sla-danger' : undefined}>{item.sla}</td>
      <td className="actions">
        {actions.can('deliberar') && (
          <Link to={`/solicitacoes/${item.id}/deliberar`} className="ghost-link">
            Deliberar
          </Link>
        )}
        {!actions.can('deliberar') && actions.can('self') && (
          <Link to={`/solicitacoes/${item.id}/deliberar`} className="ghost-link">
            Abrir
          </Link>
        )}
      </td>
    </tr>
  )
}
