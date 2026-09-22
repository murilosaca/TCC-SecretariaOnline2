import type { HateoasLinks, PageResponse } from './academico'

export type TccSituacao = 'ATIVO' | 'CONCLUIDO'

export type TccEstado =
  | 'EM_ELABORACAO'
  | 'SUBMETIDO'
  | 'CORRECOES_SOLICITADAS'
  | 'APROVADO'
  | 'REPROVADO'

export type MembroBanca = {
  id: string
  idUsuario: string
  rotulo?: string | null
  papel: string
}

export type AvaliacaoTcc = {
  id: string
  idAutor: string
  autorRotulo?: string | null
  papel: string
  acao: string
  resultado: string
  nota: number
  parecer: string
  createdAt: string
}

export type Tcc = {
  id: string
  idAluno: string
  alunoNome?: string | null
  idCurso: string
  titulo: string
  situacao: TccSituacao
  estado: TccEstado
  dataDefesa: string
  dataEntrega: string
  orientadorRotulo?: string | null
  papel?: string | null
  nomeArquivo?: string | null
  tamanho?: number | null
  enviadoEm?: string | null
  membros: MembroBanca[]
  avaliacoes: AvaliacaoTcc[]
  _links?: HateoasLinks
}

export type TccPage = PageResponse<Tcc>
