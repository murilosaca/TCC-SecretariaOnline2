import type { RequestTypePage, Solicitacao, SolicitacaoPage } from '../models/solicitacao';
import { apiGet, apiPost } from './client';

export const solicitacoesApi = {
  listarTipos: (page = 0, size = 20) => apiGet<RequestTypePage>('/request-types', { page, size }),
  listarMinhas: (page = 0, size = 20) =>
    apiGet<SolicitacaoPage>('/requests', { solicitante: 'me', page, size }),
  criar: (tipoCodigo: string, payload: Record<string, unknown>) =>
    apiPost<Solicitacao>('/requests', { tipoCodigo, payload }),
  obter: (id: string) => apiGet<Solicitacao>(`/requests/${id}`),
};
