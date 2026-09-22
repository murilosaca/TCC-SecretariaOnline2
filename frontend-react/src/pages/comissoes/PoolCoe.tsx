import { useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { ApiError } from '../../api/client'
import { coeApi } from '../../api/coe'
import { useActions } from '../../hooks/useActions'
import { dataBr, rotuloTipoDocumento } from '../../lib/estagio'
import type { CoeEstagio, CoeMembro, CoePool } from '../../models/coe'

const QUERY_KEY = ['comissoes', 'coe'] as const

export function PoolCoe() {
  const queryClient = useQueryClient()
  const pool = useQuery({
    queryKey: QUERY_KEY,
    queryFn: () => coeApi.pool(),
  })
  const colecao = useActions(pool.data?._links)
  const forbidden = pool.isError && pool.error instanceof ApiError && pool.error.status === 403
  const [selecionados, setSelecionados] = useState<string[]>([])
  const [quadro, setQuadro] = useState<string[] | null>(null)
  const [erroAcao, setErroAcao] = useState<string | null>(null)

  const atribuiveis = useMemo(
    () => (pool.data?.content ?? []).filter((item) => Boolean(item._links?.['assign-member'])),
    [pool.data],
  )

  const atribuir = useMutation({
    mutationFn: async ({ ids, assigneeId }: { ids: string[]; assigneeId: string }) => {
      let ultimo: CoePool | undefined
      for (const estagioId of ids) {
        ultimo = await coeApi.atribuir(estagioId, assigneeId)
      }
      if (!ultimo) {
        throw new Error('Nenhum estágio selecionado.')
      }
      return ultimo
    },
    onSuccess: (atualizado) => {
      queryClient.setQueryData(QUERY_KEY, atualizado)
      setSelecionados([])
      setQuadro(null)
      setErroAcao(null)
    },
    onError: (erro) => {
      setErroAcao(erro instanceof ApiError ? erro.message : 'Não foi possível atribuir o estágio.')
    },
  })

  function alternar(id: string, marcado: boolean) {
    setSelecionados((atuais) => (marcado ? [...new Set([...atuais, id])] : atuais.filter((item) => item !== id)))
  }

  function alternarTodos(marcado: boolean) {
    setSelecionados(marcado ? atribuiveis.map((item) => item.id) : [])
  }

  return (
    <section className="page">
      <header className="page-head">
        <div>
          <h1>Comissão COE</h1>
          <p className="muted">
            Pool coletivo de estágios. Atribua orientador a si ou a um colega. O parecer continua
            individual em Revisão de estágios.
          </p>
        </div>
      </header>

      {forbidden && (
        <p className="empty" role="status">
          Pool da COE indisponível para esta sessão.
        </p>
      )}
      {pool.isError && !forbidden && (
        <div className="banner danger" role="alert">
          Não foi possível carregar o pool da COE.{' '}
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
          Carregando pool da COE…
        </p>
      )}

      {pool.data && (
        <>
          <Kpis kpis={pool.data.kpis} />
          {erroAcao && (
            <div className="banner danger" role="alert">
              {erroAcao}
            </div>
          )}
          {pool.data.content.length === 0 ? (
            <p className="empty" role="status">
              Todos os estágios do período já têm orientador atribuído.
            </p>
          ) : (
            <Tabela
              itens={pool.data.content}
              selecionados={selecionados}
              atribuiveis={atribuiveis.length}
              pending={atribuir.isPending}
              onAlternar={alternar}
              onAlternarTodos={alternarTodos}
              onAtribuirAMim={(id) => atribuir.mutate({ ids: [id], assigneeId: pool.data.meuId })}
              onAbrirQuadro={(ids) => setQuadro(ids)}
            />
          )}
          {selecionados.length > 0 && colecao.can('assign-member') && (
            <div className="bulk-bar" role="region" aria-label="Ações em lote">
              <p>
                {selecionados.length} {selecionados.length === 1 ? 'estágio selecionado' : 'estágios selecionados'}
              </p>
              <button type="button" onClick={() => setQuadro(selecionados)} disabled={atribuir.isPending}>
                Atribuir selecionados
              </button>
            </div>
          )}
          {quadro && (
            <AssignmentBoard
              membros={pool.data.membros}
              pending={atribuir.isPending}
              onCancelar={() => setQuadro(null)}
              onConfirmar={(assigneeId) => atribuir.mutate({ ids: quadro, assigneeId })}
            />
          )}
        </>
      )}
    </section>
  )
}

function Kpis({ kpis }: { kpis: CoePool['kpis'] }) {
  return (
    <div className="kpi-row">
      <article className="kpi-card">
        <p className="kpi-label">Pool total</p>
        <p className="kpi-value">{kpis.poolTotal}</p>
      </article>
      <article className="kpi-card">
        <p className="kpi-label">Atribuídos a mim</p>
        <p className="kpi-value">{kpis.atribuidosAMim}</p>
      </article>
      <article className="kpi-card">
        <p className="kpi-label">Documentos pendentes</p>
        <p className="kpi-value">{kpis.documentosPendentes}</p>
      </article>
      <article className="kpi-card">
        <p className="kpi-label">Concluídos no período</p>
        <p className="kpi-value">{kpis.concluidosNoPeriodo}</p>
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
  itens: CoeEstagio[]
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
          <th scope="col">Empresa</th>
          <th scope="col">Início</th>
          <th scope="col">Documento pendente</th>
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
  item: CoeEstagio
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
          aria-label={`Selecionar estágio de ${item.alunoNome ?? item.empresa}`}
        />
      </td>
      <td>{item.alunoNome ?? '—'}</td>
      <td>{item.empresa}</td>
      <td>{dataBr(item.inicio)}</td>
      <td>{item.documentoPendente ? rotuloTipoDocumento(item.documentoPendente) : '—'}</td>
      <td>
        {item.noPool && <span className="badge no-pool">No pool</span>}
        {item.comigo && <span className="badge comigo">Comigo</span>}
        {!item.noPool && !item.comigo && (item.orientadorRotulo ?? '—')}
      </td>
      <td className="actions">
        {actions.can('self') && (
          <Link to={`/estagios/${item.id}`} className="ghost-link">
            Abrir
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
  membros: CoeMembro[]
  pending: boolean
  onCancelar: () => void
  onConfirmar: (assigneeId: string) => void
}) {
  const [escolhido, setEscolhido] = useState(membros[0]?.id ?? '')
  return (
    <div className="overlay" role="presentation">
      <aside className="assignment-board" role="dialog" aria-modal="true" aria-labelledby="coe-atribuir-titulo">
        <h2 id="coe-atribuir-titulo">Atribuir orientador</h2>
        <p className="muted">Escolha um membro do COE. A carga alta aparece quando o membro passa da média.</p>
        <ul className="assignment-list">
          {membros.map((membro) => (
            <li key={membro.id}>
              <label className="checkbox-row">
                <input
                  type="radio"
                  name="coe-assignee"
                  value={membro.id}
                  checked={escolhido === membro.id}
                  onChange={() => setEscolhido(membro.id)}
                />
                <span>
                  {membro.rotulo ?? membro.id}
                  {' · '}
                  {membro.carga} {membro.carga === 1 ? 'estágio ativo' : 'estágios ativos'}
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
