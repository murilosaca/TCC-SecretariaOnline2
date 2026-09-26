import { api } from './client'
import { baixarViaPresign } from './download'
import type { RegistrarTccRequest, Tcc, TccPage } from '../models/tcc'

export const tccsApi = {
  listarMeus: (estado?: string, page = 0, size = 20) =>
    api.get<TccPage>('/tccs', { aluno: 'me', estado: estado || undefined, page, size }),
  listarDoEscopo: (situacao?: string, page = 0, size = 20) =>
    api.get<TccPage>('/tccs', { escopo: 'cursos', situacao: situacao || undefined, page, size }),
  criar: (body: RegistrarTccRequest) => api.post<Tcc>('/tccs', body),
  atualizar: (id: string, body: RegistrarTccRequest) => api.put<Tcc>(`/tccs/${id}`, body),
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
  baixar: (href: string, nomeArquivo: string) => baixarViaPresign(href, nomeArquivo),
}
