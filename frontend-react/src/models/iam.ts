import type { HateoasLinks, PageResponse } from './academico'

export interface AdminUsuario {
  id: string
  nome: string
  emailInstitucional: string
  emailPessoal?: string | null
  grr?: string | null
  ativo: boolean
  senhaAlterada: boolean
  updatedAt: string
  _links?: HateoasLinks
}

export interface AdminUsuarioRequest {
  nome: string
  emailInstitucional: string
  emailPessoal?: string | null
  grr?: string | null
}

export interface UsuarioOpcao {
  id: string
  nome: string
  emailInstitucional: string
  grr?: string | null
}

export type { PageResponse }
