import { apiGet, apiPost } from './client';
import { loginBody } from './nativeClient';
import type { HateoasLinks } from '../models/hateoas';

export type LoginResponse = {
  accessToken: string;
  mustChangePassword: boolean;
  expiresIn: number;
  tokenType: string;
  refreshToken?: string;
};

export type Sessao = {
  id: string;
  mustChangePassword: boolean;
  authorities: string[];
  _links?: HateoasLinks;
};

export const authApi = {
  login: (identificador: string, senha: string) =>
    apiPost<LoginResponse>('/auth/login', loginBody(identificador, senha)),
  refresh: (refreshToken: string) => apiPost<LoginResponse>('/auth/refresh', { refreshToken }),
  logout: (refreshToken?: string | null) =>
    apiPost<void>('/auth/logout', refreshToken ? { refreshToken } : undefined),
  me: () => apiGet<Sessao>('/auth/me'),
  recuperarSenha: (email: string) => apiPost<void>('/auth/recuperar-senha', { email }),
  primeiroAcesso: (novaSenha: string, aceiteTermos: boolean) =>
    apiPost<void>('/auth/primeiro-acesso', { novaSenha, aceiteTermos }),
};
