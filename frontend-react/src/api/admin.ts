import type { AdminUsuario, AdminUsuarioRequest, PageResponse, UsuarioOpcao } from '../models/iam'
import { api } from './client'

export const adminApi = {
  listarUsuarios: (q?: string, page = 0, size = 20) =>
    api.get<PageResponse<AdminUsuario>>('/admin/usuarios', { q, page, size }),
  criarUsuario: (body: AdminUsuarioRequest) => api.post<AdminUsuario>('/admin/usuarios', body),
  atualizarUsuario: (id: string, body: AdminUsuarioRequest) =>
    api.put<AdminUsuario>(`/admin/usuarios/${id}`, body),
  desativarUsuario: (id: string) => api.post<AdminUsuario>(`/admin/usuarios/${id}/desativar`),
  resetSenha: (id: string) => api.post<void>(`/admin/usuarios/${id}/reset-senha`),
}

export const iamApi = {
  buscarUsuarios: (q?: string, page = 0, size = 20) =>
    api.get<PageResponse<UsuarioOpcao>>('/iam/usuarios', { q, page, size }),
}
