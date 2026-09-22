import { api } from './client'
import type { CoePool } from '../models/coe'

export const coeApi = {
  pool: () => api.get<CoePool>('/comissoes/coe'),
  atribuir: (estagioId: string, assigneeId: string) =>
    api.post<CoePool>('/comissoes/coe/atribuicoes', { estagioId, assigneeId }),
}
