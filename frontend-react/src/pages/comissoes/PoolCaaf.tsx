import { useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { ApiError } from '../../api/client'
import { caafApi } from '../../api/caaf'
import { useActions } from '../../hooks/useActions'
import { rotuloOrigemFormativa } from '../../lib/formativa'
import type { CaafFormativa, CaafMembro, CaafPool } from '../../models/caaf'

const QUERY_KEY = ['comissoes', 'caaf'] as const

export function PoolCaaf() {
  const queryClient = useQueryClient()
  const pool = useQuery({
    queryKey: QUERY_KEY,
    queryFn: () => caafApi.pool(),
  })
  const colecao = useActions(pool.data?._links)
  const forbidden = pool.isError && pool.error instanceof ApiError && pool.error.status === 403
  const [selecionados, setSelecionados] = useState<string[]>([])
  const [quadro, setQuadro] = useState<string[] | null>(null)
  const [confirmarLote, setConfirmarLote] = useState(false)
  const [erroAcao, setErroAcao] = useState<string | null>(null)
  const [sucessoLote, setSucessoLote] = useState<string | null>(null)

  const atribuiveis = useMemo(
    () => (pool.data?.content ?? []).filter((item) => Boolean(item._links?.['assign-member'])),
    [pool.data],
  )

  const selecionadosElegiveisLote = useMemo(() => {
    const mapa = new Map((pool.data?.content ?? []).map((item) => [item.id, item]))
    return selecionados.filter((id) => mapa.get(id)?.elegivelLote)
  }, [pool.data, selecionados])

  const selecaoSoLote =
    selecionados.length > 0 &&
    selecionados.length === selecionadosElegiveisLote.length &&
    colecao.can('batch-approve')

  const atribuir = useMutation({
    mutationFn: async ({ ids, assigneeId }: { ids: string[]; assigneeId: string }) => {
      let ultimo: CaafPool | undefined
      for (const formativaId of ids) {
        ultimo = await caafApi.atribuir(formativaId, assigneeId)
      }
      if (!ultimo) {
        throw new Error('Nenhuma formativa selecionada.')
      }
      return ultimo
    },
    onSuccess: (atualizado) => {
      queryClient.setQueryData(QUERY_KEY, atualizado)
      setSelecionados([])
      setQuadro(null)
      setErroAcao(null)
      setSucessoLote(null)
    },
    onError: (erro) => {
      setErroAcao(erro instanceof ApiError ? erro.message : 'Não foi possível atribuir a formativa.')
    },
  })

  const lote = useMutation({
    mutationFn: (ids: string[]) => caafApi.aprovarLote(ids),
    onSuccess: (atualizado, ids) => {
      queryClient.setQueryData(QUERY_KEY, atualizado)
      setSelecionados([])
      setConfirmarLote(false)
      setErroAcao(null)
      setSucessoLote(
        `${ids.length} ${ids.length === 1 ? 'atividade aprovada' : 'atividades aprovadas'}. Certificados sendo gerados.`,
      )
    },
    onError: (erro) => {
      setErroAcao(erro instanceof ApiError ? erro.message : 'Não foi possível aprovar em lote.')
      setConfirmarLote(false)
    },
  })

  function alternar(id: string, marcado: boolean) {
    setSelecionados((atuais) => (marcado ? [...new Set([...atuais, id])] : atuais.filter((item) => item !== id)))
  }

  function alternarTodos(marcado: boolean) {
    setSelecionados(marcado ? atribuiveis.map((item) => item.id) : [])
  }

  const pending = atribuir.isPending || lote.isPending

  return (
    <section className="page">
      <header className="page-head">
        <div>
          <h1>Comissão CAAF</h1>
          <p className="muted">
            Pool coletivo de formativas. Atribua a si ou a um colega. Aprovação em lote só para
            atividades com presença já validada pelo sistema.
          </p>
        </div>
      </header>

      {forbidden && (
        <p className="empty" role="status">
          Pool da CAAF indisponível para esta sessão.
        </p>
      )}
      {pool.isError && !forbidden && (
        <div className="banner danger" role="alert">
          Não foi possível carregar o pool da CAAF.{' '}
          <button type="button" onClick={() => pool.refetch()}>
            Tentar de novo
          </button>
        </div>
      )}
      {pool.isLoading && (
        <div className="kpi-row" aria-busy="true">
          <div className="skeleton skeleton-kpi" />
          <div className="skeleton skeleton-kpi" />
          <div className="skeleton skeleton-kpi" />
          <div className="skeleton skeleton-kpi" />
        </div>
      )}
      {pool.isLoading && (
        <p className="muted" aria-busy="true">
          Carregando pool da CAAF…
        </p>
      )}

      {pool.data && (
        <>
          <Kpis kpis={pool.data.kpis} />
          {sucessoLote && (
            <div className="banner success" role="status">
              {sucessoLote}
            </div>
          )}
          {erroAcao && (
            <div className="banner danger" role="alert">
              {erroAcao}
            </div>
          )}
          {pool.data.content.length === 0 ? (
            <p className="empty" role="status">
              Nenhuma atividade aguardando revisão no pool.
            </p>
          ) : (
            <Tabela
              itens={pool.data.content}
              selecionados={selecionados}
              atribuiveis={atribuiveis.length}
              pending={pending}
              onAlternar={alternar}
              onAlternarTodos={alternarTodos}
              onAtribuirAMim={(id) => atribuir.mutate({ ids: [id], assigneeId: pool.data.meuId })}
              onAbrirQuadro={(ids) => setQuadro(ids)}
            />
          )}
          {selecionados.length > 0 && (colecao.can('assign-member') || colecao.can('batch-approve')) && (
            <div className="bulk-bar" role="region" aria-label="Ações em lote">
              <p>
                {selecionados.length}{' '}
                {selecionados.length === 1 ? 'formativa selecionada' : 'formativas selecionadas'}
              </p>
              {colecao.can('assign-member') && (
                <button type="button" onClick={() => setQuadro(selecionados)} disabled={pending}>
                  Atribuir selecionados
                </button>
              )}
              <button
                type="button"
                onClick={() => setConfirmarLote(true)}
                disabled={pending || !selecaoSoLote}
                title={
                  selecaoSoLote
                    ? undefined
                    : 'Selecione apenas atividades de evento com presença validada para aprovação em lote.'
                }
              >
                Aprovar selecionados
              </button>
            </div>
          )}
          {quadro && (
            <AssignmentBoard
              membros={pool.data.membros}
              pending={pending}
              onCancelar={() => setQuadro(null)}
              onConfirmar={(assigneeId) => atribuir.mutate({ ids: quadro, assigneeId })}
            />
          )}
          {confirmarLote && (
            <ConfirmacaoLote
              quantidade={selecionadosElegiveisLote.length}
              pending={pending}
              onCancelar={() => setConfirmarLote(false)}
              onConfirmar={() => lote.mutate(selecionadosElegiveisLote)}
            />
          )}
        </>
      )}
    </section>
  )
}

function Kpis({ kpis }: { kpis: CaafPool['kpis'] }) {
  return (
    <div className="kpi-row">
      <article className="kpi-card">
        <p className="kpi-label">Pool total</p>
        <p className="kpi-value">{kpis.poolTotal}</p>
      </article>
      <article className="kpi-card">
        <p className="kpi-label">Atribuídas a mim</p>
        <p className="kpi-value">{kpis.atribuidasAMim}</p>
      </article>
      <article className="kpi-card">
        <p className="kpi-label">Prazo médio (dias)</p>
        <p className="kpi-value">{Number(kpis.prazoMedioDias).toFixed(1)}</p>
      </article>
      <article className="kpi-card">
        <p className="kpi-label">Aprovadas no período</p>
        <p className="kpi-value">{kpis.aprovadasNoPeriodo}</p>
      </article>
    </div>
  )
}

function Tabela({
  itens,
  selecionados,
  atribuiveis,
  pending,
  onAlternar,
  onAlternarTodos,
  onAtribuirAMim,
  onAbrirQuadro,
}: {
  itens: CaafFormativa[]
  selecionados: string[]
  atribuiveis: number
  pending: boolean
  onAlternar: (id: string, marcado: boolean) => void
  onAlternarTodos: (marcado: boolean) => void
  onAtribuirAMim: (id: string) => void
  onAbrirQuadro: (ids: string[]) => void
}) {
  const todos = atribuiveis > 0 && selecionados.length === atribuiveis
  return (
    <table>
      <thead>
        <tr>
          <th scope="col">
            <input
              type="checkbox"
              checked={todos}
              onChange={(event) => onAlternarTodos(event.target.checked)}
              aria-label="Selecionar todos"
              disabled={atribuiveis === 0}
            />
          </th>
          <th scope="col">Aluno</th>
          <th scope="col">Atividade</th>
          <th scope="col">Tipo</th>
          <th scope="col">Horas</th>
          <th scope="col">Submissão</th>
          <th scope="col">Responsável</th>
          <th scope="col">Ações</th>
        </tr>
      </thead>
      <tbody>
        {itens.map((item) => (
          <Linha
            key={item.id}
            item={item}
            marcado={selecionados.includes(item.id)}
            pending={pending}
            onAlternar={onAlternar}
            onAtribuirAMim={onAtribuirAMim}
            onAbrirQuadro={onAbrirQuadro}
          />
        ))}
      </tbody>
    </table>
  )
}

function Linha({
  item,
  marcado,
  pending,
  onAlternar,
  onAtribuirAMim,
  onAbrirQuadro,
}: {
  item: CaafFormativa
  marcado: boolean
  pending: boolean
  onAlternar: (id: string, marcado: boolean) => void
  onAtribuirAMim: (id: string) => void
  onAbrirQuadro: (ids: string[]) => void
}) {
  const actions = useActions(item._links)
  const podeAtribuir = actions.can('assign-member')
  return (
    <tr>
      <td>
        <input
          type="checkbox"
          checked={marcado}
          disabled={!podeAtribuir}
          onChange={(event) => onAlternar(item.id, event.target.checked)}
          aria-label={`Selecionar formativa ${item.titulo}`}
        />
      </td>
      <td>{item.alunoNome ?? '—'}</td>
      <td>{item.titulo}</td>
      <td>
        {rotuloOrigemFormativa(item.origem)}
        {item.elegivelLote ? '' : ' · individual'}
      </td>
      <td>{item.cargaHoraria}h</td>
      <td>{dataBr(item.createdAt)}</td>
      <td>
        {item.noPool && <span className="badge no-pool">No pool</span>}
        {item.comigo && <span className="badge comigo">Comigo</span>}
        {!item.noPool && !item.comigo && (item.responsavelRotulo ?? '—')}
      </td>
      <td className="actions">
        {actions.can('revisar') && (
          <Link to={`/formativas/${item.id}/revisar`} className="ghost-link">
            Revisar
          </Link>
        )}
        {podeAtribuir && item.noPool && (
          <button type="button" className="ghost" onClick={() => onAtribuirAMim(item.id)} disabled={pending}>
            Atribuir a mim
          </button>
        )}
        {podeAtribuir && (
          <button type="button" className="ghost" onClick={() => onAbrirQuadro([item.id])} disabled={pending}>
            Atribuir…
          </button>
        )}
      </td>
    </tr>
  )
}

function AssignmentBoard({
  membros,
  pending,
  onCancelar,
  onConfirmar,
}: {
  membros: CaafMembro[]
  pending: boolean
  onCancelar: () => void
  onConfirmar: (assigneeId: string) => void
}) {
  const [escolhido, setEscolhido] = useState(membros[0]?.id ?? '')
  return (
    <div className="overlay" role="presentation">
      <aside className="assignment-board" role="dialog" aria-modal="true" aria-labelledby="caaf-atribuir-titulo">
        <h2 id="caaf-atribuir-titulo">Atribuir responsável</h2>
        <p className="muted">Escolha um membro da CAAF. A carga alta aparece quando o membro passa da média.</p>
        <ul className="assignment-list">
          {membros.map((membro) => (
            <li key={membro.id}>
              <label className="checkbox-row">
                <input
                  type="radio"
                  name="caaf-assignee"
                  value={membro.id}
                  checked={escolhido === membro.id}
                  onChange={() => setEscolhido(membro.id)}
                />
                <span>
                  {membro.rotulo ?? membro.id}
                  {' · '}
                  {membro.carga} {membro.carga === 1 ? 'item ativo' : 'itens ativos'}
                </span>
                {membro.cargaAlta && <span className="badge carga-alta">Carga alta</span>}
              </label>
            </li>
          ))}
        </ul>
        <div className="action-bar">
          <button type="button" className="ghost" onClick={onCancelar} disabled={pending}>
            Cancelar
          </button>
          <button type="button" onClick={() => onConfirmar(escolhido)} disabled={pending || !escolhido}>
            {pending ? 'Atribuindo…' : 'Confirmar'}
          </button>
        </div>
      </aside>
    </div>
  )
}

function ConfirmacaoLote({
  quantidade,
  pending,
  onCancelar,
  onConfirmar,
}: {
  quantidade: number
  pending: boolean
  onCancelar: () => void
  onConfirmar: () => void
}) {
  return (
    <div className="overlay" role="presentation">
      <aside className="assignment-board" role="dialog" aria-modal="true" aria-labelledby="caaf-lote-titulo">
        <h2 id="caaf-lote-titulo">Confirmar aprovação em lote</h2>
        <p>
          Aprovar {quantidade}{' '}
          {quantidade === 1
            ? 'atividade de evento com presença validada?'
            : 'atividades de evento com presença validada?'}
        </p>
        <div className="action-bar">
          <button type="button" className="ghost" onClick={onCancelar} disabled={pending}>
            Cancelar
          </button>
          <button type="button" onClick={onConfirmar} disabled={pending || quantidade === 0}>
            {pending ? 'Aprovando…' : 'Confirmar'}
          </button>
        </div>
      </aside>
    </div>
  )
}

function dataBr(iso: string): string {
  const data = new Date(iso)
  if (Number.isNaN(data.getTime())) {
    return '—'
  }
  return data.toLocaleDateString('pt-BR', { timeZone: 'UTC' })
}
