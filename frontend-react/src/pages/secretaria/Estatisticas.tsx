import { FormEvent, useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { ApiError } from '../../api/client'
import { reportsApi } from '../../api/reports'
import {
  SerieChart,
  resumoFormativas,
  resumoPresencas,
  resumoSolicitacoesPorEstado,
  resumoSolicitacoesPorTipo,
} from '../../components/SerieChart'
import type { ItemSolicitacaoRelatorio } from '../../models/relatorios'

const STALE_MS = 5 * 60 * 1000
const DRILL_PAGE = 20

export function Estatisticas() {
  const [params, setParams] = useSearchParams()
  const periodo = params.get('periodo') ?? ''
  const curso = params.get('curso') ?? ''
  const [periodoDraft, setPeriodoDraft] = useState(periodo)
  const [cursoDraft, setCursoDraft] = useState(curso)
  const [tipoSelecionado, setTipoSelecionado] = useState<string | null>(null)
  const [drillPagina, setDrillPagina] = useState(0)

  useEffect(() => {
    setPeriodoDraft(periodo)
    setCursoDraft(curso)
  }, [periodo, curso])

  const consulta = useQuery({
    queryKey: ['reports', 'secretary', { periodo, curso }],
    queryFn: () =>
      reportsApi.secretary({
        periodo: periodo || undefined,
        curso: curso || undefined,
      }),
    staleTime: STALE_MS,
  })

  const forbidden = consulta.isError && consulta.error instanceof ApiError && consulta.error.status === 403

  const itensFiltrados =
    consulta.data?.itensSolicitacao.filter((item) =>
      tipoSelecionado ? item.tipoCodigo === tipoSelecionado : false,
    ) ?? []
  const itensPagina = itensFiltrados.slice(drillPagina * DRILL_PAGE, drillPagina * DRILL_PAGE + DRILL_PAGE)
  const totalPaginas = Math.max(1, Math.ceil(itensFiltrados.length / DRILL_PAGE))
  const tipoNomeSelecionado =
    consulta.data?.solicitacoesPorTipo.find((p) => p.tipoCodigo === tipoSelecionado)?.tipoNome ??
    tipoSelecionado

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
    setTipoSelecionado(null)
    setDrillPagina(0)
  }

  function selecionarTipo(tipoCodigo: string) {
    setTipoSelecionado((atual) => (atual === tipoCodigo ? null : tipoCodigo))
    setDrillPagina(0)
  }

  return (
    <section className="page relatorios" aria-labelledby="estatisticas-titulo">
      <header className="page-head">
        <div>
          <h1 id="estatisticas-titulo">Estatísticas da secretaria</h1>
          <p className="muted caption">
            {consulta.data
              ? `${consulta.data.filtros.cursoNome} · ${consulta.data.filtros.periodoRotulo ?? 'Período vigente'}`
              : 'Indicadores operacionais dos cursos vinculados'}
          </p>
        </div>
      </header>

      <form className="filter-bar" onSubmit={aplicarFiltros} aria-label="Filtros das estatísticas">
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
            placeholder="Sigla (vazio = todos vinculados)"
            autoComplete="off"
          />
        </label>
        <button type="submit">Aplicar</button>
      </form>

      {forbidden && (
        <div className="banner danger" role="alert">
          Você não tem permissão para ver estatísticas destes cursos.
        </div>
      )}
      {consulta.isError && !forbidden && (
        <div className="banner danger" role="alert">
          Não foi possível carregar as estatísticas.{' '}
          <button type="button" onClick={() => void consulta.refetch()}>
            Tentar de novo
          </button>
        </div>
      )}

      {consulta.isLoading && (
        <div aria-busy="true" aria-live="polite">
          <div className="chart-grid">
            {Array.from({ length: 4 }).map((_, i) => (
              <div key={i} className="skeleton skeleton-chart" aria-hidden="true" />
            ))}
          </div>
        </div>
      )}

      {consulta.data && (
        <>
          <div className="chart-grid">
            <SerieChart
              titulo="Solicitações por tipo"
              resumo={resumoSolicitacoesPorTipo(consulta.data.solicitacoesPorTipo)}
              pontos={consulta.data.solicitacoesPorTipo.map((p) => ({
                rotulo: p.tipoCodigo,
                valores: { qtd: p.quantidade },
              }))}
              series={[{ chave: 'qtd', rotulo: 'Quantidade', cor: '#0f4c81' }]}
              selecionado={tipoSelecionado != null}
              onSelect={() => {
                const primeiro = consulta.data.solicitacoesPorTipo[0]
                if (!primeiro) {
                  return
                }
                selecionarTipo(tipoSelecionado ? tipoSelecionado : primeiro.tipoCodigo)
              }}
            />
            <SerieChart
              titulo="Solicitações por estado"
              resumo={resumoSolicitacoesPorEstado(consulta.data.solicitacoesPorEstado)}
              pontos={consulta.data.solicitacoesPorEstado.map((p) => ({
                rotulo: p.estado,
                valores: { qtd: p.quantidade },
              }))}
              series={[{ chave: 'qtd', rotulo: 'Quantidade', cor: '#0f4c81' }]}
            />
            <SerieChart
              titulo="Presenças confirmadas"
              resumo={resumoPresencas(consulta.data.presencas)}
              pontos={consulta.data.presencas.map((p) => ({
                rotulo: p.periodo,
                valores: { confirmadas: p.confirmadas, registradas: p.registradas },
              }))}
              series={[
                { chave: 'confirmadas', rotulo: 'Confirmadas', cor: '#0f4c81' },
                { chave: 'registradas', rotulo: 'Registradas', cor: '#9b1c1c' },
              ]}
            />
            <SerieChart
              titulo="Horas formativas validadas"
              resumo={resumoFormativas(consulta.data.horasFormativas)}
              pontos={consulta.data.horasFormativas.map((p) => ({
                rotulo: p.periodo,
                valores: { horas: p.horasValidadas },
              }))}
              series={[{ chave: 'horas', rotulo: 'Horas', cor: '#0f4c81' }]}
            />
          </div>

          {consulta.data.solicitacoesPorTipo.length > 0 && (
            <div className="quick-tiles" role="group" aria-label="Filtrar drill-down por tipo">
              {consulta.data.solicitacoesPorTipo.map((p) => (
                <button
                  key={p.tipoCodigo}
                  type="button"
                  className={`quick-tile${tipoSelecionado === p.tipoCodigo ? ' selecionado' : ''}`}
                  onClick={() => selecionarTipo(p.tipoCodigo)}
                >
                  {p.tipoNome}
                </button>
              ))}
            </div>
          )}

          {tipoSelecionado && (
            <DrillDownSolicitacoes
              tipoNome={tipoNomeSelecionado ?? tipoSelecionado}
              itens={itensPagina}
              pagina={drillPagina}
              totalPaginas={totalPaginas}
              total={itensFiltrados.length}
              onPagina={setDrillPagina}
            />
          )}

          {consulta.data.solicitacoesPorTipo.length === 0 &&
            consulta.data.presencas.every((p) => p.registradas === 0) &&
            consulta.data.horasFormativas.every((p) => p.horasValidadas === 0) && (
              <p className="empty">Nenhum dado disponível para os filtros informados.</p>
            )}
        </>
      )}

      {!consulta.isLoading && !consulta.isError && consulta.data == null && (
        <p className="empty">Nenhum dado disponível para os filtros informados.</p>
      )}
    </section>
  )
}

function DrillDownSolicitacoes({
  tipoNome,
  itens,
  pagina,
  totalPaginas,
  total,
  onPagina,
}: {
  tipoNome: string
  itens: ItemSolicitacaoRelatorio[]
  pagina: number
  totalPaginas: number
  total: number
  onPagina: (n: number) => void
}) {
  return (
    <section className="stack" aria-labelledby="drill-titulo">
      <h2 id="drill-titulo">Drill-down · {tipoNome}</h2>
      {total === 0 ? (
        <p className="empty">Sem solicitações deste tipo no período.</p>
      ) : (
        <>
          <table className="data-table">
            <thead>
              <tr>
                <th scope="col">Protocolo</th>
                <th scope="col">Tipo</th>
                <th scope="col">Estado</th>
                <th scope="col">Curso</th>
                <th scope="col">Criada em</th>
              </tr>
            </thead>
            <tbody>
              {itens.map((item) => (
                <tr key={item.id}>
                  <td>{item.protocolo}</td>
                  <td>{item.tipoNome}</td>
                  <td>{item.estado}</td>
                  <td>{item.cursoSigla}</td>
                  <td>{new Date(item.createdAt).toLocaleString('pt-BR')}</td>
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
