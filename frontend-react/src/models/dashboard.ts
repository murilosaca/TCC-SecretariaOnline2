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

export type KpisProfessorDashboard = {
  pendentesDeliberar: number | null
  formativasRevisao: number | null
  eventosHoje: number | null
  slaUrgentes: number | null
}

export type FilaSolicitacaoProfessor = {
  id: string
  protocolo: string
  tipoNome: string
  estado: string
  prazoEm: string | null
  slaVencido: boolean
  urgente: boolean
  href: string
}

export type MeuEventoProfessor = {
  id: string
  titulo: string
  inicioEm: string
  fimEm: string
  estado: string
  _links?: HateoasLinks
}

export type ProfessorDashboard = {
  saudacao: { nome: string }
  kpis: KpisProfessorDashboard
  filaSolicitacoes: FilaSolicitacaoProfessor[] | null
  meusEventos: MeuEventoProfessor[] | null
  formativasCaaf: PendenciaDashboard[] | null
  estagiosPendentes: PendenciaDashboard[] | null
  tccsPendentes: PendenciaDashboard[] | null
  _links?: HateoasLinks
}
