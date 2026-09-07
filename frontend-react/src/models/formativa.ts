import type { HateoasLinks, PageResponse } from './academico'

export type FormativaEstado =
  | 'PENDENTE_CONFIRMACAO'
  | 'AGUARDANDO_CAAF'
  | 'APROVADA'
  | 'INDEFERIDA'
  | 'CANCELADA'

export type FormativaOrigem = 'PRESENCA_VALIDADA' | 'COMPROVANTE'

export type Formativa = {
  id: string
  idAluno: string
  idEvento?: string | null
  origem: FormativaOrigem
  titulo: string
  cargaHoraria: number
  estado: FormativaEstado
  createdAt: string
  updatedAt: string
  _links?: HateoasLinks
}

export type FormativaPage = PageResponse<Formativa>
