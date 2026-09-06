import type { HateoasLinks, PageResponse } from './academico'

export type JsonSchemaProperty = {
  type?: 'string' | 'number' | 'integer' | 'boolean'
  title?: string
  description?: string
  enum?: Array<string | number>
  minLength?: number
  maxLength?: number
  minimum?: number
  maximum?: number
  format?: string
}

export type JsonSchema = {
  type?: string
  title?: string
  required?: string[]
  additionalProperties?: boolean
  properties?: Record<string, JsonSchemaProperty>
}

export type RequestType = {
  id: string
  codigo: string
  nome: string
  descricao?: string | null
  status: string
  prazoDias: number
  versao: number
  formSchema: JsonSchema
  workflowJson: Record<string, unknown>
  _links?: HateoasLinks
}

export type SolicitacaoEvento = {
  id: string
  tipo: string
  estadoDe?: string | null
  estadoPara: string
  atorId?: string | null
  parecer?: string | null
  createdAt: string
}

export type Solicitacao = {
  id: string
  protocolo: string
  tipoCodigo: string
  tipoNome: string
  tipoVersao: number
  estado: string
  payload: Record<string, unknown>
  formSchema?: JsonSchema
  prazoEm: string
  prazoVencido: boolean
  sla: 'NO_PRAZO' | 'ATRASADO'
  createdAt: string
  updatedAt: string
  eventos: SolicitacaoEvento[]
  _links?: HateoasLinks
}

export type SolicitacaoPage = PageResponse<Solicitacao>
export type RequestTypePage = PageResponse<RequestType>
