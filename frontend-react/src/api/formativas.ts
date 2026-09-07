import type { Formativa, FormativaPage } from '../models/formativa'
import { api } from './client'

export const formativasApi = {
  listarMinhas: (page = 0, size = 20) =>
    api.get<FormativaPage>('/formativas', { audience: 'me', page, size }),
  obter: (id: string) => api.get<Formativa>(`/formativas/${id}`),
  confirmar: (id: string) => api.post<Formativa>(`/formativas/${id}/confirmar`),
  cancelar: (id: string) => api.post<Formativa>(`/formativas/${id}/cancelar`),
}
