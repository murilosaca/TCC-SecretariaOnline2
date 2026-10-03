import type { NotificacaoPreferencia, Perfil, PerfilPatch, SessaoDispositivo } from '../models/perfil'
import { api } from './client'

export const perfilApi = {
  obter: () => api.get<Perfil>('/me'),
  atualizar: (href: string, body: PerfilPatch) => api.patch<Perfil>(href, body),
  enviarFoto: (href: string, form: FormData) => api.postForm<Perfil>(href, form),
  trocarSenha: (body: { senhaAtual: string; novaSenha: string }) =>
    api.patch<{ mensagem: string }>('/me/password', body),
  sessoes: () => api.get<{ itens: SessaoDispositivo[] }>('/me/sessions'),
  encerrarSessao: (href: string) => api.delete(href),
  notificacoes: () => api.get<NotificacaoPreferencia>('/me/notifications'),
  salvarNotificacoes: (href: string, body: Omit<NotificacaoPreferencia, '_links'>) =>
    api.patch<NotificacaoPreferencia>(href, body),
}
