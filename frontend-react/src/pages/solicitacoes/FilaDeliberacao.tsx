import { FormEvent, useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { ApiError } from '../../api/client'
import { solicitacoesApi } from '../../api/solicitacoes'
import { useActions } from '../../hooks/useActions'
import { rotuloCodigo } from '../../lib/rotulos'
import type { Solicitacao } from '../../models/solicitacao'

export function FilaDeliberacao() {
  const location = useLocation()
  const confirmacao = (location.state as { confirmacao?: string } | null)?.confirmacao
  const queryClient = useQueryClient()
  const [tipo, setTipo] = useState('')
  const [prazo, setPrazo] = useState('')
  const [selecionadas, setSelecionadas] = useState<string[]>([])
  const [parecer, setParecer] = useState('')
  const [acao, setAcao] = useState('DEFER')
  const [erroLote, setErroLote] = useState<string | null>(null)

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

  const fila = useActions(lista.data?._links)
  const forbidden =
    (lista.isError && lista.error instanceof ApiError && lista.error.status === 403) ||
    (catalogoTipos.isError &&
      catalogoTipos.error instanceof ApiError &&
      catalogoTipos.error.status === 403)

  const lote = useMutation({
    mutationFn: () =>
      solicitacoesApi.deliberarLote(fila.href('bulk-deliberate') ?? '', selecionadas, acao, parecer),
    onSuccess: async () => {
      setSelecionadas([])
      setParecer('')
      setErroLote(null)
      await queryClient.invalidateQueries({ queryKey: ['solicitacoes', 'deliberar'] })
    },
    onError: (error) => {
      setErroLote(error instanceof ApiError ? error.message : 'Falha ao deliberar o lote.')
    },
  })

  const tiposOpcoes = tiposDaFila(catalogoTipos.data?.content, tipo)

  function onFiltro(event: FormEvent) {
    event.preventDefault()
    void lista.refetch()
  }

  const colunas = fila.can('bulk-deliberate') ? 7 : 6

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
        <form className="filter-bar filter-bar-compact" onSubmit={onFiltro} aria-label="Filtros da fila">
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
              <option value="">Todos</option>
              <option value="atrasadas">Somente atrasadas</option>
            </select>
          </label>
          <button type="submit">Filtrar</button>
        </form>
      )}

      {erroLote && (
        <div className="banner danger" role="alert">
          {erroLote}
        </div>
      )}
      {fila.can('bulk-deliberate') && (
        <form
          className="panel"
          onSubmit={(event) => {
            event.preventDefault()
            lote.mutate()
          }}
        >
          <h2>Deliberar selecionadas</h2>
          <label>
            Ação
            <select value={acao} onChange={(event) => setAcao(event.target.value)}>
              <option value="DEFER">Deferir</option>
              <option value="INDEFER">Indeferir</option>
            </select>
          </label>
          <label>
            Parecer
            <textarea value={parecer} onChange={(event) => setParecer(event.target.value)} />
          </label>
          <button type="submit" disabled={lote.isPending || selecionadas.length === 0}>
            {lote.isPending ? 'Deliberando…' : 'Deliberar selecionadas'}
          </button>
        </form>
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
              {fila.can('bulk-deliberate') && <th scope="col">Seleção</th>}
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
                <td colSpan={colunas} className="tabela-vazio" role="status">
                  {prazo === 'atrasadas'
                    ? 'Nenhuma solicitação atrasada aguardando deliberação.'
                    : 'Nenhuma solicitação aguardando deliberação.'}
                </td>
              </tr>
            ) : (
              lista.data.content.map((item) => (
                <Linha
                  key={item.id}
                  item={item}
                  selecionavel={fila.can('bulk-deliberate')}
                  marcada={selecionadas.includes(item.id)}
                  onMarcar={(marcada) =>
                    setSelecionadas((atual) =>
                      marcada ? [...atual, item.id] : atual.filter((id) => id !== item.id),
                    )
                  }
                />
              ))
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

function Linha({
  item,
  selecionavel,
  marcada,
  onMarcar,
}: {
  item: Solicitacao
  selecionavel: boolean
  marcada: boolean
  onMarcar: (marcada: boolean) => void
}) {
  const actions = useActions(item._links)
  const prazo = new Date(item.prazoEm).toLocaleDateString('pt-BR')
  return (
    <tr>
      {selecionavel && (
        <td>
          <input
            type="checkbox"
            aria-label={`Selecionar ${item.protocolo}`}
            checked={marcada}
            onChange={(event) => onMarcar(event.target.checked)}
          />
        </td>
      )}
      <td>{item.protocolo}</td>
      <td>{item.solicitanteNome || '—'}</td>
      <td>{item.tipoNome}</td>
      <td className={item.prazoVencido ? 'sla-danger' : undefined}>{prazo}</td>
      <td className={item.sla === 'ATRASADO' ? 'sla-danger' : undefined}>
        {rotuloCodigo(item.sla)}
      </td>
      <td className="actions">
        {actions.can('deliberar') && (
          <Link to={`/solicitacoes/${item.id}/deliberar`} className="button-link ghost">
            Deliberar
          </Link>
        )}
        {!actions.can('deliberar') && actions.can('self') && (
          <Link to={`/solicitacoes/${item.id}/deliberar`} className="button-link ghost">
            Abrir
          </Link>
        )}
      </td>
    </tr>
  )
}
