import type { RequestType, RequestTypePage, Solicitacao, SolicitacaoPage } from '../models/solicitacao'
import { api } from './client'

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
  criar: (tipoCodigo: string, payload: Record<string, unknown>) =>
    api.post<Solicitacao>('/requests', { tipoCodigo, payload }),
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
