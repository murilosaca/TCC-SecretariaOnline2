import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { ApiError } from '../../api/client'
import { bffApi } from '../../api/bff'
import { useAuth } from '../../auth/AuthContext'
import { useActions } from '../../hooks/useActions'
import { blocoKpi, blocoLista } from '../../lib/dashboardBlocks'
import { ePainelAluno, ePainelProfessor } from '../../lib/dashboardPortal'
import type {
  FilaSolicitacaoProfessor,
  KpisDashboard,
  KpisProfessorDashboard,
  MeuEventoProfessor,
  PendenciaDashboard,
  ProximoEventoDashboard,
  UltimaSolicitacaoDashboard,
} from '../../models/dashboard'

export function Inicio() {
  const { authorities } = useAuth()
  const painelAluno = ePainelAluno(authorities)
  const painelProfessor = ePainelProfessor(authorities)

  if (painelProfessor) {
    return <InicioProfessor />
  }
  if (painelAluno) {
    return <InicioAluno />
  }
  return (
    <section className="page dashboard" aria-labelledby="dashboard-titulo">
      <header className="page-head">
        <div>
          <h1 id="dashboard-titulo">Início</h1>
          <p className="muted caption">Secretaria Online 2 · SEPT/UFPR</p>
        </div>
      </header>
      <p className="empty">Dashboard indisponível para esta sessão.</p>
    </section>
  )
}

function InicioAluno() {
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
          <KpiRowAluno loading={dashboard.isLoading} kpis={dashboard.data.kpis} />

          <div className="dashboard-main">
            <div className="dashboard-col">
              <PendenciasBloco
                loading={dashboard.isLoading}
                itens={dashboard.data.pendencias}
                onRetry={() => dashboard.refetch()}
              />
              <FormativasPendenciasBloco
                loading={dashboard.isLoading}
                itens={dashboard.data.pendenciasFormativas}
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

function InicioProfessor() {
  const dashboard = useQuery({
    queryKey: ['bff', 'dashboard', 'professor'],
    queryFn: () => bffApi.dashboardProfessor(),
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
              <p className="muted caption">Portal do professor · SEPT/UFPR</p>
            </>
          ) : (
            <>
              <h1 id="dashboard-titulo">Início</h1>
              <p className="muted caption">Secretaria Online 2 · SEPT/UFPR</p>
            </>
          )}
        </div>
        <div className="page-head">
          {actions.can('deliberar') && (
            <Link to={actions.href('deliberar') ?? '/solicitacoes?to=me'} className="button-link">
              Deliberar
            </Link>
          )}
          {actions.can('eventos') && (
            <Link to={actions.href('eventos') ?? '/professor/eventos'} className="ghost-link">
              Meus eventos
            </Link>
          )}
        </div>
      </header>

      {forbidden && (
        <p className="empty">Dashboard do professor indisponível para esta sessão.</p>
      )}
      {dashboard.isError && !forbidden && (
        <div className="banner danger" role="alert">
          Não foi possível carregar o dashboard.{' '}
          <button type="button" onClick={() => dashboard.refetch()}>
            Tentar de novo
          </button>
        </div>
      )}

      {dashboard.data && (
        <>
          <KpiRowProfessor loading={dashboard.isLoading} kpis={dashboard.data.kpis} actions={actions} />

          <div className="dashboard-main">
            <div className="dashboard-col">
              <FilaDeliberacaoBloco
                loading={dashboard.isLoading}
                itens={dashboard.data.filaSolicitacoes}
                onRetry={() => dashboard.refetch()}
              />
              <MeusEventosBloco
                loading={dashboard.isLoading}
                itens={dashboard.data.meusEventos}
                onRetry={() => dashboard.refetch()}
              />
              {actions.can('formativasCaaf') && (
                <FilaItensBloco
                  titulo="Formativas CAAF"
                  tituloId="formativas-caaf-titulo"
                  loading={dashboard.isLoading}
                  itens={dashboard.data.formativasCaaf}
                  empty="Nenhuma formativa aguardando revisão."
                  errorMsg="Não foi possível carregar as formativas CAAF no momento."
                  verTodosHref={actions.href('formativasCaaf')}
                  onRetry={() => dashboard.refetch()}
                />
              )}
              {actions.can('estagios') && (
                <FilaItensBloco
                  titulo="Estágios sob responsabilidade"
                  tituloId="estagios-professor-titulo"
                  loading={dashboard.isLoading}
                  itens={dashboard.data.estagiosPendentes}
                  empty="Nenhum estágio pendente de parecer."
                  errorMsg="Não foi possível carregar os estágios no momento."
                  verTodosHref={actions.href('estagios')}
                  onRetry={() => dashboard.refetch()}
                />
              )}
              {actions.can('tccs') && (
                <FilaItensBloco
                  titulo="TCCs sob responsabilidade"
                  tituloId="tccs-professor-titulo"
                  loading={dashboard.isLoading}
                  itens={dashboard.data.tccsPendentes}
                  empty="Nenhum TCC pendente de avaliação."
                  errorMsg="Não foi possível carregar os TCCs no momento."
                  verTodosHref={actions.href('tccs')}
                  onRetry={() => dashboard.refetch()}
                />
              )}
            </div>
          </div>
        </>
      )}
    </section>
  )
}

