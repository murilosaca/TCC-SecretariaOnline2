import { FormEvent, useEffect, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { ApiError } from '../../api/client'
import { reportsApi } from '../../api/reports'
import {
  SerieChart,
  resumoAprovacao,
  resumoCarga,
  resumoEvasao,
  resumoFormativas,
} from '../../components/SerieChart'
import { useActions } from '../../hooks/useActions'
import type { CargaDeliberador } from '../../models/relatorios'

const STALE_MS = 5 * 60 * 1000
const DRILL_PAGE = 20

const QUICK_TILES = [
  { rel: 'configurar-curso', label: 'Configurar curso' },
  { rel: 'deliberar', label: 'Deliberar' },
  { rel: 'comissoes-caaf', label: 'Pool CAAF' },
  { rel: 'tccs-revisao', label: 'Revisão de TCCs' },
  { rel: 'tccs-secretaria', label: 'Cadastro de TCCs' },
  { rel: 'tipos-solicitacao', label: 'Tipos de Solicitação' },
] as const

function pct(valor: number): string {
  return `${Math.round(valor * 1000) / 10}%`
}

function dias(valor: number): string {
  return `${Math.round(valor * 10) / 10} d`
}

export function Relatorios() {
  const [params, setParams] = useSearchParams()
  const periodo = params.get('periodo') ?? ''
  const curso = params.get('curso') ?? ''
  const [periodoDraft, setPeriodoDraft] = useState(periodo)
  const [cursoDraft, setCursoDraft] = useState(curso)
  const [drillAberto, setDrillAberto] = useState(false)
  const [drillPagina, setDrillPagina] = useState(0)

  useEffect(() => {
    setPeriodoDraft(periodo)
    setCursoDraft(curso)
  }, [periodo, curso])

  const consulta = useQuery({
    queryKey: ['coordinator-report', { periodo, curso }],
    queryFn: () =>
      reportsApi.coordinator({
        periodo: periodo || undefined,
        curso: curso || undefined,
      }),
    staleTime: STALE_MS,
  })

  const actions = useActions(consulta.data?._links)
  const forbidden = consulta.isError && consulta.error instanceof ApiError && consulta.error.status === 403
  const kpis = consulta.data?.kpis
  const alertaIndeferimento =
    kpis != null && kpis.taxaIndeferimento > kpis.thresholdIndeferimento
  const quickTiles = QUICK_TILES.filter((t) => actions.can(t.rel))

  const carga = consulta.data?.cargaPorDeliberador ?? []
  const cargaPagina = carga.slice(drillPagina * DRILL_PAGE, drillPagina * DRILL_PAGE + DRILL_PAGE)
  const totalPaginas = Math.max(1, Math.ceil(carga.length / DRILL_PAGE))

  function aplicarFiltros(event: FormEvent) {
    event.preventDefault()
    const next = new URLSearchParams()
    if (periodoDraft.trim()) {
      next.set('periodo', periodoDraft.trim())
    }
    if (cursoDraft.trim()) {
      next.set('curso', cursoDraft.trim().toUpperCase())
    }
    setParams(next)
    setDrillAberto(false)
    setDrillPagina(0)
  }

  return (
    <section className="page relatorios" aria-labelledby="relatorios-titulo">
      <header className="page-head">
        <div>
          <h1 id="relatorios-titulo">Relatórios da coordenação</h1>
          <p className="muted caption">
            {consulta.data
              ? `${consulta.data.filtros.cursoNome} · ${consulta.data.filtros.periodoRotulo ?? 'Período vigente'}`
              : 'KPIs, séries históricas e pendências do seu curso'}
          </p>
        </div>
      </header>

      <form className="filter-bar" onSubmit={aplicarFiltros} aria-label="Filtros do relatório">
        <label>
          Período
          <input
            name="periodo"
            value={periodoDraft}
            onChange={(e) => setPeriodoDraft(e.target.value)}
            placeholder="2026-2"
            autoComplete="off"
          />
        </label>
        <label>
          Curso
          <input
            name="curso"
            value={cursoDraft}
            onChange={(e) => setCursoDraft(e.target.value)}
            placeholder="Sigla do seu curso"
            autoComplete="off"
          />
        </label>
        <button type="submit">Aplicar</button>
      </form>

      {forbidden && (
        <div className="banner danger" role="alert">
          Você não tem permissão para ver relatórios deste curso.
        </div>
      )}
      {consulta.isError && !forbidden && (
        <div className="banner danger" role="alert">
          Não foi possível carregar os relatórios.{' '}
          <button type="button" onClick={() => void consulta.refetch()}>
            Tentar de novo
          </button>
        </div>
      )}

      {consulta.isLoading && (
        <div aria-busy="true" aria-live="polite">
          <div className="kpi-row">
            {Array.from({ length: 4 }).map((_, i) => (
              <div key={i} className="skeleton skeleton-kpi" aria-hidden="true" />
            ))}
          </div>
          <div className="chart-grid">
            {Array.from({ length: 4 }).map((_, i) => (
              <div key={i} className="skeleton skeleton-chart" aria-hidden="true" />
            ))}
          </div>
        </div>
      )}

      {consulta.data && (
        <>
          {alertaIndeferimento && kpis && (
            <div className="banner warning" role="status">
              Taxa de indeferimento: {pct(kpis.taxaIndeferimento)} (acima do limite{' '}
              {pct(kpis.thresholdIndeferimento)})
            </div>
          )}

          <div className="kpi-row" aria-label="Indicadores">
            <article className="kpi-card">
              <p className="kpi-label">Tempo médio</p>
              <p className="kpi-value">{dias(kpis!.tempoMedioDias)}</p>
            </article>
            <article className={`kpi-card${alertaIndeferimento ? ' perigo' : ''}`}>
              <p className="kpi-label">Taxa de indeferimento</p>
              <p className="kpi-value">{pct(kpis!.taxaIndeferimento)}</p>
            </article>
            <article className="kpi-card">
              <p className="kpi-label">Horas validadas</p>
              <p className="kpi-value">{kpis!.horasValidadas} h</p>
            </article>
            <article className="kpi-card">
              <p className="kpi-label">Taxa de presença</p>
              <p className="kpi-value">{pct(kpis!.taxaPresenca)}</p>
            </article>
          </div>

          <div className="chart-grid">
            <SerieChart
              titulo="Evasão por período"
              resumo={resumoEvasao(consulta.data.series.evasao)}
              pontos={consulta.data.series.evasao.map((p) => ({
                rotulo: p.periodo,
                valores: { ativos: p.ativos, evadidos: p.evadidos },
              }))}
              series={[
                { chave: 'ativos', rotulo: 'Ativos', cor: '#0f4c81' },
                { chave: 'evadidos', rotulo: 'Evadidos', cor: '#9b1c1c' },
              ]}
            />
            <SerieChart
              titulo="Horas formativas validadas"
              resumo={resumoFormativas(consulta.data.series.formativas)}
              pontos={consulta.data.series.formativas.map((p) => ({
                rotulo: p.periodo,
                valores: { horas: p.horasValidadas },
              }))}
              series={[{ chave: 'horas', rotulo: 'Horas', cor: '#0f4c81' }]}
            />
            <SerieChart
              titulo="Aprovação de formativas"
              resumo={resumoAprovacao(consulta.data.series.aprovacaoFormativas)}
              pontos={consulta.data.series.aprovacaoFormativas.map((p) => ({
                rotulo: p.periodo,
                valores: { taxa: Math.round(p.taxaAprovacao * 100) },
              }))}
              series={[{ chave: 'taxa', rotulo: 'Taxa (%)', cor: '#0f4c81' }]}
            />
            <SerieChart
              titulo="Carga por deliberador"
              resumo={resumoCarga(carga)}
              pontos={carga.slice(0, 8).map((c) => ({
                rotulo: c.nome.split(' ')[0] || c.nome,
                valores: { qtd: c.quantidade },
              }))}
              series={[{ chave: 'qtd', rotulo: 'Deliberações', cor: '#0f4c81' }]}
              selecionado={drillAberto}
              onSelect={() => {
                setDrillAberto((v) => !v)
                setDrillPagina(0)
              }}
            />
          </div>

          {drillAberto && (
            <DrillDownCarga
              itens={cargaPagina}
              pagina={drillPagina}
              totalPaginas={totalPaginas}
              total={carga.length}
              onPagina={setDrillPagina}
            />
          )}

          <section className="stack" aria-labelledby="pendencias-titulo">
            <h2 id="pendencias-titulo">Pendências</h2>
            {consulta.data.pendencias.length === 0 ? (
              <p className="empty">Nenhuma pendência operacional no momento.</p>
            ) : (
              <ul className="pendencia-list">
                {consulta.data.pendencias.map((p) => (
                  <li key={p.id}>
                    <Link to={p.href}>{p.descricao}</Link>
                  </li>
                ))}
              </ul>
            )}
          </section>

          {quickTiles.length > 0 && (
            <section className="stack" aria-labelledby="atalhos-titulo">
              <h2 id="atalhos-titulo">Atalhos</h2>
              <div className="quick-tiles">
                {quickTiles.map((t) => (
                  <Link key={t.rel} to={actions.href(t.rel) ?? '#'} className="quick-tile">
                    {t.label}
                  </Link>
                ))}
              </div>
            </section>
          )}
        </>
      )}

      {!consulta.isLoading && !consulta.isError && consulta.data == null && (
        <p className="empty">Nenhum dado disponível para os filtros informados.</p>
      )}
    </section>
  )
}

function DrillDownCarga({
  itens,
  pagina,
  totalPaginas,
  total,
  onPagina,
}: {
  itens: CargaDeliberador[]
  pagina: number
  totalPaginas: number
  total: number
  onPagina: (n: number) => void
}) {
  return (
    <section className="stack" aria-labelledby="drill-titulo">
      <h2 id="drill-titulo">Drill-down · carga por deliberador</h2>
      {total === 0 ? (
        <p className="empty">Sem deliberações no período.</p>
      ) : (
        <>
          <table className="data-table">
            <thead>
              <tr>
                <th scope="col">Nome</th>
                <th scope="col">Deliberações</th>
                <th scope="col">Tempo médio (dias)</th>
              </tr>
            </thead>
            <tbody>
              {itens.map((item) => (
                <tr key={item.nome}>
                  <td>{item.nome}</td>
                  <td>{item.quantidade}</td>
                  <td>{dias(item.tempoMedioDias)}</td>
                </tr>
              ))}
            </tbody>
          </table>
          <div className="pager">
            <button type="button" disabled={pagina <= 0} onClick={() => onPagina(pagina - 1)}>
              Anterior
            </button>
            <span className="muted caption">
              Página {pagina + 1} de {totalPaginas}
            </span>
            <button
              type="button"
              disabled={pagina + 1 >= totalPaginas}
              onClick={() => onPagina(pagina + 1)}
            >
              Próxima
            </button>
          </div>
        </>
      )}
    </section>
  )
}
