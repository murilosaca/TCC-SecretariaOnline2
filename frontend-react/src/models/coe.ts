import type { HateoasLinks } from './academico'

export type CoeKpis = {
  poolTotal: number
  atribuidosAMim: number
  documentosPendentes: number
  concluidosNoPeriodo: number
}

export type CoeMembro = {
  id: string
  rotulo?: string | null
  carga: number
  cargaAlta: boolean
}

export type CoeEstagio = {
  id: string
  idAluno: string
  alunoNome?: string | null
  idCurso: string
  idOrientador?: string | null
  orientadorRotulo?: string | null
  empresa: string
  inicio: string
  documentoPendente?: string | null
  noPool: boolean
  comigo: boolean
  _links?: HateoasLinks
}

export type CoePool = {
  meuId: string
  kpis: CoeKpis
  membros: CoeMembro[]
  content: CoeEstagio[]
  _links?: HateoasLinks
}
