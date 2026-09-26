export type HateoasLinks = Record<string, string>

export type CaafKpis = {
  poolTotal: number
  atribuidasAMim: number
  prazoMedioDias: number
  aprovadasNoPeriodo: number
}

export type CaafMembro = {
  id: string
  rotulo?: string | null
  carga: number
  cargaAlta: boolean
}

export type CaafFormativa = {
  id: string
  idAluno: string
  alunoNome?: string | null
  idCurso?: string | null
  origem: string
  titulo: string
  cargaHoraria: number
  estado: string
  createdAt: string
  idResponsavel?: string | null
  responsavelRotulo?: string | null
  noPool: boolean
  comigo: boolean
  elegivelLote: boolean
  _links?: HateoasLinks
}

export type CaafPool = {
  meuId: string
  kpis: CaafKpis
  membros: CaafMembro[]
  content: CaafFormativa[]
  _links?: HateoasLinks
}
