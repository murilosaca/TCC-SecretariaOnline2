import type {
  RequestType,
  RequestTypePage,
  Solicitacao,
  SolicitacaoFilaPage,
  SolicitacaoPage,
} from '../models/solicitacao'
import { api } from './client'

function qs(params: Record<string, string | number | boolean | undefined>): string {
  const search = new URLSearchParams()
  for (const [key, value] of Object.entries(params)) {
    if (value === undefined || value === '') continue
    search.set(key, String(value))
  }
  const raw = search.toString()
  return raw ? `?${raw}` : ''
}

export const solicitacoesApi = {
  listarTipos: (page = 0, size = 20) =>
    api.get<RequestTypePage>('/request-types', { page, size }),
  obterTipo: (codigo: string) => api.get<RequestType>(`/request-types/${codigo}`),
  listarMinhas: (filtros: { estado?: string; tipo?: string; ano?: number; page?: number; size?: number } = {}) =>
    api.get<SolicitacaoPage>('/requests', {
      solicitante: 'me',
      estado: filtros.estado,
      tipo: filtros.tipo,
      ano: filtros.ano,
      page: filtros.page ?? 0,
      size: filtros.size ?? 20,
    }),
  listarFilaCurso: (
    filtros: {
      estado?: string
      tipo?: string
      curso?: string
      atraso?: boolean
      slaBreached?: boolean
      page?: number
      size?: number
    } = {},
  ) =>
    api.get<SolicitacaoFilaPage>('/requests', {
      estado: filtros.estado,
      tipo: filtros.tipo,
      curso: filtros.curso,
      atraso: filtros.atraso ? 'true' : undefined,
      slaBreached: filtros.slaBreached ? 'true' : undefined,
      page: filtros.page ?? 0,
      size: filtros.size ?? 20,
    }),
  exportarAtrasadosCsv: (
    filtros: { page?: number; size?: number } = {},
  ) =>
    api.getBlob(
      `/requests${qs({
        slaBreached: true,
        format: 'csv',
        page: filtros.page ?? 0,
        size: filtros.size ?? 100,
      })}`,
      'text/csv',
    ),
  atribuirEmMassa: (ids: string[], deliberadorId: string) =>
    api.patch<Solicitacao[]>('/requests/bulk', { ids, deliberadorId }),
  criar: (tipoCodigo: string, payload: Record<string, unknown>, onBehalfOf?: string) =>
    api.post<Solicitacao>('/requests', { tipoCodigo, payload, onBehalfOf }),
  deliberarImagemLote: (ids: string[], decisao: 'DEFERIDA' | 'INDEFERIDA', justificativa?: string) =>
    api.patch<Solicitacao[]>('/requests/bulk-deliberate', { ids, decisao, justificativa }),
  listarAutorizacoesImagem: (page = 0, size = 50) =>
    api.get<SolicitacaoPage>('/requests', { tipo: 'AUTORIZACAO_IMAGEM', estado: 'TODOS', page, size }),
  obter: (id: string, token?: string) =>
    api.get<Solicitacao>(`/requests/${id}`, { token }),
  listarParaDeliberar: (
    filtros: { tipo?: string; atraso?: boolean; page?: number; size?: number } = {},
  ) =>
    api.get<SolicitacaoPage>('/requests', {
      canDeliberate: 'true',
      tipo: filtros.tipo,
      atraso: filtros.atraso ? 'true' : undefined,
      page: filtros.page ?? 0,
      size: filtros.size ?? 20,
    }),
  transicionar: (id: string, action: string, parecer: string, token?: string) =>
    api.post<Solicitacao>(
      `/requests/${id}/transitions${token ? `?token=${encodeURIComponent(token)}` : ''}`,
      { action, parecer },
    ),
}
