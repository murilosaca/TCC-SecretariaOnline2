import type { Formativa, FormativaPage } from '../models/formativa';
import { apiGet, apiPost } from './client';

export const formativasApi = {
  listarMinhas: (page = 0, size = 20) =>
    apiGet<FormativaPage>('/formativas', { audience: 'me', page, size }),
  obter: (id: string) => apiGet<Formativa>(`/formativas/${id}`),
  confirmar: (id: string) => apiPost<Formativa>(`/formativas/${id}/confirmar`),
  cancelar: (id: string) => apiPost<Formativa>(`/formativas/${id}/cancelar`),
};
