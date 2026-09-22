import { api } from './client'
import type { Tcc, TccPage } from '../models/tcc'

export const tccsApi = {
  listarMeus: (estado?: string, page = 0, size = 20) =>
    api.get<TccPage>('/tccs', { aluno: 'me', estado: estado || undefined, page, size }),
  listarParaRevisao: (page = 0, size = 20) =>
    api.get<TccPage>('/tccs', { canReview: 'true', page, size }),
  obter: (id: string) => api.get<Tcc>(`/tccs/${id}`),
  enviarVersaoFinal: (id: string, arquivo: File) => {
    const form = new FormData()
    form.append('arquivo', arquivo)
    return api.postForm<Tcc>(`/tccs/${id}/versao-final`, form)
  },
  avaliar: (id: string, acao: string, nota: number, parecer: string) =>
    api.post<Tcc>(`/tccs/${id}/avaliacoes`, { acao, nota, parecer }),
  baixar: (href: string) => api.getBlob(href),
}
