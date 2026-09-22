import type { EgressoPainel } from '../models/egresso'
import { api } from './client'

export const egressosApi = {
  painel: () => api.get<EgressoPainel>('/egressos/me'),
  baixar: (href: string) => api.getBlob(href),
}
