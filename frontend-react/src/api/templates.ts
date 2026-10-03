import { api } from './client'
import type { PageResponse } from '../models/academico'

export type RevisaoTemplate = {
  versao: number
  assunto: string
  corpo: string
  autorId: string
  autorNome?: string | null
  criadoEm: string
  status: 'CURRENT' | 'ARCHIVED'
}

export type TemplateComunicacao = {
  id: string
  nome: string
  assunto: string
  corpo: string
  variaveis: string[]
  revisoes: RevisaoTemplate[]
  _links?: Record<string, string>
}

export type TemplateResumo = {
  id: string
  nome: string
  assunto: string
}

export const templatesApi = {
  listar: () => api.get<PageResponse<TemplateResumo>>('/admin/templates-comunicacao', { page: 0, size: 50 }),
  buscar: (id: string) => api.get<TemplateComunicacao>(`/admin/templates-comunicacao/${id}`),
  criar: (body: { nome: string; assunto: string; corpo: string }) =>
    api.post<TemplateComunicacao>('/admin/templates-comunicacao', body),
  salvar: (id: string, body: { assunto: string; corpo: string }) =>
    api.post<TemplateComunicacao>(`/admin/templates-comunicacao/${id}/revisoes`, body),
}
