import type { Formativa, FormativaPage } from '../models/formativa'
import { api } from './client'

export type TipoAtividadeFormativa = {
  id: string
  nome: string
}

export const formativasApi = {
  listarMinhas: (page = 0, size = 20) =>
    api.get<FormativaPage>('/formativas', { audience: 'me', page, size }),
  listarParaRevisao: (page = 0, size = 20) =>
    api.get<FormativaPage>('/formativas', { canReview: 'true', page, size }),
  tipos: () => api.get<{ content: TipoAtividadeFormativa[] }>('/formativas/tipos'),
  obter: (id: string) => api.get<Formativa>(`/formativas/${id}`),
  submeter: (tipoId: string, cargaHoraria: number, arquivo: File) => {
    const form = new FormData()
    form.set('tipoId', tipoId)
    form.set('cargaHoraria', String(cargaHoraria))
    form.set('arquivo', arquivo)
    return api.postForm<Formativa>('/formativas', form)
  },
  comprovante: (href: string) => api.get<{ downloadUrl: string; expiresInSeconds: number }>(href),
  confirmar: (id: string) => api.post<Formativa>(`/formativas/${id}/confirmar`),
  cancelar: (id: string) => api.post<Formativa>(`/formativas/${id}/cancelar`),
  aprovar: (id: string, parecer: string) =>
    api.post<Formativa>(`/formativas/${id}/aprovar`, { parecer }),
  indeferir: (id: string, parecer: string) =>
    api.post<Formativa>(`/formativas/${id}/indeferir`, { parecer }),
}
