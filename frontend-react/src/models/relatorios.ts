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

export type SecretaryReportFiltros = {
  periodo: string | null
  periodoRotulo: string | null
  curso: string | null
  cursoId: string | null
  cursoNome: string
  cursos: { id: string; sigla: string; nome: string }[]
}

export type PontoTipoSolicitacao = {
  tipoCodigo: string
  tipoNome: string
  quantidade: number
}

export type PontoEstadoSolicitacao = {
  estado: string
  quantidade: number
}

export type PontoPresencaSecretaria = {
  periodo: string
  confirmadas: number
  registradas: number
}

export type PontoHorasSecretaria = {
  periodo: string
  horasValidadas: number
}

export type ItemSolicitacaoRelatorio = {
  id: string
  protocolo: string
  tipoCodigo: string
  tipoNome: string
  estado: string
  createdAt: string
  cursoSigla: string
}

export type SecretaryReport = {
  filtros: SecretaryReportFiltros
  solicitacoesPorTipo: PontoTipoSolicitacao[]
  solicitacoesPorEstado: PontoEstadoSolicitacao[]
  presencas: PontoPresencaSecretaria[]
  horasFormativas: PontoHorasSecretaria[]
  itensSolicitacao: ItemSolicitacaoRelatorio[]
  _links: HateoasLinks
}
