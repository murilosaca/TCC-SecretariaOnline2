import type { Atendimento, AtendimentoPage, CategoriasAtendimento } from '../models/atendimento'
import { api } from './client'

export const atendimentosApi = {
  categorias: () => api.get<CategoriasAtendimento>('/atendimentos/categorias'),
  listarMeus: (status?: string, page = 0, size = 20) =>
    api.get<AtendimentoPage>('/atendimentos', { aluno: 'me', status, page, size }),
  registrar: (campos: {
    alunoId: string
    categoriaId: string
    assunto: string
    resposta: string
    anexo?: File | null
  }) => {
    const form = new FormData()
    form.append('alunoId', campos.alunoId)
    form.append('categoriaId', campos.categoriaId)
    form.append('assunto', campos.assunto)
    form.append('resposta', campos.resposta)
    if (campos.anexo) {
      form.append('anexo', campos.anexo)
    }
    return api.postForm<Atendimento>('/atendimentos', form)
  },
  darCiencia: (href: string) => api.post<Atendimento>(href),
}
