import type { PageResponse } from '../models/academico'
import { api } from './client'

export type OutboxEvento = {
  id: string
  tipo: string
  payloadResumo: string
  status: string
  tentativas: number
  createdAt: string
  _links?: Record<string, string>
}

export type JobCard = {
  nome: string
  frequencia: string
  ultimoRun?: string | null
  proximoRun?: string | null
  status: string
}

export type OutboxLista = PageResponse<OutboxEvento> & {
  alertaLatencia?: string | null
  jobs: JobCard[]
}

export const jobsApi = {
  listar: (status?: string, tipo?: string, page = 0) =>
    api.get<OutboxLista>('/admin/outbox', { status, tipo, page, size: 20 }),
  reentregar: (path: string) => api.post<OutboxEvento>(path),
}
