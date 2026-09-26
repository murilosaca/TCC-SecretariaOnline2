import { api } from './client'
import { baixarViaPresign } from './download'
import type { Estagio, EstagioPage, RegistrarEstagioRequest } from '../models/estagio'

export const estagiosApi = {
  listarMeus: (situacao?: string, page = 0, size = 20) =>
    api.get<EstagioPage>('/estagios', { aluno: 'me', situacao: situacao || undefined, page, size }),
  listarDoEscopo: (situacao?: string, page = 0, size = 20) =>
    api.get<EstagioPage>('/estagios', { escopo: 'cursos', situacao: situacao || undefined, page, size }),
  criar: (body: RegistrarEstagioRequest) => api.post<Estagio>('/estagios', body),
  atualizar: (id: string, body: RegistrarEstagioRequest) => api.put<Estagio>(`/estagios/${id}`, body),
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
  baixar: (href: string, nomeArquivo: string) => baixarViaPresign(href, nomeArquivo),
}
