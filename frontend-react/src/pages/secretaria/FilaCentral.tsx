import { FormEvent, useEffect, useMemo, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { ApiError } from '../../api/client'
import { solicitacoesApi } from '../../api/solicitacoes'
import { useActions } from '../../hooks/useActions'
import type { Solicitacao } from '../../models/solicitacao'

export function FilaCentral() {
  const [params, setParams] = useSearchParams()
  const estado = params.get('estado') ?? 'ABERTA'
  const tipo = params.get('tipo') ?? ''
  const curso = params.get('curso') ?? ''
  const atraso = params.get('atraso') === 'true'
  const page = Number(params.get('page') ?? '0') || 0

  const [estadoDraft, setEstadoDraft] = useState(estado)
  const [tipoDraft, setTipoDraft] = useState(tipo)
  const [cursoDraft, setCursoDraft] = useState(curso)
  const [atrasoDraft, setAtrasoDraft] = useState(atraso)
  const [selecionados, setSelecionados] = useState<string[]>([])
  const [deliberadorId, setDeliberadorId] = useState('')
  const [erroAcao, setErroAcao] = useState<string | null>(null)
  const queryClient = useQueryClient()

  useEffect(() => {
    setEstadoDraft(estado)
    setTipoDraft(tipo)
    setCursoDraft(curso)
    setAtrasoDraft(atraso)
  }, [estado, tipo, curso, atraso])

  const lista = useQuery({
    queryKey: ['solicitacoes', 'fila-curso', { estado, tipo, curso, atraso, page }],
    queryFn: () =>
      solicitacoesApi.listarFilaCurso({
        estado: estado || undefined,
        tipo: tipo || undefined,
        curso: curso || undefined,
        atraso: atraso || undefined,
        page,
      }),
  })

  const atribuir = useMutation({
    mutationFn: () => solicitacoesApi.atribuirEmMassa(selecionados, deliberadorId),
    onSuccess: async () => {
      setSelecionados([])
      setDeliberadorId('')
      setErroAcao(null)
      await queryClient.invalidateQueries({ queryKey: ['solicitacoes', 'fila-curso'] })
    },
    onError: (erro) => {
      setErroAcao(erro instanceof ApiError ? erro.message : 'Não foi possível atribuir o deliberador.')
    },
  })

  const forbidden = lista.isError && lista.error instanceof ApiError && lista.error.status === 403
  const atribuiveis = useMemo(
    () => (lista.data?.content ?? []).filter((item) => Boolean(item._links?.bulk_assign)).map((i) => i.id),
    [lista.data],
  )

  function aplicarFiltros(event: FormEvent) {
    event.preventDefault()
    const next = new URLSearchParams()
    if (estadoDraft) next.set('estado', estadoDraft)
    if (tipoDraft) next.set('tipo', tipoDraft)
    if (cursoDraft.trim()) next.set('curso', cursoDraft.trim())
    if (atrasoDraft) next.set('atraso', 'true')
    next.set('page', '0')
    setParams(next)
    setSelecionados([])
  }

  function alternar(id: string, marcado: boolean) {
    setSelecionados((atual) => (marcado ? [...atual, id] : atual.filter((x) => x !== id)))
  }

  function alternarTodos(marcado: boolean) {
    setSelecionados(marcado ? atribuiveis : [])
  }

  return (
    <section className="page" aria-labelledby="fila-titulo">
      <header className="page-head">
        <div>
          <h1 id="fila-titulo">Fila de solicitações</h1>
          <p className="muted">Consulta central dos cursos vinculados (F5.2).</p>
        </div>
      </header>

      {forbidden && (
        <p className="empty" role="status">
          Fila central indisponível para esta sessão.
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
      {erroAcao && (
        <div className="banner danger" role="alert">
          {erroAcao}
        </div>
      )}

      {!forbidden && (
        <form className="filter-bar" onSubmit={aplicarFiltros} aria-label="Filtros da fila">
          <label>
            Estado
            <select value={estadoDraft} onChange={(e) => setEstadoDraft(e.target.value)}>
              <option value="ABERTA">Abertas</option>
              <option value="TODOS">Todos</option>
              <option value="EM_ANALISE">Em análise</option>
              <option value="EM_AJUSTE">Em ajuste</option>
              <option value="DELIBERADA">Deliberada</option>
              <option value="INDEFERIDA">Indeferida</option>
              <option value="CONCLUIDA">Concluída</option>
            </select>
          </label>
          <label>
            Tipo
            <select value={tipoDraft} onChange={(e) => setTipoDraft(e.target.value)}>
              <option value="">Todos</option>
              <option value="DECLARACAO_SIMPLES">Declaração simples</option>
            </select>
          </label>
          <label>
            Curso (UUID)
            <input
              value={cursoDraft}
              onChange={(e) => setCursoDraft(e.target.value)}
              placeholder="Opcional"
              autoComplete="off"
            />
          </label>
          <label className="checkbox-row">
            <input
              type="checkbox"
              checked={atrasoDraft}
              onChange={(e) => setAtrasoDraft(e.target.checked)}
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
          Nenhuma solicitação aberta
        </p>
      )}
      {lista.data && lista.data.content.length > 0 && (
        <table className="data-table">
          <thead>
            <tr>
              <th scope="col">
                <input
                  type="checkbox"
                  checked={atribuiveis.length > 0 && selecionados.length === atribuiveis.length}
                  onChange={(e) => alternarTodos(e.target.checked)}
                  aria-label="Selecionar todas atribuíveis"
                  disabled={atribuiveis.length === 0}
                />
              </th>
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
              <Linha
                key={item.id}
                item={item}
                selecionado={selecionados.includes(item.id)}
                onAlternar={alternar}
              />
            ))}
          </tbody>
        </table>
      )}

      {selecionados.length > 0 && (
        <div className="bulk-bar" role="region" aria-label="Atribuição em massa">
          <p>
            {selecionados.length}{' '}
            {selecionados.length === 1 ? 'solicitação selecionada' : 'solicitações selecionadas'}
          </p>
          <label>
            Deliberador
            <select
              value={deliberadorId}
              onChange={(e) => setDeliberadorId(e.target.value)}
              aria-label="Selecionar deliberador"
            >
              <option value="">Selecione</option>
              {(lista.data?.deliberadores ?? []).map((d) => (
                <option key={d.id} value={d.id}>
                  {d.nome}
                </option>
              ))}
            </select>
          </label>
          <button
            type="button"
            disabled={!deliberadorId || atribuir.isPending}
            onClick={() => atribuir.mutate()}
          >
            Atribuir
          </button>
        </div>
      )}

      {lista.data && lista.data.page.totalPages > 1 && (
        <nav className="pagination" aria-label="Paginação da fila">
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

function Linha({
  item,
  selecionado,
  onAlternar,
}: {
  item: Solicitacao
  selecionado: boolean
  onAlternar: (id: string, marcado: boolean) => void
}) {
  const actions = useActions(item._links)
  const slaClass =
    item.slaStatus === 'danger' ? 'sla-danger' : item.slaStatus === 'warning' ? 'sla-warning' : undefined
  return (
    <tr>
      <td>
        <input
          type="checkbox"
          checked={selecionado}
          disabled={!actions.can('bulk_assign')}
          onChange={(e) => onAlternar(item.id, e.target.checked)}
          aria-label={`Selecionar ${item.protocolo}`}
        />
      </td>
      <td>{item.protocolo}</td>
      <td>{item.solicitanteNome || '—'}</td>
      <td>{item.tipoNome}</td>
      <td>{item.estado}</td>
      <td>{item.deliberadorNome || '—'}</td>
      <td className={slaClass}>{item.slaStatus === 'danger' ? 'Atrasado' : item.slaStatus === 'warning' ? 'Alerta' : 'No prazo'}</td>
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
