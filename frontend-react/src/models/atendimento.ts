import type { HateoasLinks, PageResponse } from './academico'

export interface Atendimento {
  id: string
  alunoId: string
  categoriaId: string
  categoria?: string | null
  assunto: string
  resposta: string
  estado: string
  registradoEm: string
  cienciaEm?: string | null
  temAnexo: boolean
  _links?: HateoasLinks
}

export interface CategoriaAtendimento {
  id: string
  nome: string
}

export interface CategoriasAtendimento {
  content: CategoriaAtendimento[]
  _links?: HateoasLinks
}

export type AtendimentoPage = PageResponse<Atendimento>
