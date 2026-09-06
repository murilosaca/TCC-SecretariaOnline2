export type HateoasLinks = Record<string, string | undefined>

export interface PageMeta {
  number: number
  size: number
  totalElements: number
  totalPages: number
}

export interface PageResponse<T> {
  content: T[]
  page: PageMeta
  _links?: HateoasLinks
}

export interface Curso {
  id: string
  nome: string
  sigla: string
  codigo: string
  idCoordenador?: string | null
  horasFormativasMinimas: number
  ativo: boolean
  _links?: HateoasLinks
}

export interface CursoRequest {
  nome: string
  sigla: string
  codigo: string
  horasFormativasMinimas: number
  ativo?: boolean
}

export interface Disciplina {
  id: string
  idCurso: string
  codigo: string
  nome: string
  periodo: number
  cargaHorariaTotal: number
  creditos: number
  ativa: boolean
  _links?: HateoasLinks
}

export interface DisciplinaRequest {
  idCurso: string
  codigo: string
  nome: string
  periodo: number
  cargaHorariaTotal: number
  creditos: number
  ativa?: boolean
}

export type AlunoSituacao = 'MATRICULADO' | 'TRANCADO' | 'FORMANDO' | 'EGRESSO' | 'DESLIGADO'

export interface Aluno {
  id: string
  nome: string
  nomeSocial?: string | null
  grr: string
  emailInstitucional: string
  emailPessoal?: string | null
  telefone?: string | null
  idCurso: string
  situacao: AlunoSituacao
  ativo: boolean
  _links?: HateoasLinks
}

export interface AlunoRequest {
  nome: string
  nomeSocial?: string | null
  grr: string
  emailInstitucional: string
  emailPessoal?: string | null
  telefone?: string | null
  idCurso: string
  situacao: AlunoSituacao
  ativo?: boolean
}

export interface ContatoSecretaria {
  nome: string
  endereco: string
  telefone: string
  email: string
  horario: string
}

export interface PeriodoLetivo {
  id: string
  ano: number
  semestre: number
  inicio: string
  fim: string
  ativo: boolean
  _links?: HateoasLinks
}

export interface PeriodoLetivoRequest {
  ano: number
  semestre: number
  inicio: string
  fim: string
  ativo?: boolean
}
