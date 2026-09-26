import { api } from './client'
import type { CaafPool } from '../models/caaf'

export const caafApi = {
  pool: () => api.get<CaafPool>('/comissoes/caaf'),
  atribuir: (formativaId: string, assigneeId: string) =>
    api.post<CaafPool>('/comissoes/caaf/atribuicoes', { formativaId, assigneeId }),
  aprovarLote: (ids: string[]) =>
    api.post<CaafPool>('/comissoes/caaf/lote', { ids, decisao: 'APROVADA' }),
}
