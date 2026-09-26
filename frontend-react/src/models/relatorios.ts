import type { HateoasLinks } from './academico'

export type CoordinatorReportFiltros = {
  periodo: string | null
  periodoRotulo: string | null
  curso: string
  cursoId: string
  cursoNome: string
}

export type CoordinatorReportKpis = {
  tempoMedioDias: number
  taxaIndeferimento: number
  horasValidadas: number
  taxaPresenca: number
  thresholdIndeferimento: number
}

export type PontoEvasao = {
  periodo: string
  ativos: number
  evadidos: number
}

export type PontoFormativa = {
  periodo: string
  horasValidadas: number
  aprovadas: number
  indeferidas: number
}

export type PontoAprovacao = {
  periodo: string
  taxaAprovacao: number
}

export type PendenciaRelatorio = {
  id: string
  descricao: string
  href: string
}

export type CargaDeliberador = {
  nome: string
  quantidade: number
  tempoMedioDias: number
}

export type CoordinatorReport = {
  filtros: CoordinatorReportFiltros
  kpis: CoordinatorReportKpis
  series: {
    evasao: PontoEvasao[]
    formativas: PontoFormativa[]
    aprovacaoFormativas: PontoAprovacao[]
  }
  pendencias: PendenciaRelatorio[]
  cargaPorDeliberador: CargaDeliberador[]
  _links: HateoasLinks
}
