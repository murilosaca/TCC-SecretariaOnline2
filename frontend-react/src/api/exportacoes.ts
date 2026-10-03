import { api } from './client'

export interface ExportacaoKind {
  kind: string
  titulo: string
}

export interface ExportacaoJob {
  id: string
  kind: string
  titulo: string
  status: string
  nomeArquivo: string | null
  expiresAt: string | null
  mensagem: string | null
  createdAt: string
  _links: Record<string, string>
}

export interface ExportacoesPage {
  kinds: ExportacaoKind[]
  content: ExportacaoJob[]
  _links: Record<string, string>
}

export const exportacoesApi = {
  listar: () => api.get<ExportacoesPage>('/exportacoes'),
  solicitar: (kind: string) => api.post<ExportacaoJob>(`/exportacoes/${kind}`, {}),
  obter: (id: string) => api.get<ExportacaoJob>(`/exportacoes/${id}`),
  download: (id: string) => api.get<{ url: string; expiresInSeconds: number }>(`/exportacoes/${id}/download`),
}