function KpiRowAluno({ loading, kpis }: { loading: boolean; kpis?: KpisDashboard }) {
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

function KpiRowProfessor({
  loading,
  kpis,
  actions,
}: {
  loading: boolean
  kpis?: KpisProfessorDashboard
  actions: ReturnType<typeof useActions>
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
  if (!kpis) {
    return null
  }
  const sla = kpis.slaUrgentes
  return (
    <div className="kpi-row">
      <KpiNumerico
        label="Pendentes deliberar"
        valor={kpis.pendentesDeliberar}
        estado={blocoKpi(kpis.pendentesDeliberar)}
      />
      {actions.can('formativasCaaf') && (
        <KpiNumerico
          label="Formativas revisão"
          valor={kpis.formativasRevisao}
          estado={blocoKpi(kpis.formativasRevisao)}
        />
      )}
      <KpiNumerico label="Eventos hoje" valor={kpis.eventosHoje} estado={blocoKpi(kpis.eventosHoje)} />
      <article className="kpi-card">
        <p className="kpi-label">SLA urgentes</p>
        {sla == null ? (
          <p className="muted">Indisponível neste momento.</p>
        ) : (
          <>
            <p className="kpi-value">{sla}</p>
            {sla > 0 && (
              <p className="badge estado-em_ajuste">
                {sla === 1 ? '1 solicitação urgente' : `${sla} solicitações urgentes`}
              </p>
            )}
            {actions.can('deliberar') && sla > 0 && (
              <Link to={actions.href('deliberar') ?? '/solicitacoes?to=me'} className="ghost-link">
                Ver fila
              </Link>
            )}
          </>
        )}
      </article>
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

function FormativasPendenciasBloco({
  loading,
  itens,
  onRetry,
}: {
  loading: boolean
  itens?: PendenciaDashboard[] | null
  onRetry: () => void
}) {
  if (!loading && itens != null && itens.length === 0) {
    return null
  }
  const estado = blocoLista(itens)
  return (
    <section className="panel" aria-labelledby="formativas-pendencias-titulo">
      <h2 id="formativas-pendencias-titulo">Formativas a confirmar</h2>
      {loading && (
        <p className="muted" aria-busy="true">
          Carregando formativas…
        </p>
      )}
      {!loading && estado === 'error' && (
        <div className="banner warning" role="status">
          Não foi possível carregar as formativas no momento.{' '}
          <button type="button" onClick={onRetry}>
            Tentar de novo
          </button>
        </div>
      )}
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

function FilaDeliberacaoBloco({
  loading,
  itens,
  onRetry,
}: {
  loading: boolean
  itens?: FilaSolicitacaoProfessor[] | null
  onRetry: () => void
}) {
  const estado = blocoLista(itens)
  return (
    <section className="panel" aria-labelledby="fila-deliberacao-titulo">
      <h2 id="fila-deliberacao-titulo">Fila de solicitações</h2>
      {loading && (
        <p className="muted" aria-busy="true">
          Carregando fila…
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
      {!loading && estado === 'empty' && <p className="empty">Nenhuma solicitação para deliberar.</p>}
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
                  <Link to={item.href}>{item.protocolo}</Link>
                  {item.urgente && <span className="badge estado-em_ajuste"> Urgente</span>}
                </td>
                <td>{item.tipoNome}</td>
                <td>
                  <span className={`badge estado-${item.estado.toLowerCase()}`}>{item.estado}</span>
                </td>
                <td className={item.slaVencido ? 'sla-danger' : undefined}>
                  {item.prazoEm ? new Date(item.prazoEm).toLocaleDateString('pt-BR') : '—'}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  )
}

function MeusEventosBloco({
  loading,
  itens,
  onRetry,
}: {
  loading: boolean
  itens?: MeuEventoProfessor[] | null
  onRetry: () => void
}) {
  const estado = blocoLista(itens)
  return (
    <section className="panel" aria-labelledby="meus-eventos-titulo">
      <h2 id="meus-eventos-titulo">Meus eventos hoje</h2>
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
      {!loading && estado === 'empty' && <p className="empty">Nenhum evento seu hoje.</p>}
      {!loading && estado === 'ok' && itens && (
        <ul className="pendencia-list">
          {itens.map((item) => {
            const itemActions = {
              can: (rel: string) => Boolean(item._links?.[rel]),
              href: (rel: string) => item._links?.[rel],
            }
            return (
              <li key={item.id}>
                <div className="evento-card">
                  <span>{item.titulo}</span>
                  <span className="evento-card-meta">
                    <time dateTime={item.inicioEm}>
                      {new Date(item.inicioEm).toLocaleString('pt-BR', {
                        dateStyle: 'short',
                        timeStyle: 'short',
                      })}
                    </time>
                    <span className={`badge estado-${item.estado.toLowerCase()}`}>{item.estado}</span>
                    {item.estado === 'EM_ANDAMENTO' && (
                      <span className="badge estado-em_andamento">Em andamento</span>
                    )}
                  </span>
                  <span className="evento-card-meta">
                    {itemActions.can('self') && (
                      <Link to={itemActions.href('self') ?? `/professor/eventos/${item.id}`} className="ghost-link">
                        Detalhe
                      </Link>
                    )}
                    {itemActions.can('operar') && (
                      <Link
                        to={itemActions.href('operar') ?? `/professor/eventos/${item.id}/operacao`}
                        className="button-link"
                      >
                        Operar evento
                      </Link>
                    )}
                  </span>
                </div>
              </li>
            )
          })}
        </ul>
      )}
    </section>
  )
}

function FilaItensBloco({
  titulo,
  tituloId,
  loading,
  itens,
  empty,
  errorMsg,
  verTodosHref,
  onRetry,
}: {
  titulo: string
  tituloId: string
  loading: boolean
  itens?: PendenciaDashboard[] | null
  empty: string
  errorMsg: string
  verTodosHref?: string
  onRetry: () => void
}) {
  const estado = blocoLista(itens)
  return (
    <section className="panel" aria-labelledby={tituloId}>
      <h2 id={tituloId}>{titulo}</h2>
      {loading && (
        <p className="muted" aria-busy="true">
          Carregando…
        </p>
      )}
      {!loading && estado === 'error' && (
        <div className="banner warning" role="status">
          {errorMsg}{' '}
          <button type="button" onClick={onRetry}>
            Tentar de novo
          </button>
        </div>
      )}
      {!loading && estado === 'empty' && <p className="empty">{empty}</p>}
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
      {verTodosHref && (
        <p>
          <Link to={verTodosHref} className="ghost-link">
            Ver todos
          </Link>
        </p>
      )}
    </section>
  )
}
