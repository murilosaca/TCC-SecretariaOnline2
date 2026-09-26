type SerieLinha = {
  chave: string
  rotulo: string
  cor: string
}

type Ponto = {
  rotulo: string
  valores: Record<string, number>
}

type Props = {
  titulo: string
  resumo: string
  pontos: Ponto[]
  series: SerieLinha[]
  onSelect?: () => void
  selecionado?: boolean
}

/** Gráfico de linhas SVG leve (camada reutilizável pela fatia 19). */
export function SerieChart({ titulo, resumo, pontos, series, onSelect, selecionado }: Props) {
  const largura = 480
  const altura = 220
  const pad = { top: 16, right: 16, bottom: 36, left: 40 }
  const plotW = largura - pad.left - pad.right
  const plotH = altura - pad.top - pad.bottom
  const maxValor = Math.max(
    1,
    ...pontos.flatMap((p) => series.map((s) => p.valores[s.chave] ?? 0)),
  )

  function x(i: number): number {
    if (pontos.length <= 1) {
      return pad.left + plotW / 2
    }
    return pad.left + (i / (pontos.length - 1)) * plotW
  }

  function y(v: number): number {
    return pad.top + plotH - (v / maxValor) * plotH
  }

  return (
    <figure
      className={`chart-card${selecionado ? ' selecionado' : ''}`}
      aria-labelledby={undefined}
    >
      <h3 className="chart-title" role="heading" aria-level={3}>
        {titulo}
      </h3>
      <div className="chart-plot" role="img" aria-label={resumo}>
        <svg viewBox={`0 0 ${largura} ${altura}`} width="100%" height="auto" aria-hidden="true">
          <line
            x1={pad.left}
            y1={pad.top + plotH}
            x2={pad.left + plotW}
            y2={pad.top + plotH}
            className="chart-axis"
          />
          <line
            x1={pad.left}
            y1={pad.top}
            x2={pad.left}
            y2={pad.top + plotH}
            className="chart-axis"
          />
          {series.map((s) => {
            const d = pontos
              .map((p, i) => `${i === 0 ? 'M' : 'L'} ${x(i)} ${y(p.valores[s.chave] ?? 0)}`)
              .join(' ')
            return (
              <path key={s.chave} d={d} fill="none" stroke={s.cor} strokeWidth="2.5" />
            )
          })}
          {pontos.map((p, i) => (
            <text key={p.rotulo} x={x(i)} y={altura - 12} textAnchor="middle" className="chart-tick">
              {p.rotulo}
            </text>
          ))}
        </svg>
      </div>
      <p className="chart-resumo muted caption">{resumo}</p>
      <ul className="chart-legend">
        {series.map((s) => (
          <li key={s.chave}>
            <span className="chart-swatch" style={{ background: s.cor }} aria-hidden="true" />
            {s.rotulo}
          </li>
        ))}
      </ul>
      {onSelect && (
        <button type="button" className="ghost" onClick={onSelect}>
          {selecionado ? 'Ocultar drill-down' : 'Ver drill-down'}
        </button>
      )}
    </figure>
  )
}

export function resumoEvasao(pontos: { periodo: string; evadidos: number }[]): string {
  if (pontos.length === 0) {
    return 'Sem dados de evasão no intervalo.'
  }
  const pico = pontos.reduce((a, b) => (b.evadidos > a.evadidos ? b : a))
  return `Maior evasão em ${pico.periodo}: ${pico.evadidos} aluno(s) desligado(s).`
}

export function resumoFormativas(pontos: { periodo: string; horasValidadas: number }[]): string {
  if (pontos.length === 0) {
    return 'Sem horas formativas validadas no intervalo.'
  }
  const total = pontos.reduce((acc, p) => acc + p.horasValidadas, 0)
  const pico = pontos.reduce((a, b) => (b.horasValidadas > a.horasValidadas ? b : a))
  return `Total de ${total} h validadas; pico em ${pico.periodo} (${pico.horasValidadas} h).`
}

export function resumoAprovacao(pontos: { periodo: string; taxaAprovacao: number }[]): string {
  if (pontos.length === 0) {
    return 'Sem taxa de aprovação de formativas no intervalo.'
  }
  const ultimo = pontos[pontos.length - 1]
  return `Taxa de aprovação mais recente (${ultimo.periodo}): ${Math.round(ultimo.taxaAprovacao * 100)}%.`
}

export function resumoCarga(pontos: { nome: string; quantidade: number }[]): string {
  if (pontos.length === 0) {
    return 'Sem deliberações no período filtrado.'
  }
  const topo = pontos[0]
  return `Maior carga: ${topo.nome} com ${topo.quantidade} deliberação(ões).`
}
