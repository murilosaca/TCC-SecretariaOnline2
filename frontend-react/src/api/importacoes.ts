import { api } from './client'

export interface ImportacaoLinha {
  numero: number
  status: string
  mensagem: string | null
  valores: Record<string, string>
}

export interface ImportacaoJob {
  id: string
  kind: string
  status: string
  nomeArquivo: string
  checksumSha256: string
  totalLinhas: number
  validCount: number
  errorCount: number
  warningCount: number
  importadas: number
  mensagem: string | null
  createdAt: string
  updatedAt: string
  linhas?: ImportacaoLinha[] | null
  _links: Record<string, string>
}

export const importacoesApi = {
  modelo: (kind: string) => api.getBlob(`/importacoes/modelos/${kind}`, 'text/csv'),
  enviar: (kind: string, arquivo: File) => {
    const form = new FormData()
    form.append('arquivo', arquivo)
    return api.postForm<ImportacaoJob>(`/importacoes/${kind}`, form)
  },
  obter: (id: string) => api.get<ImportacaoJob>(`/importacoes/${id}`),
  confirmar: (id: string) => api.post<ImportacaoJob>(`/importacoes/${id}/confirmar`),
}
