import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { ApiError } from '../../api/client'
import { bffApi } from '../../api/bff'
import { useActions } from '../../hooks/useActions'
import { blocoKpi, blocoLista } from '../../lib/dashboardBlocks'
import type {
  KpisDashboard,
  PendenciaDashboard,
  ProximoEventoDashboard,
  UltimaSolicitacaoDashboard,
} from '../../models/dashboard'

export function Inicio() {
  const dashboard = useQuery({
    queryKey: ['bff', 'dashboard', 'aluno'],
    queryFn: () => bffApi.dashboardAluno(),
  })
  const actions = useActions(dashboard.data?._links)
  const forbidden = dashboard.isError && dashboard.error instanceof ApiError && dashboard.error.status === 403

  return (
    <section className="page dashboard" aria-labelledby="dashboard-titulo">
      <header className="page-head">
        <div>
          {dashboard.isLoading ? (
            <div className="skeleton skeleton-title" aria-hidden="true" />
          ) : dashboard.data ? (
            <>
              <h1 id="dashboard-titulo">Olá, {dashboard.data.saudacao.nome}</h1>
              <p className="muted caption">
                {[dashboard.data.saudacao.curso, dashboard.data.periodoVigente?.rotulo]
                  .filter(Boolean)
                  .join(' · ') || 'Portal do aluno · SEPT/UFPR'}
              </p>
            </>
          ) : (
            <>
              <h1 id="dashboard-titulo">Início</h1>
              <p className="muted caption">Secretaria Online 2 · SEPT/UFPR</p>
            </>
          )}
        </div>
        {actions.can('novaSolicitacao') && (
          <Link to="/solicitacoes/nova" className="button-link">
            Nova solicitação
          </Link>
        )}
      </header>

      {forbidden && (
        <p className="empty">Dashboard do aluno indisponível para esta sessão.</p>
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
          Não há período letivo vigente. A secretaria ainda não cadastrou o calendário atual.
        </div>
      )}
      {dashboard.data && dashboard.data.alertaPeriodoAusente === null && !dashboard.data.periodoVigente && (
        <div className="banner warning" role="status">
          Não foi possível carregar o período letivo no momento.
        </div>
      )}

      {dashboard.data && (
        <>
          <KpiRow loading={dashboard.isLoading} kpis={dashboard.data.kpis} />

          <div className="dashboard-main">
            <div className="dashboard-col">
              <PendenciasBloco
                loading={dashboard.isLoading}
                itens={dashboard.data.pendencias}
                onRetry={() => dashboard.refetch()}
              />
              <SolicitacoesBloco
                loading={dashboard.isLoading}
                itens={dashboard.data.ultimasSolicitacoes}
                onRetry={() => dashboard.refetch()}
              />
              <EventosBloco
                loading={dashboard.isLoading}
                itens={dashboard.data.proximosEventos}
                onRetry={() => dashboard.refetch()}
              />
            </div>
          </div>
        </>
      )}
    </section>
  )
}

function KpiRow({ loading, kpis }: { loading: boolean; kpis?: KpisDashboard }) {
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
  if (!kpis) {
    return null
  }
  const horas = kpis.horasFormativas
  return (
    <div className="kpi-row">
      <article className="kpi-card">
        <p className="kpi-label">Horas formativas</p>
        {horas ? (
          <>
            <p className="kpi-value">
              {horas.validadas} / {horas.requeridas} h
            </p>
            <progress
              className="kpi-progress"
              max={horas.requeridas}
              value={horas.validadas}
              aria-label="Progresso de horas formativas"
            />
          </>
        ) : (
          <p className="muted">Indisponível neste momento.</p>
        )}
      </article>
      <KpiNumerico
        label="Solicitações"
        valor={kpis.solicitacoesAbertas}
        estado={blocoKpi(kpis.solicitacoesAbertas)}
      />
      <KpiNumerico label="Eventos hoje" valor={kpis.eventosHoje} estado={blocoKpi(kpis.eventosHoje)} />
      <KpiNumerico label="Certificados" valor={kpis.certificados} estado={blocoKpi(kpis.certificados)} />
    </div>
  )
}

