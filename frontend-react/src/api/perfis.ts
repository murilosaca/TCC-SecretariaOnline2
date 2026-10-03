import type { PageResponse } from '../models/academico'
import { api } from './client'

export type PerfilAcesso = {
  id: string
  nome: string
  descricao?: string | null
  tipo: 'SYSTEM' | 'CUSTOM' | string
  authorities: string[]
  quantidadeAuthorities: number
  quantidadeUsuarios: number
  _links?: Record<string, string>
}

export type AuthorityItem = {
  nome: string
  descricao: string
  modulo: string
  sistema: boolean
  readOnlyName: boolean
  _links?: Record<string, string>
}

export type MatrizFgac = {
  authorities: AuthorityItem[]
  perfis: PerfilAcesso[]
  matriz: Record<string, string[]>
}

export type AtribuicaoPerfis = {
  usuarioId: string
  selecionados: string[]
  perfis: PerfilAcesso[]
}

export const perfisApi = {
  listar: (q?: string, page = 0) =>
    api.get<PageResponse<PerfilAcesso>>('/admin/perfis', { q, page, size: 50 }),
  criar: (body: { nome: string; descricao?: string; authorities: string[] }) =>
    api.post<PerfilAcesso>('/admin/perfis', body),
  atualizar: (id: string, body: { descricao?: string; authorities: string[] }) =>
    api.put<PerfilAcesso>(`/admin/perfis/${id}`, body),
  excluir: (path: string) => api.delete(path),
  atribuicao: (id: string) => api.get<AtribuicaoPerfis>(`/admin/usuarios/${id}/perfis`),
  substituir: (id: string, perfilIds: string[]) =>
    api.put<AtribuicaoPerfis>(`/admin/usuarios/${id}/perfis`, { perfilIds }),
}

export const autoridadesApi = {
  listar: () => api.get<MatrizFgac>('/admin/autoridades'),
  descrever: (nome: string, descricao: string) =>
    api.patch<AuthorityItem>(`/admin/autoridades/${encodeURIComponent(nome)}`, { descricao }),
  salvarMatriz: (perfis: { id: string; authorities: string[] }[]) =>
    api.put<MatrizFgac>('/admin/autoridades/matriz', { perfis }),
}
