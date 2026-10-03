import { Link } from 'react-router-dom'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { ApiError } from '../../api/client'
import { bffApi } from '../../api/bff'
import { useActions } from '../../hooks/useActions'
import { blocoKpi, blocoLista } from '../../lib/dashboardBlocks'
import type { FilaSecretariaDashboard, KpisSecretariaDashboard } from '../../models/dashboard'

const STALE_MS = 60_000
const TILES: { rel: string; label: string }[] = [
  { rel: 'cursos', label: 'Cursos' },
  { rel: 'alunos', label: 'Alunos' },
  { rel: 'disciplinas', label: 'Disciplinas' },
  { rel: 'calendarios', label: 'Calendários' },
  { rel: 'fila-solicitacoes', label: 'Fila de solicitações' },
  { rel: 'atrasados', label: 'Atrasados' },
  { rel: 'diplomas', label: 'Diplomas' },
  { rel: 'estatisticas', label: 'Estatísticas' },
  { rel: 'importacoes', label: 'Importações' },
  { rel: 'exportacoes', label: 'Exportações' },
]

export function InicioSecretaria() {
  const queryClient = useQueryClient()
  const dashboard = useQuery({
    queryKey: ['bff', 'dashboard', 'secretary'],
    queryFn: () => bffApi.dashboardSecretaria(),
    staleTime: STALE_MS,
  })
  const actions = useActions(dashboard.data?._links)
  const forbidden = dashboard.isError && dashboard.error instanceof ApiError && dashboard.error.status === 403
  const carregando = dashboard.isLoading || dashboard.isRefetching

  function atualizar() {
    void queryClient.invalidateQueries({ queryKey: ['bff', 'dashboard', 'secretary'] })
  }

  return (
    <section className="page dashboard" aria-labelledby="dashboard-titulo">
      <header className="page-head">
        <div>
          {carregando && !dashboard.data ? (
            <div className="skeleton skeleton-title" aria-hidden="true" />
          ) : (
            <>
              <h1 id="dashboard-titulo">
                {dashboard.data ? `Olá, ${dashboard.data.saudacao.nome}` : 'Início'}
              </h1>
              <p className="muted caption">
                {dashboard.data?.periodoVigente?.rotulo
                  ? `${dashboard.data.periodoVigente.rotulo} · Portal da secretaria · SEPT/UFPR`
                  : 'Portal da secretaria · SEPT/UFPR'}
              </p>
            </>
          )}
        </div>
        <button type="button" onClick={atualizar}>
          Atualizar
        </button>
      </header>

      {forbidden && (
        <div className="banner danger" role="alert">
          {dashboard.error instanceof ApiError
            ? dashboard.error.message
            : 'Dashboard da secretaria indisponível para esta sessão.'}
        </div>
      )}
      {dashboard.isError && !forbidden && (
        <div className="banner danger" role="alert">
          Não foi possível carregar o dashboard.{' '}
          <button type="button" onClick={() => dashboard.refetch()}>
            Tentar de novo
          </button>
        </div>
      )}

      {dashboard.data?.alertaPeriodoAusente && (
        <div className="banner warning" role="status">
          Não há período letivo vigente. Cadastre o calendário atual para o alerta sair do painel.
        </div>
      )}
      {dashboard.data && dashboard.data.alertasSla != null && dashboard.data.alertasSla > 0 && (
        <div className="banner danger" role="alert">
          <span className="sla-danger">
            {dashboard.data.alertasSla === 1
              ? '1 solicitação com SLA estourado'
              : `${dashboard.data.alertasSla} solicitações com SLA estourado`}
          </span>
        </div>
      )}

      {dashboard.data && (
        <>
          <KpiRowSecretaria loading={carregando} kpis={dashboard.data.kpis} />
          <FilaSecretaria
            loading={carregando}
            itens={dashboard.data.filaPriorizada}
            onRetry={() => dashboard.refetch()}
          />
          <AgendaDia
            loading={carregando}
            itens={dashboard.data.agendaDia}
            onRetry={() => dashboard.refetch()}
          />
          <QuickTiles actions={actions} />
        </>
      )}
    </section>
  )
}

