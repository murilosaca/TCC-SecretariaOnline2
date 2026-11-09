import { useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { ApiError } from '../../api/client'
import { solicitacoesApi } from '../../api/solicitacoes'
import { useActions } from '../../hooks/useActions'
import { rotuloCodigo } from '../../lib/rotulos'
import type { Solicitacao } from '../../models/solicitacao'

const PRAZO_OPCOES = [
  { value: '', label: 'Todos' },
  { value: 'atrasadas', label: 'Somente atrasadas' },
] as const

export function FilaDeliberacao() {
  const location = useLocation()
  const confirmacao = (location.state as { confirmacao?: string } | null)?.confirmacao
  const [tipo, setTipo] = useState('')
  const [prazo, setPrazo] = useState('')

  const catalogoTipos = useQuery({
    queryKey: ['solicitacoes', 'deliberar', 'catalogo-tipos'],
    queryFn: () => solicitacoesApi.listarParaDeliberar({ size: 100 }),
    retry: false,
  })

  const lista = useQuery({
    queryKey: ['solicitacoes', 'deliberar', tipo, prazo],
    queryFn: () =>
      solicitacoesApi.listarParaDeliberar({
        tipo: tipo || undefined,
        atraso: prazo === 'atrasadas' || undefined,
      }),
  })

  const forbidden =
    (lista.isError && lista.error instanceof ApiError && lista.error.status === 403) ||
    (catalogoTipos.isError &&
      catalogoTipos.error instanceof ApiError &&
      catalogoTipos.error.status === 403)

  const tiposOpcoes = tiposDaFila(catalogoTipos.data?.content, tipo)

  return (
    <section className="page">
      <header className="page-head">
        <div>
          <h1>Fila de deliberação</h1>
          <p className="muted">
            Solicitações que aguardam sua decisão, com prazo e indicador de SLA. Use os filtros para
            priorizar por tipo ou atrasos.
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
        <div className="filter-bar filter-bar-compact" aria-label="Filtros da fila">
          <label>
            Tipo
            <select
              value={tipo}
              aria-label="Filtrar por tipo"
              onChange={(event) => setTipo(event.target.value)}
            >
              <option value="">Todos</option>
              {tiposOpcoes.map((item) => (
                <option key={item.codigo} value={item.codigo}>
                  {item.nome}
                </option>
              ))}
            </select>
          </label>
          <label>
            Prazo
            <select
              value={prazo}
              aria-label="Filtrar por prazo"
              onChange={(event) => setPrazo(event.target.value)}
            >
              {PRAZO_OPCOES.map((opcao) => (
                <option key={opcao.value || 'todos'} value={opcao.value}>
                  {opcao.label}
                </option>
              ))}
            </select>
          </label>
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
              <th scope="col">Número</th>
              <th scope="col">Aluno</th>
              <th scope="col">Tipo</th>
              <th scope="col">Prazo</th>
              <th scope="col">SLA</th>
              <th scope="col">Ações</th>
            </tr>
          </thead>
          <tbody>
            {lista.data.content.length === 0 ? (
              <tr>
                <td colSpan={6} className="tabela-vazio" role="status">
                  {prazo === 'atrasadas'
                    ? 'Nenhuma solicitação atrasada aguardando deliberação.'
                    : 'Nenhuma solicitação aguardando deliberação.'}
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

function tiposDaFila(
  itens: Solicitacao[] | undefined,
  tipoSelecionado: string,
): { codigo: string; nome: string }[] {
  const map = new Map<string, string>()
  for (const item of itens ?? []) {
    if (!item.tipoCodigo) continue
    map.set(item.tipoCodigo, item.tipoNome || rotuloCodigo(item.tipoCodigo))
  }
  if (tipoSelecionado && !map.has(tipoSelecionado)) {
    map.set(tipoSelecionado, rotuloCodigo(tipoSelecionado))
  }
  return [...map.entries()]
    .map(([codigo, nome]) => ({ codigo, nome }))
    .sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR'))
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
      <td className={item.sla === 'ATRASADO' ? 'sla-danger' : undefined}>
        {rotuloCodigo(item.sla)}
      </td>
      <td className="actions">
        {actions.can('deliberar') && (
          <Link to={`/solicitacoes/${item.id}/deliberar`} className="button-link">
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
