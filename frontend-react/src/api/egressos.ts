import type { EgressoPainel } from '../models/egresso'
import { api } from './client'
import { baixarViaPresign } from './download'

export const egressosApi = {
  painel: () => api.get<EgressoPainel>('/egressos/me'),
  baixar: (href: string, nomeArquivo: string) => baixarViaPresign(href, nomeArquivo),
}
