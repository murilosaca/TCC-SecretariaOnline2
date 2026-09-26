import type {
  ConfirmarColacaoRequest,
  ConfirmarEntregaRequest,
  Diploma,
  ElegiveisColacao,
} from '../models/diploma'
import type { PageResponse } from '../models/academico'
import { api } from './client'

export const diplomasApi = {
  elegiveis: (cursoId: string, periodoId: string) =>
    api.get<ElegiveisColacao>('/diplomas/elegiveis', { cursoId, periodoId }),

  confirmar: (body: ConfirmarColacaoRequest) => api.post<Diploma[]>('/diplomas', body),

  listar: (cursoId: string, situacao?: string, page = 0, size = 50) =>
    api.get<PageResponse<Diploma>>('/diplomas', { cursoId, situacao, page, size }),

  confirmarEntrega: (id: string, body: ConfirmarEntregaRequest) =>
    api.patch<Diploma>(`/diplomas/${id}/confirm-delivery`, body),

  uploadPdf: (id: string, file: File) => {
    const form = new FormData()
    form.append('file', file)
    return api.postForm<Diploma>(`/diplomas/${id}/pdf`, form)
  },
}
