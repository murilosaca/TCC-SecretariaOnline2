import type { HateoasLinks, PageResponse } from './academico'

export interface EgressoCertificado {
  id: string
  titulo: string
  tipo: string
  emitidoEm: string
  hashSha256: string
  _links?: HateoasLinks
}

export interface EgressoDiploma {
  numero: string
  emitidoEm: string
  _links?: HateoasLinks
}

export interface EgressoColacao {
  data: string | null
  turma: string | null
}

export interface EgressoItem {
  alunoId: string
  nome: string
  grr?: string | null
  cursoId: string
  cursoSigla: string
  cursoNome?: string | null
  dataColacao: string
  anoColacao: number
  situacaoDiploma: string
  numeroDiploma: string
  _links?: HateoasLinks
}

export type EgressoPage = PageResponse<EgressoItem>

export interface EgressoPainel {
  nome: string
  curso: string | null
  concluidoEm: string | null
  kpis: {
    horasFormativasValidadas: number | null
    certificadosEmitidos: number | null
    situacaoDiploma: string | null
  }
  diploma: EgressoDiploma | null
  colacao: EgressoColacao | null
  certificados: EgressoCertificado[]
  _links?: HateoasLinks
}
