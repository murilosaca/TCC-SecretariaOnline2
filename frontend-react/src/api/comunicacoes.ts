import { api } from './client'
import type { AudienciaComunicacao, HubComunicacao, PublicarComunicacao } from '../models/comunicacao'

export type FiltroComunicacao = {
  aba: string
  lido?: string
  tipo?: string
}

export const comunicacoesApi = {
  listar: (filtro: FiltroComunicacao) =>
    api.get<HubComunicacao>('/communications', {
      aba: filtro.aba,
      lido: filtro.lido || undefined,
      tipo: filtro.tipo || undefined,
    }),
  marcarLida: (href: string) => api.post<void>(href),
  audiencias: () => api.get<{ content: AudienciaComunicacao[] }>('/communications/audiencias'),
  publicar: (body: PublicarComunicacao) => api.post<unknown>('/communications', body),
}
