import type { Formativa, FormativaPage } from '../models/formativa'
import { api } from './client'

export const formativasApi = {
  listarMinhas: (page = 0, size = 20) =>
    api.get<FormativaPage>('/formativas', { audience: 'me', page, size }),
  listarParaRevisao: (page = 0, size = 20) =>
    api.get<FormativaPage>('/formativas', { canReview: 'true', page, size }),
  obter: (id: string) => api.get<Formativa>(`/formativas/${id}`),
  confirmar: (id: string) => api.post<Formativa>(`/formativas/${id}/confirmar`),
  cancelar: (id: string) => api.post<Formativa>(`/formativas/${id}/cancelar`),
  aprovar: (id: string, parecer: string) =>
    api.post<Formativa>(`/formativas/${id}/aprovar`, { parecer }),
  indeferir: (id: string, parecer: string) =>
    api.post<Formativa>(`/formativas/${id}/indeferir`, { parecer }),
}
