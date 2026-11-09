import { FormEvent, useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { ApiError } from '../../api/client'
import { solicitacoesApi } from '../../api/solicitacoes'
import { useActions } from '../../hooks/useActions'
import type { Solicitacao } from '../../models/solicitacao'

export function FilaDeliberacao() {
  const location = useLocation()
  const confirmacao = (location.state as { confirmacao?: string } | null)?.confirmacao
  const queryClient = useQueryClient()
  const [tipo, setTipo] = useState('')
  const [atraso, setAtraso] = useState(false)
  const [selecionadas, setSelecionadas] = useState<string[]>([])
  const [parecer, setParecer] = useState('')
  const [acao, setAcao] = useState('DEFER')
  const [erroLote, setErroLote] = useState<string | null>(null)
  const tipos = useQuery({
    queryKey: ['request-types', 'filtro'],
    queryFn: () => solicitacoesApi.listarTipos(0, 100),
  })
  const publicados = (tipos.data?.content ?? []).filter((tipo) => tipo.status !== 'DRAFT')

  const lista = useQuery({
    queryKey: ['solicitacoes', 'deliberar', tipo, atraso],
    queryFn: () =>
      solicitacoesApi.listarParaDeliberar({
        tipo: tipo || undefined,
        atraso: atraso || undefined,
      }),
  })

  const fila = useActions(lista.data?._links)
  const forbidden = lista.isError && lista.error instanceof ApiError && lista.error.status === 403
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
              {tipos.isLoading && <option value="" disabled>Carregando tipos…</option>}
              {publicados.map((item) => (
                <option key={item.codigo} value={item.codigo}>
                  {item.nome}
                </option>
              ))}
            </select>
            {tipos.isError && <span className="field-error">Não foi possível carregar os tipos.</span>}
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
      {lista.data && lista.data.content.length === 0 && (
        <p className="empty" role="status">
          Nenhuma solicitação aguardando deliberação.
        </p>
      )}
      {lista.data && lista.data.content.length > 0 && (
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
            {lista.data.content.map((item) => (
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
            ))}
          </tbody>
        </table>
      )}
    </section>
  )
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