function KpiNumerico({
  label,
  valor,
  estado,
}: {
  label: string
  valor: number | null
  estado: ReturnType<typeof blocoKpi>
}) {
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

function PendenciasBloco({
  loading,
  itens,
  onRetry,
}: {
  loading: boolean
  itens?: PendenciaDashboard[] | null
  onRetry: () => void
}) {
  const estado = blocoLista(itens)
  return (
    <section className="panel" aria-labelledby="pendencias-titulo">
      <h2 id="pendencias-titulo">Pendências</h2>
      {loading && (
        <p className="muted" aria-busy="true">
          Carregando pendências…
        </p>
      )}
      {!loading && estado === 'error' && (
        <div className="banner warning" role="status">
          Não foi possível carregar as pendências no momento.{' '}
          <button type="button" onClick={onRetry}>
            Tentar de novo
          </button>
        </div>
      )}
      {!loading && estado === 'empty' && <p className="empty">Nenhuma pendência no momento.</p>}
      {!loading && estado === 'ok' && itens && (
        <ul className="pendencia-list">
          {itens.map((item) => (
            <li key={item.id}>
              <Link to={item.href}>
                {item.titulo} <span className={`badge estado-${item.estado.toLowerCase()}`}>{item.estado}</span>
              </Link>
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}

function SolicitacoesBloco({
  loading,
  itens,
  onRetry,
}: {
  loading: boolean
  itens?: UltimaSolicitacaoDashboard[] | null
  onRetry: () => void
}) {
  const estado = blocoLista(itens)
  return (
    <section className="panel" aria-labelledby="solicitacoes-titulo">
      <h2 id="solicitacoes-titulo">Últimas solicitações</h2>
      {loading && (
        <p className="muted" aria-busy="true">
          Carregando solicitações…
        </p>
      )}
      {!loading && estado === 'error' && (
        <div className="banner warning" role="status">
          Não foi possível carregar as solicitações no momento.{' '}
          <button type="button" onClick={onRetry}>
            Tentar de novo
          </button>
        </div>
      )}
      {!loading && estado === 'empty' && <p className="empty">Você ainda não abriu solicitações.</p>}
      {!loading && estado === 'ok' && itens && (
        <table>
          <thead>
            <tr>
              <th scope="col">Número</th>
              <th scope="col">Tipo</th>
              <th scope="col">Estado</th>
              <th scope="col">Prazo</th>
            </tr>
          </thead>
          <tbody>
            {itens.map((item) => (
              <tr key={item.id}>
                <td>
                  <Link to={`/solicitacoes/${item.id}`}>{item.protocolo}</Link>
                </td>
                <td>{item.tipoNome}</td>
                <td>
                  <span className={`badge estado-${item.estado.toLowerCase()}`}>{item.estado}</span>
                </td>
                <td className={item.slaVencido ? 'sla-danger' : undefined}>
                  {new Date(item.prazoEm).toLocaleDateString('pt-BR')}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  )
}

function EventosBloco({
  loading,
  itens,
  onRetry,
}: {
  loading: boolean
  itens?: ProximoEventoDashboard[] | null
  onRetry: () => void
}) {
  const estado = blocoLista(itens)
  return (
    <section className="panel" aria-labelledby="eventos-titulo">
      <h2 id="eventos-titulo">Próximos eventos</h2>
      {loading && (
        <p className="muted" aria-busy="true">
          Carregando eventos…
        </p>
      )}
      {!loading && estado === 'error' && (
        <div className="banner warning" role="status">
          Não foi possível carregar os eventos no momento.{' '}
          <button type="button" onClick={onRetry}>
            Tentar de novo
          </button>
        </div>
      )}
      {!loading && estado === 'empty' && <p className="empty">Nenhum evento com janela aberta hoje.</p>}
      {!loading && estado === 'ok' && itens && (
        <ul className="pendencia-list">
          {itens.map((item) => (
            <li key={item.id}>
              <Link to={item.href} className="evento-card">
                <span>{item.titulo}</span>
                <span className="evento-card-meta">
                  <time dateTime={item.inicioEm}>
                    {new Date(item.inicioEm).toLocaleString('pt-BR', {
                      dateStyle: 'short',
                      timeStyle: 'short',
                    })}
                  </time>
                  {item.janelaAtiva && <span className="badge estado-em_andamento">Janela aberta</span>}
                </span>
              </Link>
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}
