import { useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { ApiError } from '../../api/client'
import { solicitacoesApi } from '../../api/solicitacoes'
import { useActions } from '../../hooks/useActions'
import type { Solicitacao } from '../../models/solicitacao'

export function Atrasados() {
  const [params, setParams] = useSearchParams()
  const page = Number(params.get('page') ?? '0') || 0
  const [exportando, setExportando] = useState(false)
  const [erroExport, setErroExport] = useState<string | null>(null)

  const lista = useQuery({
    queryKey: ['solicitacoes', 'atrasados', page],
    queryFn: () =>
      solicitacoesApi.listarFilaCurso({
        slaBreached: true,
        estado: 'TODOS',
        page,
      }),
  })

  const forbidden = lista.isError && lista.error instanceof ApiError && lista.error.status === 403

  async function exportarCsv() {
    setExportando(true)
    setErroExport(null)
    try {
      const blob = await solicitacoesApi.exportarAtrasadosCsv({ page: 0, size: 100 })
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `atrasados-${new Date().toISOString().slice(0, 10)}.csv`
      a.click()
      URL.revokeObjectURL(url)
    } catch (erro) {
      setErroExport(erro instanceof ApiError ? erro.message : 'Não foi possível exportar o CSV.')
    } finally {
      setExportando(false)
    }
  }

  return (
    <section className="page" aria-labelledby="atrasados-titulo">
      <header className="page-head">
        <div>
          <h1 id="atrasados-titulo">Solicitações atrasadas</h1>
          <p className="muted">SLA vencido nos cursos vinculados (F5.5).</p>
        </div>
        <button type="button" onClick={() => void exportarCsv()} disabled={exportando || forbidden}>
          {exportando ? 'Exportando…' : 'Exportar'}
        </button>
      </header>

      {forbidden && (
        <p className="empty" role="status">
          Lista de atrasados indisponível para esta sessão.
        </p>
      )}
      {lista.isError && !forbidden && (
        <div className="banner danger" role="alert">
          Não foi possível carregar os atrasados.{' '}
          <button type="button" onClick={() => lista.refetch()}>
            Tentar de novo
          </button>
        </div>
      )}
      {erroExport && (
        <div className="banner danger" role="alert">
          {erroExport}
        </div>
      )}
      {lista.data && lista.data.page.totalElements > 100 && (
        <p className="muted">O CSV traz só os 100 primeiros do filtro.</p>
      )}

      {lista.isLoading && (
        <p className="muted" aria-busy="true">
          Carregando atrasados…
        </p>
      )}
      {lista.data && lista.data.content.length === 0 && (
        <p className="empty" role="status">
          Nenhuma solicitação atrasada
        </p>
      )}
      {lista.data && lista.data.content.length > 0 && (
        <table className="data-table">
          <thead>
            <tr>
              <th scope="col">Número</th>
              <th scope="col">Aluno</th>
              <th scope="col">Tipo</th>
              <th scope="col">Estado</th>
              <th scope="col">Deliberador</th>
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

      {lista.data && lista.data.page.totalPages > 1 && (
        <nav className="pagination" aria-label="Paginação de atrasados">
          <button
            type="button"
            disabled={page <= 0}
            onClick={() => {
              const next = new URLSearchParams(params)
              next.set('page', String(page - 1))
              setParams(next)
            }}
          >
            Anterior
          </button>
          <span className="muted">
            Página {page + 1} de {lista.data.page.totalPages}
          </span>
          <button
            type="button"
            disabled={page + 1 >= lista.data.page.totalPages}
            onClick={() => {
              const next = new URLSearchParams(params)
              next.set('page', String(page + 1))
              setParams(next)
            }}
          >
            Próxima
          </button>
        </nav>
      )}
    </section>
  )
}

function Linha({ item }: { item: Solicitacao }) {
  const actions = useActions(item._links)
  return (
    <tr>
      <td>{item.protocolo}</td>
      <td>{item.solicitanteNome || '—'}</td>
      <td>{item.tipoNome}</td>
      <td>{item.estado}</td>
      <td>{item.deliberadorNome || '—'}</td>
      <td className="sla-danger">
        Atrasado{item.diasAtraso != null ? ` (${item.diasAtraso}d)` : ''}
      </td>
      <td className="actions">
        {actions.can('deliberar') && (
          <Link to={`/solicitacoes/${item.id}/deliberar`} className="ghost-link">
            Deliberar
          </Link>
        )}
      </td>
    </tr>
  )
}
