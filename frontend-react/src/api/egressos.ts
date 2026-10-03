import type { EgressoPage, EgressoPainel } from '../models/egresso'
import { api } from './client'
import { baixarViaPresign } from './download'

export const egressosApi = {
  painel: () => api.get<EgressoPainel>('/egressos/me'),
  baixar: (href: string, nomeArquivo: string) => baixarViaPresign(href, nomeArquivo),
  listar: (filtros: {
    cursoId?: string
    ano?: string
    situacao?: string
    page?: number
    size?: number
  } = {}) =>
    api.get<EgressoPage>('/egressos', {
      cursoId: filtros.cursoId,
      ano: filtros.ano,
      situacao: filtros.situacao,
      page: filtros.page ?? 0,
      size: filtros.size ?? 20,
    }),
  exportarCsv: (filtros: { cursoId?: string; ano?: string; situacao?: string; page?: number; size?: number } = {}) =>
    api.getBlob(
      `/egressos${qs({
        format: 'csv',
        cursoId: filtros.cursoId,
        ano: filtros.ano,
        situacao: filtros.situacao,
        page: filtros.page ?? 0,
        size: filtros.size ?? 20,
      })}`,
      'text/csv',
    ),
}

function qs(params: Record<string, string | number | undefined>): string {
  const search = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== '') {
      search.set(key, String(value))
    }
  })
  const query = search.toString()
  return query ? `?${query}` : ''
}
