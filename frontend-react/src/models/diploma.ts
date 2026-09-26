import type { HateoasLinks } from './academico'

export interface BloqueioColacao {
  razao: string
  detalhe: string | null
}

export interface ElegivelColacao {
  alunoId: string
  nome: string
  grr: string
  elegivel: boolean
  bloqueio: BloqueioColacao | null
}

export interface ElegiveisColacao {
  cursoId: string
  periodoId: string
  content: ElegivelColacao[]
  _links?: HateoasLinks
}

export interface Diploma {
  id: string
  alunoId: string
  alunoNome: string | null
  cursoId: string
  periodoId: string | null
  numero: string
  situacao: string
  dataColacao: string
  livro: string
  folha: string
  turma: string | null
  metodoEntrega: string | null
  dataEntrega: string | null
  temPdf: boolean
  _links?: HateoasLinks
}

export interface ConfirmarColacaoRequest {
  cursoId: string
  periodoId: string
  alunoIds: string[]
  dataColacao: string
  livro: string
  folha: string
  turma?: string | null
}

export interface ConfirmarEntregaRequest {
  metodo: 'PRESENCIAL' | 'PROCURACAO' | 'CORREIO'
  dataEntrega?: string | null
}
