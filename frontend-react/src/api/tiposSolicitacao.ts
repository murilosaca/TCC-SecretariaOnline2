import type { PageResponse } from '../models/academico'
import type { JsonSchema, RequestType } from '../models/solicitacao'
import { api } from './client'

export type TipoEditor = RequestType

export const tiposSolicitacaoApi = {
  listar: (page = 0) =>
    api.get<PageResponse<TipoEditor>>('/admin/tipos-solicitacao', { page, size: 50 }),
  criar: (body: {
    codigo: string
    nome: string
    descricao?: string
    prazoDias?: number
    formSchema: string
    workflowJson: string
  }) => api.post<TipoEditor>('/admin/tipos-solicitacao', body),
  publicar: (id: string, formSchema: string, workflowJson: string) =>
    api.post<TipoEditor>(`/admin/tipos-solicitacao/${id}/publicar`, { formSchema, workflowJson }),
  excluir: (path: string) => api.delete(path),
}

export function erroJson(texto: string): string | null {
  try {
    const valor = JSON.parse(texto) as unknown
    if (valor === null || typeof valor !== 'object' || Array.isArray(valor)) {
      return 'JSON inválido na linha 1'
    }
    return null
  } catch (error) {
    const mensagem = error instanceof SyntaxError ? error.message : 'JSON inválido'
    const linhaExplicita = /line (\d+)/.exec(mensagem)
    if (linhaExplicita) {
      return `JSON inválido na linha ${linhaExplicita[1]}`
    }
    const posicao = /position (\d+)/.exec(mensagem)
    if (!posicao) {
      return 'JSON inválido na linha 1'
    }
    const linha = texto.slice(0, Number(posicao[1])).split('\n').length
    return `JSON inválido na linha ${linha}`
  }
}

export function schemaDe(texto: string): JsonSchema | null {
  if (erroJson(texto)) {
    return null
  }
  return JSON.parse(texto) as JsonSchema
}