function KpiRowSecretaria({
  loading,
  kpis,
}: {
  loading: boolean
  kpis: KpisSecretariaDashboard
}) {
  if (loading) {
    return (
      <div className="kpi-row" aria-busy="true">
        <div className="skeleton skeleton-kpi" />
        <div className="skeleton skeleton-kpi" />
        <div className="skeleton skeleton-kpi" />
        <div className="skeleton skeleton-kpi" />
      </div>
    )
  }
  return (
    <div className="kpi-row">
      <Kpi label="Abertas" valor={kpis.abertas} />
      <Kpi label="Atrasadas" valor={kpis.atrasadas} />
      <Kpi label="Concluídas hoje" valor={kpis.concluidasHoje} />
      <Kpi label="Eventos do dia" valor={kpis.eventosDia} />
    </div>
  )
}

function Kpi({ label, valor }: { label: string; valor: number | null }) {
  const estado = blocoKpi(valor)
  return (
    <article className="kpi-card">
      <p className="kpi-label">{label}</p>
      {estado === 'error' ? (
        <p className="muted">Indisponível neste momento.</p>
      ) : (
        <p className="kpi-value">{valor}</p>
      )}
    </article>
  )
}

function FilaSecretaria({
  loading,
  itens,
  onRetry,
}: {
  loading: boolean
  itens: FilaSecretariaDashboard[] | null
  onRetry: () => void
}) {
  const estado = blocoLista(itens)
  return (
    <section className="panel" aria-labelledby="fila-secretaria-titulo">
      <h2 id="fila-secretaria-titulo">Fila prioritária</h2>
      {loading && (
        <p className="muted" aria-busy="true">
          Carregando fila…
        </p>
      )}
      {!loading && estado === 'error' && (
        <div className="banner warning" role="status">
          Não foi possível carregar a fila no momento.{' '}
          <button type="button" onClick={onRetry}>
            Tentar de novo
          </button>
        </div>
      )}
      {!loading && estado === 'empty' && (
        <p className="empty">Nenhuma solicitação aberta nos seus cursos.</p>
      )}
      {!loading && estado === 'ok' && itens && (
        <table>
          <thead>
            <tr>
              <th scope="col">Protocolo</th>
              <th scope="col">Tipo</th>
              <th scope="col">Estado</th>
              <th scope="col">SLA</th>
            </tr>
          </thead>
          <tbody>
            {itens.map((item) => (
              <tr key={item.id}>
                <td>
                  <Link to={item.href}>{item.protocolo}</Link>
                </td>
                <td>{item.tipoNome}</td>
                <td>{item.estado}</td>
                <td>
                  {item.slaStatus === 'danger' && <span className="sla-danger">SLA estourado</span>}
                  {item.slaStatus === 'warning' && <span className="sla-warning">Vence em 24 h</span>}
                  {!item.slaStatus && <span className="muted">No prazo</span>}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  )
}

function AgendaDia({
  loading,
  itens,
  onRetry,
}: {
  loading: boolean
  itens: { id: string; titulo: string; inicioEm: string; estado: string }[] | null
  onRetry: () => void
}) {
  const estado = blocoLista(itens)
  return (
    <section className="panel" aria-labelledby="agenda-dia-titulo">
      <h2 id="agenda-dia-titulo">Agenda do dia</h2>
      {loading && (
        <p className="muted" aria-busy="true">
          Carregando agenda…
        </p>
      )}
      {!loading && estado === 'error' && (
        <div className="banner warning" role="status">
          Não foi possível carregar a agenda no momento.{' '}
          <button type="button" onClick={onRetry}>
            Tentar de novo
          </button>
        </div>
      )}
      {!loading && estado === 'empty' && <p className="empty">Nenhum evento neste dia.</p>}
      {!loading && estado === 'ok' && itens && (
        <ul>
          {itens.map((item) => (
            <li key={item.id}>
              {item.titulo}{' '}
              <span className="muted">{new Date(item.inicioEm).toLocaleString('pt-BR')}</span>
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}

function QuickTiles({ actions }: { actions: ReturnType<typeof useActions> }) {
  const visiveis = TILES.filter((tile) => actions.can(tile.rel))
  if (visiveis.length === 0) {
    return null
  }
  return (
    <section className="panel" aria-labelledby="atalhos-titulo">
      <h2 id="atalhos-titulo">Atalhos</h2>
      <div className="quick-tiles">
        {visiveis.map((tile) => (
          <Link key={tile.rel} to={actions.href(tile.rel) ?? '#'} className="quick-tile">
            {tile.label}
          </Link>
        ))}
      </div>
    </section>
  )
}
