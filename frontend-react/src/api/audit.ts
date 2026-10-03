import { api } from './client'

export interface AuditLogItem {
  id: string
  atorId: string | null
  atorNome: string | null
  acao: string
  entidade: string | null
  ip: string | null
  timestamp: string
  payloadAntes: string | null
  payloadDepois: string | null
}

export interface AuditLogPage {
  content: AuditLogItem[]
  page: { number: number; size: number; totalElements: number; totalPages: number }
}

export const auditApi = {
  listar: (params: { ator?: string; acao?: string; de?: string; ate?: string; page?: number; size?: number }) =>
    api.get<AuditLogPage>('/audit-log', params),
}
