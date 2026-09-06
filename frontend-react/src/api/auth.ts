import { api } from './client'

export type LoginResponse = {
  accessToken: string
  mustChangePassword: boolean
  expiresIn: number
  tokenType: string
}

export type Sessao = {
  id: string
  mustChangePassword: boolean
  authorities: string[]
}

export const authApi = {
  login: (identificador: string, senha: string) =>
    api.post<LoginResponse>('/auth/login', { identificador, senha }),
  refresh: () => api.post<LoginResponse>('/auth/refresh'),
  logout: () => api.post<void>('/auth/logout'),
  me: () => api.get<Sessao>('/auth/me'),
  recuperarSenha: (email: string) => api.post<void>('/auth/recuperar-senha', { email }),
  validarTokenRedefinicao: (token: string) => api.get<void>('/auth/redefinir-senha', { token }),
  redefinirSenha: (token: string, novaSenha: string) =>
    api.post<void>('/auth/redefinir-senha', { token, novaSenha }),
  primeiroAcesso: (novaSenha: string, aceiteTermos: boolean) =>
    api.post<void>('/auth/primeiro-acesso', { novaSenha, aceiteTermos }),
}
