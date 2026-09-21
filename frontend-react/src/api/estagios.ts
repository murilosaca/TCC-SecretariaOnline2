import { api } from './client'
import type { Estagio, EstagioPage } from '../models/estagio'

export const estagiosApi = {
  listarMeus: (situacao?: string, page = 0, size = 20) =>
    api.get<EstagioPage>('/estagios', { aluno: 'me', situacao: situacao || undefined, page, size }),
  listarParaRevisao: (page = 0, size = 20) =>
    api.get<EstagioPage>('/estagios', { canReview: 'true', page, size }),
  obter: (id: string) => api.get<Estagio>(`/estagios/${id}`),
  enviarDocumento: (id: string, tipo: string, arquivo: File) => {
    const form = new FormData()
    form.set('tipo', tipo)
    form.set('arquivo', arquivo)
    return api.postForm<Estagio>(`/estagios/${id}/documentos`, form)
  },
  parecer: (id: string, documentoId: string, acao: 'APROVAR' | 'REPROVAR', parecer: string) =>
    api.post<Estagio>(`/estagios/${id}/documentos/${documentoId}/parecer`, { acao, parecer }),
  encerrar: (id: string) => api.post<Estagio>(`/estagios/${id}/encerrar`),
}
