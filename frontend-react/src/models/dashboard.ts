import type { HateoasLinks } from './academico'

export type SaudacaoDashboard = {
  nome: string
  curso?: string | null
}

export type PeriodoVigenteDashboard = {
  id: string
  ano: number
  semestre: number
  rotulo: string
  inicio: string
  fim: string
}

export type KpiHorasFormativas = {
  validadas: number
  requeridas: number
}

export type KpisDashboard = {
  horasFormativas: KpiHorasFormativas | null
  solicitacoesAbertas: number | null
  eventosHoje: number | null
  certificados: number | null
}

export type PendenciaDashboard = {
  id: string
  titulo: string
  estado: string
  href: string
}

export type UltimaSolicitacaoDashboard = {
  id: string
  protocolo: string
  tipoNome: string
  estado: string
  prazoEm: string
  slaVencido: boolean
}

export type ProximoEventoDashboard = {
  id: string
  titulo: string
  inicioEm: string
  fimEm: string
  janelaAtiva: boolean
  href: string
}

export type AlunoDashboard = {
  saudacao: SaudacaoDashboard
  periodoVigente: PeriodoVigenteDashboard | null
  alertaPeriodoAusente: boolean | null
  kpis: KpisDashboard
  pendencias: PendenciaDashboard[] | null
  ultimasSolicitacoes: UltimaSolicitacaoDashboard[] | null
  proximosEventos: ProximoEventoDashboard[] | null
  pendenciasFormativas?: PendenciaDashboard[] | null
  _links?: HateoasLinks
}
